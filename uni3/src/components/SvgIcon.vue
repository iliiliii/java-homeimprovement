<template>
  <view 
    class="svg-icon"
    :style="wrapperStyle"
  >
    <image 
      :src="iconSrc" 
      :style="imageStyle"
      mode="aspectFit"
      class="icon-image"
    />
  </view>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  name: {
    type: String,
    required: true
  },
  size: {
    type: [String, Number],
    default: 24
  },
  color: {
    type: String,
    default: '#000000'
  }
})

// 图标名称映射
const iconMap = {
  'home': 'home',
  'photo': 'photo',
  'calendar': 'calendar',
  'file-text': 'file-text',
  'bag': 'shopping-bag',
  'shopping-bag': 'shopping-bag',
  'rmb-circle': 'currency-yuan',
  'currency-yuan': 'currency-yuan',
  'account': 'user',
  'user': 'user',
  'edit-pen': 'edit',
  'edit': 'edit',
  'checkmark': 'check',
  'check': 'check',
  'star': 'star',
  'chat': 'message-circle',
  'message-circle': 'message-circle',
  'kefu-ermai': 'message-circle',
  'info-circle': 'info-circle',
  'arrow-right': 'chevron-right',
  'chevron-right': 'chevron-right',
  'checkmark-circle': 'circle-check',
  'circle-check': 'circle-check',
  'weixin-fill': 'brand-wechat',
  'brand-wechat': 'brand-wechat',
  'download': 'download',
  'close': 'close',
  'x': 'close',
  'chevron-left': 'chevron-left',
  'arrow-left': 'chevron-left',
  'back': 'chevron-left'
}

// 生成带颜色的 SVG Data URL
const iconSrc = computed(() => {
  const iconName = iconMap[props.name] || props.name
  const color = encodeURIComponent(props.color)
  
  // SVG 模板映射
  const svgTemplates = {
    'home': `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><g fill="none" stroke="${color}" stroke-linecap="round" stroke-linejoin="round" stroke-width="2"><path d="M5 12H3l9-9l9 9h-2M5 12v7a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2v-7"/><path d="M9 21v-6a2 2 0 0 1 2-2h2a2 2 0 0 1 2 2v6"/></g></svg>`,
    
    'photo': `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><g fill="none" stroke="${color}" stroke-linecap="round" stroke-linejoin="round" stroke-width="2"><path d="M15 8h.01M3 6a3 3 0 0 1 3-3h12a3 3 0 0 1 3 3v12a3 3 0 0 1-3 3H6a3 3 0 0 1-3-3z"/><path d="m3 16l5-5c.928-.893 2.072-.893 3 0l5 5"/><path d="m14 14l1-1c.928-.893 2.072-.893 3 0l3 3"/></g></svg>`,
    
    'calendar': `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><path fill="none" stroke="${color}" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 7a2 2 0 0 1 2-2h12a2 2 0 0 1 2 2v12a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2zm12-4v4M8 3v4m-4 4h16m-9 4h1m0 0v3"/></svg>`,
    
    'file-text': `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><g fill="none" stroke="${color}" stroke-linecap="round" stroke-linejoin="round" stroke-width="2"><path d="M14 3v4a1 1 0 0 0 1 1h4"/><path d="M17 21H7a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h7l5 5v11a2 2 0 0 1-2 2M9 9h1m-1 4h6m-6 4h6"/></g></svg>`,
    
    'shopping-bag': `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><g fill="none" stroke="${color}" stroke-linecap="round" stroke-linejoin="round" stroke-width="2"><path d="M6.331 8H17.67a2 2 0 0 1 1.977 2.304l-1.255 8.152A3 3 0 0 1 15.426 21H8.574a3 3 0 0 1-2.965-2.544l-1.255-8.152A2 2 0 0 1 6.331 8"/><path d="M9 11V6a3 3 0 0 1 6 0v5"/></g></svg>`,
    
    'currency-yuan': `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><path fill="none" stroke="${color}" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 19v-7L7 5m10 0l-5 7m-4 1h8"/></svg>`,
    
    'user': `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><path fill="none" stroke="${color}" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7a4 4 0 1 0 8 0a4 4 0 0 0-8 0M6 21v-2a4 4 0 0 1 4-4h4a4 4 0 0 1 4 4v2"/></svg>`,
    
    'edit': `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><g fill="none" stroke="${color}" stroke-linecap="round" stroke-linejoin="round" stroke-width="2"><path d="M7 7H6a2 2 0 0 0-2 2v9a2 2 0 0 0 2 2h9a2 2 0 0 0 2-2v-1"/><path d="M20.385 6.585a2.1 2.1 0 0 0-2.97-2.97L9 12v3h3zM16 5l3 3"/></g></svg>`,
    
    'check': `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><path fill="none" stroke="${color}" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="m5 12l5 5L20 7"/></svg>`,
    
    'star': `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><path fill="none" stroke="${color}" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="m12 17.75l-6.172 3.245l1.179-6.873l-5-4.867l6.9-1l3.086-6.253l3.086 6.253l6.9 1l-5 4.867l1.179 6.873z"/></svg>`,
    
    'message-circle': `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><path fill="none" stroke="${color}" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="m3 20l1.3-3.9C1.976 12.663 2.874 8.228 6.4 5.726c3.526-2.501 8.59-2.296 11.845.48c3.255 2.777 3.695 7.266 1.029 10.501S11.659 20.922 7.7 19z"/></svg>`,
    
    'info-circle': `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><g fill="none" stroke="${color}" stroke-linecap="round" stroke-linejoin="round" stroke-width="2"><path d="M3 12a9 9 0 1 0 18 0a9 9 0 0 0-18 0m9-3h.01"/><path d="M11 12h1v4h1"/></g></svg>`,
    
    'chevron-right': `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><path fill="none" stroke="${color}" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="m9 6l6 6l-6 6"/></svg>`,
    
    'circle-check': `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><g fill="none" stroke="${color}" stroke-linecap="round" stroke-linejoin="round" stroke-width="2"><path d="M3 12a9 9 0 1 0 18 0a9 9 0 1 0-18 0"/><path d="m9 12l2 2l4-4"/></g></svg>`,
    
    'brand-wechat': `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><g fill="none" stroke="${color}" stroke-linecap="round" stroke-linejoin="round" stroke-width="2"><path d="M16.5 10c3.038 0 5.5 2.015 5.5 4.5c0 1.397-.778 2.645-2 3.47V20l-1.964-1.178A6.7 6.7 0 0 1 16.5 19c-3.038 0-5.5-2.015-5.5-4.5s2.462-4.5 5.5-4.5"/><path d="M11.197 15.698c-.69.196-1.43.302-2.197.302a8 8 0 0 1-2.612-.432L4 17v-2.801C2.763 13.117 2 11.635 2 10c0-3.314 3.134-6 7-6c3.782 0 6.863 2.57 7 5.785v.233M10 8h.01M7 8h.01M15 14h.01M18 14h.01"/></g></svg>`,
    
    'download': `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><path fill="none" stroke="${color}" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 17v2a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2v-2M7 11l5 5l5-5m-5-7v12"/></svg>`,
    
    'close': `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><path fill="none" stroke="${color}" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M18 6L6 18M6 6l12 12"/></svg>`,
    
    'chevron-left': `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><path fill="none" stroke="${color}" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="m15 6l-6 6l6 6"/></svg>`
  }
  
  const svgString = svgTemplates[iconName] || svgTemplates['home']
  
  // 转换为 Data URL
  return 'data:image/svg+xml;charset=utf-8,' + svgString
})

const wrapperStyle = computed(() => {
  const sizeValue = typeof props.size === 'number' ? `${props.size}rpx` : props.size
  return {
    width: sizeValue,
    height: sizeValue,
    display: 'inline-flex',
    alignItems: 'center',
    justifyContent: 'center',
    flexShrink: 0
  }
})

const imageStyle = computed(() => {
  return {
    width: '100%',
    height: '100%'
  }
})
</script>

<style lang="scss" scoped>
.svg-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.icon-image {
  display: block;
  width: 100%;
  height: 100%;
}
</style>
