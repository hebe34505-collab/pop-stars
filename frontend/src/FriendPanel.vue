<script setup>
import { onMounted, ref } from 'vue'
import { request } from './api.js'

const board = ref({ friends: [], incoming: [], outgoing: [] })
const username = ref('')
const errorText = ref('')
const tip = ref('')

async function load() {
  board.value = await request('/api/friends')
}

async function addFriend() {
  errorText.value = ''
  tip.value = ''
  try {
    await request('/api/friends', {
      method: 'POST',
      body: JSON.stringify({ username: username.value })
    })
    username.value = ''
    tip.value = '申请已发出'
    await load()
  } catch (error) {
    errorText.value = error.message
  }
}

async function accept(item) {
  errorText.value = ''
  await request('/api/friends/' + item.id + '/accept', { method: 'POST' })
  await load()
}

async function remove(item) {
  errorText.value = ''
  await request('/api/friends/' + item.id, { method: 'DELETE' })
  await load()
}

onMounted(async () => {
  try {
    await load()
  } catch (error) {
    errorText.value = error.message
  }
})
</script>

<template>
  <section>
    <h3>添加好友</h3>
    <form class="form" @submit.prevent="addFriend">
      <label>对方用户名 <input v-model="username" maxlength="16" placeholder="填写注册时的用户名" /></label>
      <button class="restart" type="submit">发送申请</button>
    </form>
    <p v-if="errorText" class="error">{{ errorText }}</p>
    <p v-if="tip" class="ok">{{ tip }}</p>

    <h3>收到的申请</h3>
    <p v-if="board.incoming.length === 0" class="tip">没有新的申请。</p>
    <div v-for="item in board.incoming" :key="item.id" class="person">
      <span>{{ item.nickname }}（{{ item.username }}）</span>
      <button class="text-button" type="button" @click="accept(item)">同意</button>
      <button class="text-button" type="button" @click="remove(item)">拒绝</button>
    </div>

    <h3>我的好友</h3>
    <p v-if="board.friends.length === 0" class="tip">还没有好友。</p>
    <div v-for="item in board.friends" :key="item.id" class="person">
      <span>{{ item.nickname }}（{{ item.username }}）</span>
      <button class="text-button" type="button" @click="remove(item)">删除</button>
    </div>

    <h3>等待对方同意</h3>
    <p v-if="board.outgoing.length === 0" class="tip">没有发出的申请。</p>
    <div v-for="item in board.outgoing" :key="'out-' + item.id" class="person">
      <span>{{ item.nickname }}（{{ item.username }}）</span>
      <button class="text-button" type="button" @click="remove(item)">取消</button>
    </div>
  </section>
</template>
