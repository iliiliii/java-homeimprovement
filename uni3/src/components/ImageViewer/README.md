# ImageViewer 图片预览组件

统一的图片预览组件，支持原生预览和自定义预览两种模式。

## 使用场景

### 1. 单张图片预览（推荐原生预览）
```vue
<template>
  <view>
    <image :src="imageUrl" @click="previewSingleImage" />
    
    <ImageViewer 
      v-model:visible="viewerVisible"
      :images="[imageUrl]"
      :use-native-preview="true"
    />
  </view>
</template>

<script setup>
import { ref } from 'vue'
import ImageViewer from '@/components/ImageViewer/index.vue'

const viewerVisible = ref(false)
const imageUrl = ref('https://example.com/image.jpg')

const previewSingleImage = () => {
  viewerVisible.value = true
}
</script>
```

### 2. 多张图片预览（推荐自定义预览）
```vue
<template>
  <view>
    <view class="image-grid">
      <image 
        v-for="(img, index) in images"
        :key="index"
        :src="img.url"
        @click="previewImages(index)"
      />
    </view>
    
    <ImageViewer 
      v-model:visible="viewerVisible"
      :images="images"
      :start-index="currentIndex"
      url-key="url"
      name-key="name"
      :show-thumbnail="true"
      :use-native-preview="false"
    />
  </view>
</template>

<script setup>
import { ref } from 'vue'
import ImageViewer from '@/components/ImageViewer/index.vue'

const viewerVisible = ref(false)
const currentIndex = ref(0)
const images = ref([
  { url: 'https://example.com/1.jpg', name: '图片1' },
  { url: 'https://example.com/2.jpg', name: '图片2' },
  { url: 'https://example.com/3.jpg', name: '图片3' }
])

const previewImages = (index) => {
  currentIndex.value = index
  viewerVisible.value = true
}
</script>
```

### 3. 头像预览（推荐原生预览）
```vue
<template>
  <view>
    <image 
      class="avatar"
      :src="userAvatar"
      @click="previewAvatar"
    />
    
    <ImageViewer 
      v-model:visible="avatarViewerVisible"
      :images="[userAvatar]"
      :use-native-preview="true"
    />
  </view>
</template>
```

### 4. 商品图片预览（推荐自定义预览）
```vue
<template>
  <view>
    <swiper class="product-images">
      <swiper-item 
        v-for="(img, index) in productImages"
        :key="index"
        @click="previewProduct(index)"
      >
        <image :src="img.url" />
      </swiper-item>
    </swiper>
    
    <ImageViewer 
      v-model:visible="productViewerVisible"
      :images="productImages"
      :start-index="productIndex"
      url-key="url"
      name-key="title"
      :show-thumbnail="true"
      :show-info="true"
      :use-native-preview="false"
    />
  </view>
</template>
```

## 组件属性

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| visible | Boolean | false | 是否显示预览 |
| images | Array | [] | 图片列表 |
| startIndex | Number | 0 | 初始显示的图片索引 |
| urlKey | String | 'url' | 图片URL字段名 |
| nameKey | String | 'name' | 图片名称字段名 |
| descKey | String | 'desc' | 图片描述字段名 |
| useNativePreview | Boolean | 全局配置 | 是否使用原生预览 |
| showInfo | Boolean | 全局配置 | 是否显示信息栏（自定义预览） |
| showThumbnail | Boolean | 全局配置 | 是否显示缩略图（自定义预览） |
| closeOnClick | Boolean | 全局配置 | 点击图片是否关闭（自定义预览） |

## 事件

| 事件名 | 参数 | 说明 |
|--------|------|------|
| update:visible | Boolean | 显示状态变化 |
| close | - | 关闭预览 |
| change | { index, image } | 切换图片 |
| save | { index, url } | 保存图片 |

## 方法

| 方法名 | 参数 | 说明 |
|--------|------|------|
| goToImage | index | 跳转到指定图片 |
| close | - | 关闭预览 |
| save | - | 保存当前图片 |

## 推荐使用规则

### 使用原生预览的场景：
- ✅ 单张图片预览
- ✅ 头像预览
- ✅ 简单的图片查看
- ✅ 对性能要求极高的场景

### 使用自定义预览的场景：
- ✅ 多张图片预览
- ✅ 需要缩略图导航
- ✅ 需要显示图片信息
- ✅ 需要自定义UI样式
- ✅ 相册、画廊类应用

## 全局配置

可以在 `uni3/src/config/imageViewer.js` 中修改全局默认配置：

```javascript
export const imageViewerConfig = {
  useNativePreview: false, // 全局默认使用自定义预览
  customPreview: {
    showInfo: true,
    showThumbnail: true,
    closeOnClick: true
  }
}
```