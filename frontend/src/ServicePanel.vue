<script setup>
import { onMounted, ref } from 'vue'
import { request } from './api.js'

const messages = ref([])
const content = ref('')
const errorText = ref('')
const tip = ref('')

async function load() {
  errorText.value = ''
  messages.value = await request('/api/service/mine')
}

async function send() {
  errorText.value = ''
  tip.value = ''
  try {
    await request('/api/service', {
      method: 'POST',
      body: JSON.stringify({ content: content.value })
    })
    content.value = ''
    tip.value = '已发送，请等待客服回复'
    await load()
  } catch (error) {
    errorText.value = error.message
  }
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
    <h3>联系客服</h3>
    <p class="hint">写下你的问题，管理员会在管理页回复。回复后刷新本页就能看到。</p>
    <form class="form" @submit.prevent="send">
      <label>留言 <textarea v-model="content" maxlength="200" rows="3" placeholder="1到200个字"></textarea></label>
      <button class="restart" type="submit">发送</button>
    </form>
    <p v-if="errorText" class="error">{{ errorText }}</p>
    <p v-if="tip" class="ok">{{ tip }}</p>
    <p v-if="messages.length === 0" class="tip">还没有留言。</p>
    <article v-for="item in messages" :key="item.id" class="note">
      <p>{{ item.content }}</p>
      <span>{{ item.createdAt }}</span>
      <p v-if="item.reply" class="reply">客服回复：{{ item.reply }}</p>
      <p v-else class="hint">等待回复</p>
    </article>
  </section>
</template>
