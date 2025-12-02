<template>
  <view class="shopping-page">
    <!-- 头部 -->
    <view class="header">
      <view class="back-btn" @click="goBack">
        <u-icon name="arrow-left" size="48" />
      </view>
      <text class="page-title">物料清单</text>
      <view style="width: 48rpx;"></view>
    </view>
    
    <!-- 分类标签 -->
    <view class="category-tabs">
      <scroll-view scroll-x class="tabs-scroll">
        <view class="tabs-container">
          <view 
            class="tab-item"
            :class="{ active: currentCategory === cat.key }"
            v-for="cat in categories"
            :key="cat.key"
            @click="switchCategory(cat.key)"
          >
            {{ cat.name }}
          </view>
        </view>
      </scroll-view>
    </view>
    
    <!-- 产品列表 -->
    <view class="product-list">
      <view 
        class="product-card"
        v-for="product in filteredProducts"
        :key="product.id"
      >
        <view class="checkbox-wrapper" @click="toggleSelect(product)">
          <view class="checkbox" :class="{ checked: product.selected }">
            <u-icon v-if="product.selected" name="checkmark" size="28" color="#fff" />
          </view>
        </view>
        <image class="product-img" :src="product.image" mode="aspectFill" />
        <view class="product-info">
          <view class="flex-between product-header">
            <text class="product-name">{{ product.name }}</text>
            <view class="product-status" :class="product.status">
              {{ getStatusText(product.status) }}
            </view>
          </view>
          <text class="product-spec">{{ product.spec }}</text>
          <view class="flex-between product-footer">
            <text class="product-price">¥ {{ formatNumber(product.price) }}</text>
            <text class="product-deadline">{{ product.deadline }}</text>
          </view>
        </view>
      </view>
    </view>
    
    <!-- 空状态 -->
    <view v-if="filteredProducts.length === 0" class="empty-state">
      <u-icon name="bag" size="100" color="#ccc" />
      <text class="empty-text">暂无物料</text>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'

const currentCategory = ref('all')

const categories = ref([
  { key: 'all', name: '全部' },
  { key: 'main', name: '主材' },
  { key: 'furniture', name: '家具' },
  { key: 'appliance', name: '家电' },
  { key: 'soft', name: '软装' }
])

const products = ref([
  {
    id: 1,
    name: '科勒 智能马桶',
    spec: 'K-77795T-0',
    category: 'main',
    price: 4599,
    status: 'pending',
    deadline: '需 11.30 前到位',
    selected: true,
    image: 'https://images.unsplash.com/photo-1584622650111-993a426fbf0a?ixlib=rb-4.0.3&auto=format&fit=crop&w=200&q=80'
  },
  {
    id: 2,
    name: '大自然 实木地板',
    spec: '橡木原色 910*125',
    category: 'main',
    price: 12000,
    status: 'purchased',
    deadline: '已入库',
    selected: false,
    image: 'https://images.unsplash.com/photo-1556228453-efd6c1ff04f6?ixlib=rb-4.0.3&auto=format&fit=crop&w=200&q=80'
  },
  {
    id: 3,
    name: '方太 油烟机套装',
    spec: 'JQ08TS + JZT-TH33B',
    category: 'appliance',
    price: 8999,
    status: 'purchased',
    deadline: '待安装',
    selected: false,
    image: 'https://images.unsplash.com/photo-1556909114-f6e7ad7d3136?ixlib=rb-4.0.3&auto=format&fit=crop&w=200&q=80'
  },
  {
    id: 4,
    name: '索菲亚 衣柜',
    spec: '主卧 3.2m',
    category: 'furniture',
    price: 15800,
    status: 'pending',
    deadline: '需 12.15 前下单',
    selected: false,
    image: 'https://images.unsplash.com/photo-1595428774223-ef52624120d2?ixlib=rb-4.0.3&auto=format&fit=crop&w=200&q=80'
  }
])

const filteredProducts = computed(() => {
  if (currentCategory.value === 'all') {
    return products.value
  }
  return products.value.filter(p => p.category === currentCategory.value)
})

const switchCategory = (key) => {
  currentCategory.value = key
}

const getStatusText = (status) => {
  const map = {
    pending: '待采购',
    purchased: '已采购',
    installed: '已安装'
  }
  return map[status] || ''
}

const toggleSelect = (product) => {
  product.selected = !product.selected
}

const formatNumber = (num) => {
  return num.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ',')
}

const goBack = () => {
  uni.navigateBack()
}
</script>

<style lang="scss" scoped>
.shopping-page {
  min-height: 100vh;
}

// 头部
.header {
  padding: 100rpx 48rpx 32rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.back-btn {
  width: 48rpx;
  display: flex;
  align-items: center;
}

// 分类标签
.category-tabs {
  padding: 0 48rpx;
  margin-bottom: 32rpx;
}

.tabs-scroll {
  white-space: nowrap;
}

.tabs-container {
  display: inline-flex;
  gap: 24rpx;
}

.tab-item {
  padding: 16rpx 32rpx;
  border-radius: 100rpx;
  background: white;
  color: $glass-text-muted;
  font-size: 28rpx;
  flex-shrink: 0;
  
  &.active {
    background: $glass-accent;
    color: white;
  }
}

// 产品列表
.product-list {
  padding: 0 48rpx;
}

.product-card {
  background: white;
  border-radius: 40rpx;
  padding: 32rpx;
  margin-bottom: 32rpx;
  display: flex;
  gap: 24rpx;
  box-shadow: $shadow-card;
}

.checkbox-wrapper {
  display: flex;
  align-items: center;
}

.checkbox {
  width: 48rpx;
  height: 48rpx;
  border: 4rpx solid $glass-text-muted;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  
  &.checked {
    background: $glass-accent;
    border-color: $glass-accent;
  }
}

.product-img {
  width: 160rpx;
  height: 160rpx;
  border-radius: 24rpx;
  background: #eee;
  flex-shrink: 0;
}

.product-info {
  flex: 1;
  min-width: 0;
}

.product-header {
  margin-bottom: 8rpx;
}

.product-name {
  font-weight: 600;
  font-size: 28rpx;
  color: $glass-text-main;
}

.product-status {
  font-size: 24rpx;
  
  &.pending {
    color: $glass-warning;
  }
  
  &.purchased {
    color: $glass-success;
  }
  
  &.installed {
    color: $glass-accent;
  }
}

.product-spec {
  display: block;
  font-size: 24rpx;
  color: $glass-text-muted;
  margin-bottom: 16rpx;
}

.product-footer {
  margin-top: auto;
}

.product-price {
  font-weight: 600;
  color: $glass-accent;
  font-size: 28rpx;
}

.product-deadline {
  font-size: 24rpx;
  color: $glass-text-muted;
}

// 空状态
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 100rpx;
}

.empty-text {
  margin-top: 24rpx;
  font-size: 28rpx;
  color: $glass-text-muted;
}
</style>

