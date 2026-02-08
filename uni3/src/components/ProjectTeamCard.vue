<template>
  <view  v-if="members.length > 0" class="team-card">
    <!-- 卡片头部 -->
    <view class="card-header">
      <view class="header-icon-wrapper">
        <SvgIcon name="users" size="28rpx" color="#C40016" />
      </view>
      <text class="header-title">项目服务团队</text>
    </view>
    
    <!-- 加载状态 -->
    <view v-if="loading" class="team-loading">
      <view class="loading-spinner"></view>
    </view>
    
    <!-- 成员列表 -->
    <view v-else-if="members.length > 0" class="team-members">
      <view 
        class="member-card" 
        v-for="member in members" 
        :key="member.id"
      >
        <view class="member-avatar">
          <UserAvatar 
            :avatar="member.avatar" 
            :name="member.name" 
            size="112rpx"
          />
        </view>
        <view class="member-info">
          <text class="member-name">{{ member.name }}</text>
          <text class="member-post">{{ member.post }}</text>
        </view>
      </view>
    </view>
    
    <!-- 空状态 -->
    <view v-else class="team-empty">
      <view class="empty-icon">👥</view>
      <text class="empty-text">暂无团队成员</text>
    </view>
  </view>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import UserAvatar from '@/components/UserAvatar.vue'
import SvgIcon from '@/components/SvgIcon.vue'
import { getProjectMembers } from '@/api/dashboard.js'

const props = defineProps({
  projectId: {
    type: String,
    required: true
  }
})

// 暴露刷新方法给父组件
defineExpose({
  refresh: loadMembers
})

const members = ref([])
const loading = ref(false)

// 加载团队成员
async function loadMembers() {
  if (!props.projectId) {
    members.value = []
    return
  }
  
  loading.value = true
  try {
    const data = await getProjectMembers(props.projectId, { loading: false })
    members.value = data || []
  } catch (error) {
    console.error('[ProjectTeamCard] 加载团队成员失败:', error)
    members.value = []
  } finally {
    loading.value = false
  }
}

// 监听项目ID变化
watch(() => props.projectId, (newId) => {
  if (newId) {
    loadMembers()
  } else {
    members.value = []
  }
}, { immediate: true })

onMounted(() => {
  loadMembers()
})
</script>

<style lang="scss" scoped>
.team-card {
  margin: 0 $spacing-xl;
  padding: $spacing-l;
  background: $color-white;
  border-radius: $radius-2xl;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);
  border: 1rpx solid $color-border-light;
}

// 卡片头部
.card-header {
  display: flex;
  align-items: center;
  gap: $spacing-s;
  margin-bottom: $spacing-l;
  padding-bottom: $spacing-m;
  border-bottom: 1rpx solid $color-border-light;
}

.header-icon-wrapper {
  width: 48rpx;
  height: 48rpx;
  border-radius: $radius-l;
  background: linear-gradient(135deg, rgba(196, 0, 22, 0.08) 0%, rgba(196, 0, 22, 0.04) 100%);
  display: flex;
  align-items: center;
  justify-content: center;
}

.header-title {
  font-size: 30rpx;
  font-weight: 600;
  color: $color-text-primary;
  letter-spacing: 0.5rpx;
}

// 加载状态
.team-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 80rpx 0;
  
  .loading-spinner {
    width: 48rpx;
    height: 48rpx;
    border: 3rpx solid $color-border-light;
    border-top: 3rpx solid $color-brand;
    border-radius: 50%;
    animation: spin 0.8s linear infinite;
  }
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

// 成员列表 - 横向布局，更紧凑
.team-members {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: $spacing-m;
}

.member-card {
  display: flex;
  align-items: center;
  gap: $spacing-m;
  padding: $spacing-m;
  background: linear-gradient(135deg, $color-gray-50 0%, $color-white 100%);
  border-radius: $radius-xl;
  border: 1rpx solid $color-border-light;
  transition: all 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  position: relative;
  overflow: hidden;
  
  // 装饰元素
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 3rpx;
    background: linear-gradient(90deg, $color-brand 0%, transparent 100%);
    opacity: 0;
    transition: opacity 0.3s ease;
  }
  
  &:active {
    transform: scale(0.96);
    background: $color-gray-100;
    
    &::before {
      opacity: 1;
    }
  }
}

.member-avatar {
  flex-shrink: 0;
  
  :deep(.user-avatar) {
    border: 3rpx solid $color-white;
    box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.08);
  }
}

.member-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 6rpx;
}

.member-name {
  font-size: 28rpx;
  font-weight: 600;
  color: $color-text-primary;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  line-height: 1.3;
}

.member-post {
  font-size: 22rpx;
  color: $color-text-tertiary;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  line-height: 1.4;
}

// 空状态
.team-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80rpx 0;
  gap: $spacing-m;
}

.empty-icon {
  font-size: 80rpx;
  opacity: 0.3;
  line-height: 1;
}

.empty-text {
  font-size: 26rpx;
  color: $color-text-quaternary;
}

// 响应式优化
@media (max-width: 750rpx) {
  .team-card {
    margin: 0 $spacing-m $spacing-m;
    padding: $spacing-m;
  }
  
  .card-header {
    margin-bottom: $spacing-m;
  }
  
  .header-title {
    font-size: 28rpx;
  }
  
  .member-card {
    padding: $spacing-s $spacing-m;
    gap: $spacing-s;
  }
  
  .member-name {
    font-size: 26rpx;
  }
  
  .member-post {
    font-size: 20rpx;
  }
}
</style>
