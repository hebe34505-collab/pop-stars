<script setup>
import { computed, onMounted, ref } from 'vue'
import {
  calculateScore,
  createPlayableBoard,
  dropStars,
  findGroup,
  hasRemovableGroup,
  moveColumnsLeft,
  removeStars
} from './game.js'
import { getToken, request, setToken } from './api.js'
import ScorePanel from './ScorePanel.vue'
import AdminPanel from './AdminPanel.vue'
import NoticePanel from './NoticePanel.vue'
import ServicePanel from './ServicePanel.vue'
import FriendPanel from './FriendPanel.vue'

const levelList = ref([])
const currentNo = ref(1)
const unlockedLevel = ref(1)
const level = ref(null)
const board = ref([])
const score = ref(0)
const startScore = ref(0)
const scoreRecords = ref([])
const movesLeft = ref(0)
const tip = ref('正在读取关卡...')
const errorText = ref('')
const result = ref(null)
const busy = ref(false)
const selectedKeys = ref([])
const user = ref(null)
const ready = ref(false)
const page = ref('game')
const authMode = ref('login')
const authError = ref('')
const authTip = ref('')
const form = ref({ username: '', password: '', nickname: '' })
const nicknameInput = ref('')
const editingName = ref(false)
const shakingId = ref(0)
const notices = ref([])

const stars = computed(() => {
  const list = []
  board.value.forEach((row, rowIndex) => {
    row.forEach((cell, colIndex) => {
      if (!cell) {
        return
      }
      list.push({
        id: cell.id,
        color: cell.color,
        removing: cell.removing,
        row: rowIndex,
        col: colIndex
      })
    })
  })
  return list
})

const hasNext = computed(() => {
  if (!level.value) {
    return false
  }
  return levelList.value.some((item) => item.levelNo === level.value.levelNo + 1)
})

const boardStyle = computed(() => {
  if (!level.value) {
    return {}
  }
  return {
    '--rows': level.value.rows,
    '--cols': level.value.cols
  }
})

const passScore = computed(() => {
  if (!level.value) {
    return 0
  }
  return startScore.value + level.value.targetScore
})

const reachedGoal = computed(() => {
  return !!level.value && score.value >= passScore.value
})

const progress = computed(() => {
  if (!level.value || level.value.targetScore <= 0) {
    return 0
  }
  const gained = score.value - startScore.value
  const value = Math.round((gained / level.value.targetScore) * 100)
  if (value < 0) {
    return 0
  }
  return value > 100 ? 100 : value
})

function wait(ms) {
  return new Promise((resolve) => {
    setTimeout(resolve, ms)
  })
}

function cellKey(row, col) {
  return row + ',' + col
}

function isSelected(star) {
  return selectedKeys.value.includes(cellKey(star.row, star.col))
}

function isValidLevel(data) {
  return data.rows >= 4 && data.rows <= 12
    && data.cols >= 4 && data.cols <= 12
    && data.colorCount >= 2 && data.colorCount <= 6
    && data.targetScore > 0
    && data.maxMoves > 0
    && data.levelNo > 0
}

function isUnlocked(levelNo) {
  return levelNo <= unlockedLevel.value
}

function openNext(levelNo) {
  const next = levelList.value
    .map((item) => item.levelNo)
    .filter((no) => no > levelNo)
    .sort((a, b) => a - b)[0]
  if (next && next > unlockedLevel.value) {
    unlockedLevel.value = next
  }
}

function carriedScore(levelNo) {
  const list = levelList.value.slice().sort((a, b) => a.levelNo - b.levelNo)
  const index = list.findIndex((item) => item.levelNo === levelNo)
  if (index <= 0) {
    return 0
  }
  const previous = list[index - 1].levelNo
  const win = scoreRecords.value.find((item) => item.levelNo === previous && item.win)
  return win ? win.score : 0
}

async function loadProgress() {
  const data = await request('/api/scores/me')
  unlockedLevel.value = data.unlockedLevel || 1
  scoreRecords.value = data.records || []
}

async function loadLevels() {
  const response = await fetch('/api/levels')
  if (!response.ok) {
    throw new Error('关卡接口返回错误')
  }
  const list = await response.json()
  if (!Array.isArray(list) || list.length === 0 || list.some((item) => !isValidLevel(item))) {
    throw new Error('关卡配置超出可玩范围')
  }
  return list
}

async function restart(options) {
  if (busy.value) {
    return
  }
  errorText.value = ''
  result.value = null
  selectedKeys.value = []
  tip.value = ''
  try {
    const list = await loadLevels()
    levelList.value = list
    await loadProgress()
    if (options && options.continueLatest) {
      currentNo.value = unlockedLevel.value
    }
    if (!isUnlocked(currentNo.value)) {
      currentNo.value = list[0].levelNo
    }
    let config = list.find((item) => item.levelNo === currentNo.value)
    if (!config) {
      config = list[0]
      currentNo.value = config.levelNo
    }
    level.value = config
    startScore.value = carriedScore(config.levelNo)
    score.value = startScore.value
    movesLeft.value = config.maxMoves
    board.value = createPlayableBoard(config.rows, config.cols, config.colorCount)
    tip.value = ''
  } catch (error) {
    level.value = null
    board.value = []
    errorText.value = '读取关卡配置失败，请先启动后端'
    tip.value = ''
  }
}

async function goNext() {
  if (!level.value || !hasNext.value || busy.value) {
    return
  }
  if (!result.value) {
    if (!reachedGoal.value) {
      return
    }
    const error = await saveScore(true)
    if (error) {
      errorText.value = '成绩保存失败：' + error
      return
    }
  }
  currentNo.value = level.value.levelNo + 1
  await restart()
}

async function completeLevel() {
  if (!level.value || hasNext.value || busy.value || !reachedGoal.value || result.value) {
    return
  }
  const error = await saveScore(true)
  if (error) {
    errorText.value = '成绩保存失败：' + error
    return
  }
  result.value = {
    title: '全部通关',
    text: `第 ${level.value.levelNo} 关通过，当前分数 ${score.value}，全部关卡都完成了。成绩已保存`,
    showNext: false
  }
}

function clearHover() {
  selectedKeys.value = []
}

function handleHover(star) {
  if (busy.value || result.value) {
    return
  }
  const group = findGroup(board.value, star.row, star.col)
  if (group.length < 2) {
    selectedKeys.value = []
    return
  }
  selectedKeys.value = group.map(([row, col]) => cellKey(row, col))
}

function shakeStar(star) {
  shakingId.value = star.id
  window.setTimeout(() => {
    if (shakingId.value === star.id) {
      shakingId.value = 0
    }
  }, 280)
}

async function handleClick(star) {
  if (busy.value || result.value || star.removing) {
    return
  }
  const group = findGroup(board.value, star.row, star.col)
  if (group.length < 2) {
    shakeStar(star)
    return
  }

  busy.value = true
  selectedKeys.value = []
  const gained = calculateScore(group.length)
  score.value += gained
  movesLeft.value -= 1
  tip.value = `消除了 ${group.length} 颗星星，获得 ${gained} 分`

  group.forEach(([row, col]) => {
    board.value[row][col].removing = true
  })
  board.value = board.value.map((row) => row.slice())
  await wait(260)

  removeStars(board.value, group)
  dropStars(board.value)
  moveColumnsLeft(board.value)
  board.value = board.value.map((row) => row.slice())
  await finishTurn()
  busy.value = false
}

async function finishTurn() {
  const stuck = movesLeft.value <= 0 || !hasRemovableGroup(board.value)
  if (!stuck) {
    if (reachedGoal.value) {
      tip.value = hasNext.value
        ? '已达到目标，可以继续消除，或进入下一关'
        : '已达到目标，可以继续消除，或完成本关'
    }
    return
  }

  const win = reachedGoal.value
  let title = ''
  let text = ''
  if (win) {
    const passed = `第 ${level.value.levelNo} 关通过，当前分数 ${score.value}`
    title = hasNext.value ? '挑战成功' : '全部通关'
    text = hasNext.value ? passed + '，可以进入下一关' : passed + '，全部关卡都完成了'
    tip.value = title
  } else if (movesLeft.value <= 0) {
    title = '挑战失败'
    text = `步数已用完。当前分数 ${score.value}，目标 ${passScore.value} 分`
    tip.value = '步数用完了'
  } else {
    title = '挑战失败'
    text = `没有可以消除的星星了。当前分数 ${score.value}，目标 ${passScore.value} 分`
    tip.value = '没有可以消除的星星了'
  }

  const error = await saveScore(win)
  text += error ? '。成绩保存失败：' + error : '。成绩已保存'
  result.value = {
    title,
    text,
    showNext: win && hasNext.value && !error && isUnlocked(level.value.levelNo + 1)
  }
}

async function saveScore(win) {
  try {
    await request('/api/scores', {
      method: 'POST',
      body: JSON.stringify({
        levelNo: level.value.levelNo,
        score: score.value,
        targetScore: passScore.value,
        movesUsed: level.value.maxMoves - movesLeft.value,
        win
      })
    })
    if (win) {
      openNext(level.value.levelNo)
    }
    return ''
  } catch (error) {
    return error.message
  }
}

async function loadMe() {
  if (!getToken()) {
    user.value = null
    return
  }
  try {
    user.value = await request('/api/auth/me')
    nicknameInput.value = user.value.nickname
  } catch (error) {
    setToken('')
    user.value = null
  }
}

async function submitAuth() {
  authError.value = ''
  try {
    if (authMode.value === 'register') {
      await request('/api/auth/register', {
        method: 'POST',
        body: JSON.stringify(form.value)
      })
      authMode.value = 'login'
      authError.value = ''
      authTip.value = '注册成功，请登录'
      form.value.password = ''
      form.value.nickname = ''
      return
    }
    const resultData = await request('/api/auth/login', {
      method: 'POST',
      body: JSON.stringify({
        username: form.value.username,
        password: form.value.password
      })
    })
    setToken(resultData.token)
    user.value = resultData.user
    nicknameInput.value = resultData.user.nickname
    form.value.password = ''
    authTip.value = ''
    page.value = 'game'
    await restart({ continueLatest: true })
  } catch (error) {
    authError.value = error.message
    authTip.value = ''
  }
}

function showLogin() {
  authMode.value = 'login'
  authError.value = ''
  authTip.value = ''
}

function showRegister() {
  authMode.value = 'register'
  authError.value = ''
  authTip.value = ''
}

function startEditName() {
  nicknameInput.value = user.value.nickname
  editingName.value = true
  authError.value = ''
}

async function saveNickname() {
  authError.value = ''
  try {
    user.value = await request('/api/auth/nickname', {
      method: 'PUT',
      body: JSON.stringify({ nickname: nicknameInput.value })
    })
    nicknameInput.value = user.value.nickname
    editingName.value = false
  } catch (error) {
    authError.value = error.message
  }
}

async function logout() {
  try {
    await request('/api/auth/logout', { method: 'POST' })
  } catch (error) {
    // 令牌失效时也直接清掉本地登录状态。
  }
  setToken('')
  user.value = null
  page.value = 'game'
  editingName.value = false
  authMode.value = 'login'
}

function openPage(name) {
  if (name === 'admin' && (!user.value || user.value.role !== 'ADMIN')) {
    return
  }
  page.value = name
}

async function loadNotices() {
  try {
    const response = await fetch('/api/notices')
    if (response.ok) {
      notices.value = await response.json()
    }
  } catch (error) {
    notices.value = []
  }
}

onMounted(async () => {
  await loadNotices()
  await loadMe()
  if (user.value) {
    await restart({ continueLatest: true })
  }
  ready.value = true
})
</script>

<template>
  <main class="page" :class="{ center: !user }">
    <p v-if="!ready" class="loading">正在进入...</p>

    <section v-else-if="!user" class="panel auth-panel">
      <header class="header">
        <p class="badge">★ 休闲消除</p>
        <h1>消灭星星</h1>
        <p class="subtitle">登录后开始消除，成绩会记在你的账号里</p>
      </header>

      <div class="switch">
        <button type="button" :class="{ active: authMode === 'login' }" @click="showLogin">登录</button>
        <button type="button" :class="{ active: authMode === 'register' }" @click="showRegister">注册</button>
      </div>

      <form class="form auth-form" @submit.prevent="submitAuth">
        <label>用户名 <input v-model="form.username" maxlength="16" placeholder="3到16位字母或数字" autocomplete="username" /></label>
        <label>密码 <input v-model="form.password" type="password" maxlength="20" placeholder="6到20位" autocomplete="current-password" /></label>
        <label v-if="authMode === 'register'">昵称 <input v-model="form.nickname" maxlength="16" placeholder="不填则与用户名相同" /></label>
        <p v-if="authError" class="error">{{ authError }}</p>
        <p v-else-if="authTip" class="ok">{{ authTip }}</p>
        <button class="restart" type="submit">{{ authMode === 'register' ? '注册' : '进入游戏' }}</button>
      </form>
      <article v-if="notices.length" class="note">
        <h3>{{ notices[0].title }}</h3>
        <p>{{ notices[0].content }}</p>
      </article>
    </section>

    <section v-else class="panel" :class="{ wide: page !== 'game' }">
      <header class="header">
        <p class="badge">★ 休闲消除</p>
        <h1>消灭星星</h1>
        <p class="subtitle">把相邻的同色星星点掉，达到目标分数就获胜</p>
      </header>

      <div class="toolbar">
        <button type="button" class="nav-button" :class="{ active: page === 'game' }" @click="openPage('game')">游戏</button>
        <button type="button" class="nav-button" :class="{ active: page === 'score' }" @click="openPage('score')">我的成绩</button>
        <button type="button" class="nav-button" :class="{ active: page === 'notice' }" @click="openPage('notice')">公告</button>
        <button type="button" class="nav-button" :class="{ active: page === 'service' }" @click="openPage('service')">客服</button>
        <button type="button" class="nav-button" :class="{ active: page === 'friend' }" @click="openPage('friend')">好友</button>
        <button v-if="user.role === 'ADMIN'" type="button" class="nav-button" :class="{ active: page === 'admin' }" @click="openPage('admin')">管理</button>
        <span class="toolbar-gap"></span>
        <button type="button" class="nickname" @click="startEditName">{{ user.nickname }}</button>
        <button type="button" class="text-button" @click="logout">退出</button>
      </div>

      <form v-if="editingName" class="form nickname-form" @submit.prevent="saveNickname">
        <label>修改昵称 <input v-model="nicknameInput" maxlength="16" /></label>
        <button class="text-button" type="submit">保存</button>
        <button class="text-button" type="button" @click="editingName = false">取消</button>
        <p v-if="authError" class="error">{{ authError }}</p>
      </form>

      <ScorePanel v-if="page === 'score'" />
      <NoticePanel v-else-if="page === 'notice'" />
      <ServicePanel v-else-if="page === 'service'" />
      <FriendPanel v-else-if="page === 'friend'" />
      <AdminPanel v-else-if="page === 'admin'" />

      <template v-else>
        <p v-if="level" class="level-name">第 {{ level.levelNo }} 关</p>
        <p v-if="level" class="hint carry">
          <template v-if="startScore > 0">从 {{ startScore }} 分继续。</template>
          达到 {{ passScore }} 分后还可以继续消除，再{{ hasNext ? '进入下一关' : '完成本关' }}。
        </p>

        <div v-if="level" class="stats">
          <div>
            <span>目标分数</span>
            <strong>{{ passScore }}</strong>
          </div>
          <div>
            <span>当前分数</span>
            <strong :key="score" class="bump">{{ score }}</strong>
          </div>
          <div>
            <span>剩余步数</span>
            <strong>{{ movesLeft }}</strong>
          </div>
        </div>

        <div v-if="level" class="progress" :aria-label="'已完成 ' + progress + '%'">
          <div class="progress-bar" :style="{ width: progress + '%' }"></div>
        </div>

        <p v-if="tip" class="tip">{{ tip }}</p>
        <p v-if="errorText" class="error">{{ errorText }}</p>

        <div v-if="level" class="board-wrap">
          <div class="board" :style="boardStyle">
            <button
              v-for="star in stars"
              :key="star.id"
              type="button"
              class="star"
              :class="['color-' + star.color, { removing: star.removing, selected: isSelected(star), shake: shakingId === star.id }]"
              :style="{ '--row': star.row, '--col': star.col }"
              :disabled="busy || !!result"
              @click="handleClick(star)"
              @mouseenter="handleHover(star)"
              @mouseleave="clearHover"
            ></button>
          </div>
        </div>

        <div class="actions">
          <button class="restart" type="button" :disabled="busy" @click="restart">
            {{ errorText ? '重新连接' : '重新开始' }}
          </button>
          <button v-if="reachedGoal && hasNext && !result" class="restart" type="button" :disabled="busy" @click="goNext">
            下一关
          </button>
          <button v-else-if="reachedGoal && !hasNext && !result" class="restart" type="button" :disabled="busy" @click="completeLevel">
            完成本关
          </button>
        </div>
      </template>
    </section>

    <div v-if="result" class="mask">
      <div class="dialog">
        <p class="badge">★ ★ ★</p>
        <h2>{{ result.title }}</h2>
        <p>{{ result.text }}</p>
        <div class="dialog-actions">
          <button v-if="result.showNext" class="restart" type="button" @click="goNext">下一关</button>
          <button class="restart" type="button" @click="restart">{{ result.showNext ? '再玩本关' : '再来一局' }}</button>
        </div>
      </div>
    </div>
  </main>
</template>
