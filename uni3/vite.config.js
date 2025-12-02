import { defineConfig } from 'vite'
import uni from '@dcloudio/vite-plugin-uni'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [uni()],
  css: {
    preprocessorOptions: {
      scss: {
        // 确保变量在所有样式之前加载
        additionalData: `@import "@/uni.scss"; @import "@/styles/variables.scss";`,
        // 静默废弃警告
        silenceDeprecations: ['legacy-js-api', 'import']
      }
    }
  }
})

