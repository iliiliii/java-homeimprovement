<template>
  <view class="webview-page">
    <web-view :src="url" @message="handleMessage" />
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const url = ref('')
const title = ref('文档')

onMounted(() => {
  // 获取页面参数
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const options = currentPage.options || currentPage.$page?.options || {}
  
  if (options.url) {
    url.value = decodeURIComponent(options.url)
  }
  
  if (options.title) {
    title.value = decodeURIComponent(options.title)
    // 设置导航栏标题
    uni.setNavigationBarTitle({ title: title.value })
  }
})

const handleMessage = (e) => {
  console.log('WebView Message:', e.detail.data)
}
</script>

<style lang="scss" scoped>
.webview-page {
  width: 100%;
  height: 100vh;
}
</style>
