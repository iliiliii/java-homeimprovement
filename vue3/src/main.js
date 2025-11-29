import { createApp } from 'vue'

import Cookies from 'js-cookie'

import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import 'element-plus/theme-chalk/dark/css-vars.css'
import locale from 'element-plus/es/locale/lang/zh-cn'

import '@/assets/styles/index.scss' // global css

// 导入 v-viewer
import 'viewerjs/dist/viewer.css'
import VueViewer from 'v-viewer'

import App from './App'
import store from './store'
import router from './router'
import directive from './directive' // directive

// 注册指令
import plugins from './plugins' // plugins
import { download } from '@/utils/request'

// svg图标
import 'virtual:svg-icons-register'
import SvgIcon from '@/components/SvgIcon'
import elementIcons from '@/components/SvgIcon/svgicon'

import './permission' // permission control

import { useDict } from '@/utils/dict'
import { getConfigKey } from "@/api/system/config"
import { parseTime, resetForm, addDateRange, handleTree, selectDictLabel, selectDictLabels } from '@/utils/ruoyi'

// 分页组件
import Pagination from '@/components/Pagination'
// 自定义表格工具组件
import RightToolbar from '@/components/RightToolbar'
// 富文本组件
import Editor from "@/components/Editor"
// 文件上传组件
import FileUpload from "@/components/FileUpload"
// 图片上传组件
import ImageUpload from "@/components/ImageUpload"
// 图片预览组件
import ImagePreview from "@/components/ImagePreview"
// 懒加载图片组件
import LazyImage from "@/components/LazyImage"
// 字典标签组件
import DictTag from '@/components/DictTag'

const app = createApp(App)

// 全局方法挂载
app.config.globalProperties.useDict = useDict
app.config.globalProperties.download = download
app.config.globalProperties.parseTime = parseTime
app.config.globalProperties.resetForm = resetForm
app.config.globalProperties.handleTree = handleTree
app.config.globalProperties.addDateRange = addDateRange
app.config.globalProperties.getConfigKey = getConfigKey
app.config.globalProperties.selectDictLabel = selectDictLabel
app.config.globalProperties.selectDictLabels = selectDictLabels

// 全局组件挂载
app.component('DictTag', DictTag)
app.component('Pagination', Pagination)
app.component('FileUpload', FileUpload)
app.component('ImageUpload', ImageUpload)
app.component('ImagePreview', ImagePreview)
app.component('LazyImage', LazyImage)
app.component('RightToolbar', RightToolbar)
app.component('Editor', Editor)

app.use(router)
app.use(store)
app.use(plugins)
app.use(elementIcons)
app.component('svg-icon', SvgIcon)
// 注册 v-viewer
app.use(VueViewer, {
  defaultOptions: {
    // 工具栏
    toolbar: true,
    // 显示缩放按钮
    zoomOn: true,
    // 显示缩小按钮
    zoomOff: true,
    // 显示旋转按钮
    rotateOn: true,
    // 显示翻转按钮
    flipHOn: true,
    // 显示全屏按钮
    fullScreen: true,
    // 显示上一张按钮
    prev: true,
    // 显示下一张按钮
    next: true,
    // 显示重置按钮
    reset: true,
    // 显示下载按钮
    download: true,
    // 导航栏
    navbar: true,
    // 标题
    title: false,
    // 按钮提示
    tooltip: true,
    // 可移动
    movable: true,
    // 可缩放
    zoomable: true,
    // 可旋转
    rotatable: true,
    // 可翻转
    flip: true,
    // 动画
    transition: true,
    // 键盘导航
    keyboard: true,
    // 循环浏览
    loop: true,
    // 最小缩放比例
    minZoomRatio: 0.1,
    // 最大缩放比例
    maxZoomRatio: 5,
    // z-index
    zIndex: 9999
  }
})

directive(app)

// 使用element-plus 并且设置全局的大小
app.use(ElementPlus, {
  locale: locale,
  // 支持 large、default、small
  size: Cookies.get('size') || 'default'
})

app.mount('#app')
