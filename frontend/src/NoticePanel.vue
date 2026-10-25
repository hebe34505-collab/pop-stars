<script setup>
import { onMounted, ref } from 'vue'
import { request } from './api.js'

const notices = ref([])
const errorText = ref('')

async function load() {
  errorText.value = ''
  try {
    notices.value = await request('/api/notices')
  } catch (error) {
    errorText.value = error.message
  }
}

onMounted(load)
</script>

<template>
  <section>
    <h3>公告</h3>
    <p v-if="errorText" class="error">{{ errorText }}</p>
    <p v-else-if="notices.length === 0" class="tip">暂时没有公告。</p>
    <article v-for="item in notices" :key="item.id" class="note">
      <h3>{{ item.title }}</h3>
      <p>{{ item.content }}</p>
      <span>{{ item.createdAt }}</span>
    </article>
  </section>
</template>
