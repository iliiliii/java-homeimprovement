<template>
  <view class="budget-page">
    <!-- 统一导航栏 -->
    <NavBar title="预算详情" />
    
    <!-- 导航栏占位 -->
    <view :style="{ height: navHeight + 'px' }"></view>
    
    <!-- 总预算卡片 -->
    <view class="budget-summary">
      <text class="budget-label">总预算 (元)</text>
      <text class="budget-total">{{ formatNumber(totalBudget) }}</text>
      
      <!-- 预算分布条 -->
      <view class="budget-bar">
        <view 
          v-for="(item, index) in categories" 
          :key="index"
          class="budget-bar-item"
          :style="{ 
            width: (item.amount / totalBudget * 100) + '%',
            background: item.color 
          }"
        ></view>
      </view>
      
      <view class="budget-footer flex-between">
        <text class="budget-used">已用: {{ formatNumber(usedBudget) }}</text>
        <text class="budget-remaining">剩余: {{ formatNumber(totalBudget - usedBudget) }}</text>
      </view>
    </view>
    
    <!-- 费用明细 -->
    <view class="category-section">
      <text class="section-title">费用明细</text>
      
      <view 
        class="category-row"
        v-for="(item, index) in categories"
        :key="index"
        @click="viewCategory(item)"
      >
        <view class="cat-icon" :style="{ color: item.color }">
          <SvgIcon :name="item.icon" size="40rpx" :color="item.color" />
        </view>
        <view class="cat-content">
          <view class="flex-between">
            <text class="cat-name">{{ item.name }}</text>
            <text class="cat-amount">¥ {{ formatNumber(item.amount) }}</text>
          </view>
          <view class="progress-bar">
            <view 
              class="progress-fill" 
              :style="{ 
                width: item.progress + '%',
                background: item.color 
              }"
            ></view>
          </view>
          <view class="flex-between cat-detail">
            <text class="cat-spent">已支付 ¥{{ formatNumber(item.spent) }}</text>
            <text class="cat-progress">{{ item.progress }}%</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import NavBar from '@/components/NavBar.vue'
import SvgIcon from '@/components/SvgIcon.vue'
import { getTotalNavHeight } from '@/utils/system.js'

const navHeight = ref(0)
const totalBudget = ref(500000)

const categories = ref([
  {
    name: '设计费',
    icon: 'star',
    amount: 20000,
    spent: 20000,
    progress: 100,
    color: '#FF6B6B'
  },
  {
    name: '硬装施工',
    icon: 'home',
    amount: 180000,
    spent: 108000,
    progress: 60,
    color: '#4ECDC4'
  },
  {
    name: '全屋定制',
    icon: 'grid',
    amount: 150000,
    spent: 45000,
    progress: 30,
    color: '#45B7D1'
  },
  {
    name: '软装家电',
    icon: 'bag',
    amount: 150000,
    spent: 15000,
    progress: 10,
    color: '#96CEB4'
  }
])

const usedBudget = computed(() => {
  return categories.value.reduce((sum, item) => sum + item.spent, 0)
})

onMounted(() => {
  navHeight.value = getTotalNavHeight()
})

const formatNumber = (num) => {
  return num.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ',')
}

const viewCategory = (item) => {
  uni.showToast({
    title: `查看${item.name}详情`,
    icon: 'none'
  })
}
</script>

<style lang="scss" scoped>
.budget-page {
  min-height: 100vh;
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
}

// 总预算卡片
.budget-summary {
  margin: 32rpx 48rpx 48rpx;
  background: white;
  border-radius: 40rpx;
  padding: 48rpx;
  box-shadow: $shadow-card;
  text-align: center;
}

.budget-label {
  display: block;
  font-size: 28rpx;
  color: $glass-text-muted;
  margin-bottom: 16rpx;
}

.budget-total {
  display: block;
  font-size: 72rpx;
  font-weight: 700;
  color: $glass-text-main;
  margin-bottom: 48rpx;
}

.budget-bar {
  display: flex;
  height: 24rpx;
  border-radius: 12rpx;
  overflow: hidden;
  gap: 4rpx;
  margin-bottom: 24rpx;
}

.budget-bar-item {
  height: 100%;
  border-radius: 12rpx;
}

.budget-footer {
  font-size: 24rpx;
  color: $glass-text-muted;
}

// 费用明细
.category-section {
  padding: 0 48rpx;
}

.section-title {
  display: block;
  font-size: 36rpx;
  font-weight: 600;
  color: $glass-text-main;
  margin-bottom: 32rpx;
}

.category-row {
  display: flex;
  align-items: flex-start;
  padding: 32rpx;
  background: white;
  border-radius: 32rpx;
  margin-bottom: 24rpx;
  box-shadow: $shadow-card;
}

.cat-icon {
  width: 80rpx;
  height: 80rpx;
  background: $glass-bg;
  border-radius: 20rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 32rpx;
  flex-shrink: 0;
}

.cat-content {
  flex: 1;
}

.cat-name {
  font-weight: 600;
  font-size: 28rpx;
  color: $glass-text-main;
}

.cat-amount {
  font-weight: 600;
  font-size: 28rpx;
  color: $glass-text-main;
}

.progress-bar {
  height: 12rpx;
  background: $glass-bg;
  border-radius: 6rpx;
  margin: 16rpx 0;
  overflow: hidden;
  
  .progress-fill {
    height: 100%;
    border-radius: 6rpx;
    transition: width 0.3s ease;
  }
}

.cat-detail {
  font-size: 24rpx;
}

.cat-spent {
  color: $glass-text-muted;
}

.cat-progress {
  color: $glass-text-muted;
}
</style>
