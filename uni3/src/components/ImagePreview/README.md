# 图片预览组件

为 UniApp Vue3 微信小程序设计的图片预览组件集。

## 组件说明

### 1. ImagePreview - 图片网格预览

用于展示图片列表，支持点击预览、删除、添加等功能。

```vue
<template>
  <ImagePreview 
    :images="imageList"
    :columns="3"
    :max-count="9"
    :deletable="false"
    :show-add="false"
    @preview="handlePreview"
    @delete="handleDelete"
  />
</template>

<script setup>
import ImagePreview from '@/components/ImagePreview/index.vue'

const imageList = [
  'https://example.com/image1.jpg',
  'https://example.com/image2.jpg',
  // 或者对象格式
  { url: 'https://example.com/image3.jpg', name: '客厅效果图' }
]
</script>
```

#### Props

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| images | Array | [] | 图片列表，支持字符串或对象数组 |
| urlKey | String | 'url' | 图片URL字段名 |
| columns | Number | 3 | 列数 (1-4) |
| imageMode | String | 'aspectFill' | 图片裁剪模式 |
| maxCount | Number | 9 | 最大显示数量 |
| showMore | Boolean | true | 超出时显示+N |
| deletable | Boolean | false | 是否可删除 |
| showAdd | Boolean | false | 是否显示添加按钮 |
| addText | String | '' | 添加按钮文字 |
| lazyLoad | Boolean | true | 懒加载 |
| gap | Number/String | 16 | 间距 (rpx) |
| radius | Number/String | 16 | 圆角 (rpx) |

#### Events

| 事件 | 参数 | 说明 |
|------|------|------|
| preview | { index, images } | 预览图片 |
| click | { index, image } | 点击图片 |
| delete | { index, image } | 删除图片 |
| add | - | 添加图片 |

---

### 2. ImageViewer - 全屏图片查看器

用于全屏查看图片，支持手势缩放、滑动切换、保存图片等功能。

```vue
<template>
  <view @click="openViewer">查看设计图</view>
  
  <ImageViewer 
    v-model:visible="showViewer"
    :images="designImages"
    :start-index="0"
    :show-info="true"
    @save="handleSave"
  />
</template>

<script setup>
import { ref } from 'vue'
import ImageViewer from '@/components/ImageViewer/index.vue'

const showViewer = ref(false)
const designImages = [
  { url: 'https://...', name: '客厅效果图 v2.0', desc: '更新于 2024-01-15' },
  { url: 'https://...', name: '卧室效果图 v1.5', desc: '更新于 2024-01-14' }
]

const openViewer = () => {
  showViewer.value = true
}
</script>
```

#### Props

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| visible | Boolean | false | 是否显示 (v-model) |
| images | Array | [] | 图片列表 |
| urlKey | String | 'url' | 图片URL字段名 |
| nameKey | String | 'name' | 图片名称字段名 |
| descKey | String | 'desc' | 图片描述字段名 |
| startIndex | Number | 0 | 初始显示索引 |
| showInfo | Boolean | true | 是否显示信息栏 |
| showThumbnail | Boolean | false | 是否显示缩略图导航 |
| closeOnClick | Boolean | true | 点击图片关闭 |

#### Events

| 事件 | 参数 | 说明 |
|------|------|------|
| close | - | 关闭查看器 |
| change | { index, image } | 切换图片 |
| save | { index, url } | 保存图片 |

---

## 技术选型说明

### 为什么不使用第三方库？

1. **兼容性考虑**：大多数 Web 端图片预览库（如 PhotoSwipe、Viewer.js）在小程序环境不兼容
2. **包体积**：小程序对包体积有严格限制，自定义组件更轻量
3. **原生体验**：基于 `uni.previewImage` 和 `movable-area` 实现，保证原生般的流畅体验
4. **可定制性**：完全可控的样式和功能，满足设计稿需求

### 核心技术实现

- **图片预览**：使用 `uni.previewImage` 原生 API，最稳定可靠
- **手势缩放**：使用 `movable-area` + `movable-view` 组件
- **滑动切换**：使用 `swiper` 组件
- **保存图片**：使用 `uni.downloadFile` + `uni.saveImageToPhotosAlbum`

### 小程序兼容性

| 功能 | 微信小程序 | H5 | App |
|------|-----------|----|----|
| 图片预览 | ✅ | ✅ | ✅ |
| 手势缩放 | ✅ | ✅ | ✅ |
| 保存图片 | ✅ | ⚠️ 降级 | ✅ |
| 长按菜单 | ✅ | - | ✅ |
