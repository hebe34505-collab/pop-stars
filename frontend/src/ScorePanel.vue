<script setup>
import { onMounted, ref } from 'vue'
import { request } from './api.js'

const bestScore = ref(0)
const records = ref([])
const errorText = ref('')

async function load() {
  errorText.value = ''
  try {
    const data = await request('/api/scores/me')
    bestScore.value = data.bestScore
    records.value = data.records
  } catch (error) {
    errorText.value = error.message
  }
}

onMounted(load)
</script>

<template>
  <section>
    <div class="best">
      <span>个人最高分</span>
      <strong>{{ bestScore }}</strong>
    </div>
    <p v-if="errorText" class="error">{{ errorText }}</p>
    <p v-else-if="records.length === 0" class="tip">还没有成绩。完成一局游戏后会自动保存。</p>
    <table v-else class="data-table">
      <thead>
        <tr>
          <th>时间</th>
          <th>关卡</th>
          <th>分数</th>
          <th>目标</th>
          <th>用步</th>
          <th>结果</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="item in records" :key="item.id">
          <td>{{ item.createdAt }}</td>
          <td>第 {{ item.levelNo }} 关</td>
          <td>{{ item.score }}</td>
          <td>{{ item.targetScore }}</td>
          <td>{{ item.movesUsed }}</td>
          <td>{{ item.win ? '胜利' : '失败' }}</td>
        </tr>
      </tbody>
    </table>
  </section>
</template>
