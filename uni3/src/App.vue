<script setup>
import { onLaunch, onShow, onHide } from '@dcloudio/uni-app'

onLaunch(() => {
  console.log('App Launch')
})

onShow(() => {
  console.log('App Show')
})

onHide(() => {
  console.log('App Hide')
})
</script>

<style lang="scss">
// uview-plus 样式 (变量已通过 vite.config.js 全局引入)
@import 'uview-plus/index.scss';
// 全局样式
@import './styles/global.scss';
</style>

