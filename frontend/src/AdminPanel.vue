<script setup>
import { onMounted, ref } from 'vue'
import { request } from './api.js'

const users = ref([])
const scores = ref([])
const levels = ref([])
const notices = ref([])
const messages = ref([])
const noticeForm = ref({ title: '', content: '' })
const form = ref(null)
const message = ref('')
const errorText = ref('')

async function load() {
  errorText.value = ''
  try {
    users.value = await request('/api/admin/users')
    scores.value = await request('/api/admin/scores')
    levels.value = await request('/api/levels')
    notices.value = await request('/api/notices')
    messages.value = await request('/api/admin/service')
    if (!form.value && levels.value.length > 0) {
      selectLevel(levels.value[0])
    }
  } catch (error) {
    errorText.value = error.message
  }
}

function selectLevel(item) {
  form.value = {
    levelNo: item.levelNo,
    rows: item.rows,
    cols: item.cols,
    colorCount: item.colorCount,
    targetScore: item.targetScore,
    maxMoves: item.maxMoves
  }
  message.value = ''
}

async function saveLevel() {
  message.value = ''
  errorText.value = ''
  try {
    const saved = await request('/api/admin/levels/' + form.value.levelNo, {
      method: 'PUT',
      body: JSON.stringify({
        rows: Number(form.value.rows),
        cols: Number(form.value.cols),
        colorCount: Number(form.value.colorCount),
        targetScore: Number(form.value.targetScore),
        maxMoves: Number(form.value.maxMoves)
      })
    })
    message.value = '第 ' + saved.levelNo + ' 关已保存。回到游戏后点重新开始即可生效。'
    levels.value = await request('/api/levels')
  } catch (error) {
    errorText.value = error.message
  }
}

async function deleteUser(user) {
  if (!window.confirm('确定删除用户 ' + user.username + ' 吗？该用户的成绩也会一起删除。')) {
    return
  }
  errorText.value = ''
  try {
    await request('/api/admin/users/' + user.id, { method: 'DELETE' })
    await load()
  } catch (error) {
    errorText.value = error.message
  }
}

async function publishNotice() {
  message.value = ''
  errorText.value = ''
  try {
    await request('/api/admin/notices', {
      method: 'POST',
      body: JSON.stringify(noticeForm.value)
    })
    noticeForm.value = { title: '', content: '' }
    message.value = '公告已发布'
    notices.value = await request('/api/notices')
  } catch (error) {
    errorText.value = error.message
  }
}

async function deleteNotice(item) {
  if (!window.confirm('确定删除这条公告吗？')) {
    return
  }
  errorText.value = ''
  await request('/api/admin/notices/' + item.id, { method: 'DELETE' })
  notices.value = await request('/api/notices')
}

async function replyMessage(item) {
  errorText.value = ''
  const reply = window.prompt('回复 ' + item.nickname, item.reply || '')
  if (!reply) {
    return
  }
  try {
    await request('/api/admin/service/' + item.id, {
      method: 'PUT',
      body: JSON.stringify({ reply })
    })
    messages.value = await request('/api/admin/service')
  } catch (error) {
    errorText.value = error.message
  }
}

async function deleteScore(item) {
  if (!window.confirm('确定删除这条成绩吗？')) {
    return
  }
  errorText.value = ''
  try {
    await request('/api/admin/scores/' + item.id, { method: 'DELETE' })
    scores.value = await request('/api/admin/scores')
  } catch (error) {
    errorText.value = error.message
  }
}

onMounted(load)
</script>

<template>
  <section>
    <p v-if="errorText" class="error">{{ errorText }}</p>
    <p v-if="message" class="tip">{{ message }}</p>

    <h3>公告</h3>
    <form class="form" @submit.prevent="publishNotice">
      <label>标题 <input v-model="noticeForm.title" maxlength="30" /></label>
      <label>内容 <input v-model="noticeForm.content" maxlength="300" /></label>
      <button class="restart" type="submit">发布公告</button>
    </form>
    <div v-for="item in notices" :key="item.id" class="person">
      <span>{{ item.title }}</span>
      <button class="text-button" type="button" @click="deleteNotice(item)">删除</button>
    </div>

    <h3>客服留言</h3>
    <p v-if="messages.length === 0" class="tip">还没有玩家留言。</p>
    <article v-for="item in messages" :key="item.id" class="note">
      <p>{{ item.nickname }}：{{ item.content }}</p>
      <p v-if="item.reply" class="reply">已回复：{{ item.reply }}</p>
      <button class="text-button" type="button" @click="replyMessage(item)">回复</button>
    </article>

    <h3>关卡参数</h3>
    <div class="levels">
      <button
        v-for="item in levels"
        :key="item.levelNo"
        type="button"
        class="nav-button"
        :class="{ active: form && form.levelNo === item.levelNo }"
        @click="selectLevel(item)"
      >
        第{{ item.levelNo }}关
      </button>
    </div>
    <form v-if="form" class="form" @submit.prevent="saveLevel">
      <label>行数 <input v-model="form.rows" type="number" min="4" max="12" /></label>
      <label>列数 <input v-model="form.cols" type="number" min="4" max="12" /></label>
      <label>颜色数量 <input v-model="form.colorCount" type="number" min="2" max="6" /></label>
      <label>本关需得分 <input v-model="form.targetScore" type="number" min="1" /></label>
      <label>步数 <input v-model="form.maxMoves" type="number" min="1" /></label>
      <button class="restart" type="submit">保存关卡</button>
    </form>
    <p class="hint">本关需得分是这一关要新拿到的分数。玩家实际过关线 = 上一关结算分 + 本关需得分。星星颜色每次开局随机生成。</p>

    <h3>注册用户</h3>
    <table class="data-table">
      <thead>
        <tr>
          <th>用户名</th>
          <th>昵称</th>
          <th>角色</th>
          <th>注册时间</th>
          <th></th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="user in users" :key="user.id">
          <td>{{ user.username }}</td>
          <td>{{ user.nickname }}</td>
          <td>{{ user.role === 'ADMIN' ? '管理员' : '玩家' }}</td>
          <td>{{ user.createdAt }}</td>
          <td>
            <button v-if="user.role !== 'ADMIN'" class="text-button" type="button" @click="deleteUser(user)">删除</button>
          </td>
        </tr>
      </tbody>
    </table>

    <h3>全部成绩</h3>
    <p v-if="scores.length === 0" class="tip">还没有成绩记录。</p>
    <table v-else class="data-table">
      <thead>
        <tr>
          <th>时间</th>
          <th>玩家</th>
          <th>关卡</th>
          <th>分数</th>
          <th>结果</th>
          <th></th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="item in scores" :key="item.id">
          <td>{{ item.createdAt }}</td>
          <td>{{ item.nickname }}</td>
          <td>第 {{ item.levelNo }} 关</td>
          <td>{{ item.score }}</td>
          <td>{{ item.win ? '胜利' : '失败' }}</td>
          <td><button class="text-button" type="button" @click="deleteScore(item)">删除</button></td>
        </tr>
      </tbody>
    </table>
  </section>
</template>
