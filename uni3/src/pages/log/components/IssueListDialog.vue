<template>
  <view v-if="visible" class="issue-list-mask" @tap="handleMaskClick">
    <view class="issue-list-dialog" @tap.stop>
      <!-- 头部 -->
      <view class="dialog-header">
        <view class="back-btn" @tap="handleClose">
          <text class="back-icon">‹</text>
        </view>
        <text class="dialog-title">问题列表</text>
        <view class="placeholder"></view>
      </view>
      
      <!-- 问题列表 -->
      <scroll-view scroll-y class="dialog-body" v-if="!showFixForm && !showFixHistory">
        <view v-if="loading" class="loading-container">
          <text>加载中...</text>
        </view>
        <view v-else-if="issues.length === 0" class="empty-container">
          <text>暂无问题记录</text>
        </view>
        <view v-else class="issue-list">
          <view 
            v-for="issue in issues" 
            :key="issue.id"
            class="issue-item"
          >
            <view class="issue-header">
              <text class="issue-title">{{ issue.title }}</text>
              <view class="issue-status" :class="getStatusClass(issue.status)">
                {{ issue.statusText }}
              </view>
            </view>
            <view class="issue-info">
              <text class="issue-category">{{ issue.categoryText }}</text>
              <text class="issue-date">{{ formatDate(issue.createdAt) }}</text>
            </view>
            <view class="issue-desc">{{ issue.description }}</view>
            <view class="issue-actions">
              <view class="action-btn history-btn" @tap="viewFixHistory(issue)">
                <text>整改记录</text>
              </view>
              <view 
                v-if="issue.status !== 'RESOLVED' && issue.status !== 'CLOSED'"
                class="action-btn fix-btn" 
                @tap="submitFix(issue)"
              >
                <text>提交整改</text>
              </view>
            </view>
          </view>
          <!-- 底部安全区域占位 -->
          <view class="bottom-safe-area"></view>
        </view>
      </scroll-view>

      <!-- 整改记录列表 -->
      <view v-if="showFixHistory" class="fix-history-container">
        <view class="sub-header">
          <view class="back-btn" @tap="backToList">
            <text class="back-icon">‹</text>
          </view>
          <text class="sub-title">{{ currentIssue?.title }} - 整改记录</text>
        </view>
        <scroll-view scroll-y class="fix-list">
          <view v-if="fixLoading" class="loading-container">
            <text>加载中...</text>
          </view>
          <view v-else-if="fixes.length === 0" class="empty-container">
            <text>暂无整改记录</text>
          </view>
          <view v-else class="fix-list-content">
            <view v-for="fix in fixes" :key="fix.id" class="fix-item">
              <view class="fix-header">
                <view class="fix-status" :class="getStatusClass(fix.status)">
                  {{ fix.statusText }}
                </view>
                <text class="fix-date">{{ formatDate(fix.createdAt) }}</text>
              </view>
              <view class="fix-desc">{{ fix.fixDescription }}</view>
              <view v-if="fix.images && fix.images.length > 0" class="fix-images">
                <image 
                  v-for="(img, idx) in fix.images" 
                  :key="idx"
                  :src="getImageUrl(img)"
                  mode="aspectFill"
                  class="fix-image"
                  @tap="previewImage(fix.images, idx)"
                />
              </view>
              <view class="fix-footer">
                <text class="fix-by">{{ fix.createdBy }}</text>
              </view>
            </view>
          </view>
        </scroll-view>
        <view class="fix-history-footer" v-if="currentIssue?.status !== 'RESOLVED' && currentIssue?.status !== 'CLOSED'">
          <view class="btn btn-primary" @tap="submitFix(currentIssue)">提交整改</view>
        </view>
      </view>

      <!-- 整改提交表单 -->
      <view v-if="showFixForm" class="fix-form-container">
        <view class="sub-header">
          <view class="back-btn" @tap="backFromFixForm">
            <text class="back-icon">‹</text>
          </view>
          <text class="sub-title">提交整改 - {{ currentIssue?.title }}</text>
        </view>
        <scroll-view scroll-y class="fix-form">
          <!-- 整改描述 -->
          <view class="form-item">
            <view class="form-label">
              <text class="required">*</text>
              <text>整改描述</text>
            </view>
            <textarea 
              class="form-textarea"
              v-model="fixForm.fixDescription"
              placeholder="请详细描述整改措施和方法"
              :maxlength="500"
            />
            <view class="word-count">{{ fixForm.fixDescription.length }}/500</view>
          </view>

          <!-- 整改状态 -->
          <view class="form-item">
            <view class="form-label">
              <text class="required">*</text>
              <text>整改状态</text>
            </view>
            <view class="radio-group">
              <view 
                class="radio-item"
                :class="{ active: fixForm.status === 'IN_PROGRESS' }"
                @tap="fixForm.status = 'IN_PROGRESS'"
              >
                <view class="radio-dot"></view>
                <text>解决中</text>
              </view>
              <view 
                class="radio-item"
                :class="{ active: fixForm.status === 'RESOLVED' }"
                @tap="fixForm.status = 'RESOLVED'"
              >
                <view class="radio-dot"></view>
                <text>已解决</text>
              </view>
            </view>
          </view>

          <!-- 整改照片 -->
          <view class="form-item">
            <view class="form-label">
              <text>整改照片</text>
              <text class="label-hint">（最多9张）</text>
            </view>
            <view class="image-upload-area">
              <view 
                v-for="(img, index) in fixForm.images" 
                :key="index"
                class="image-item"
              >
                <image :src="getImageUrl(img)" mode="aspectFill" class="uploaded-image" />
                <view class="image-delete" @tap="removeFixImage(index)">
                  <text>×</text>
                </view>
              </view>
              <view 
                v-if="fixForm.images.length < 9"
                class="image-add"
                @tap="chooseFixImage"
              >
                <text class="add-icon">+</text>
              </view>
            </view>
          </view>
          
          <!-- 底部占位 -->
          <view class="form-bottom-space"></view>
        </scroll-view>
        <view class="fix-form-footer">
          <view class="btn btn-cancel" @tap="backFromFixForm">取消</view>
          <view class="btn btn-primary" :class="{ disabled: fixSubmitting }" @tap="handleSubmitFix">
            {{ fixSubmitting ? '提交中...' : '提交整改' }}
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useUserStore } from '@/store/user.js'
import { getQualityIssueList, getFixesByIssueId, submitFix as submitFixApi } from '@/api/qualityIssue'
import { BASE_URL } from '@/utils/request'
import { compressImage, formatFileSize } from '@/utils/imageCompress'

const props = defineProps({
  visible: { type: Boolean, default: false }
})

const emit = defineEmits(['update:visible', 'refresh'])

const userStore = useUserStore()
const loading = ref(false)
const fixLoading = ref(false)
const fixSubmitting = ref(false)
const issues = ref([])
const fixes = ref([])
const currentIssue = ref(null)
const showFixHistory = ref(false)
const showFixForm = ref(false)
const uploadingCount = ref(0)

// 整改表单
const fixForm = ref({
  fixDescription: '',
  images: [],
  status: 'IN_PROGRESS'
})

watch(() => props.visible, (val) => {
  if (val) {
    loadIssues()
    showFixHistory.value = false
    showFixForm.value = false
  }
})

const loadIssues = async () => {
  loading.value = true
  try {
    const res = await getQualityIssueList({ page: 1, pageSize: 100 })
    issues.value = res.list || []
  } catch (error) {
    console.error('加载问题列表失败:', error)
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

const viewFixHistory = async (issue) => {
  currentIssue.value = issue
  showFixHistory.value = true
  fixLoading.value = true
  try {
    const res = await getFixesByIssueId(issue.id)
    fixes.value = res || []
  } catch (error) {
    console.error('加载整改记录失败:', error)
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    fixLoading.value = false
  }
}

const submitFix = (issue) => {
  currentIssue.value = issue
  showFixForm.value = true
  showFixHistory.value = false
  fixForm.value = {
    fixDescription: '',
    images: [],
    status: 'IN_PROGRESS'
  }
}

const backToList = () => {
  showFixHistory.value = false
  currentIssue.value = null
  fixes.value = []
}

const backFromFixForm = () => {
  if (showFixHistory.value) {
    showFixForm.value = false
  } else {
    showFixForm.value = false
    currentIssue.value = null
  }
}

const chooseFixImage = () => {
  uni.chooseImage({
    count: 9 - fixForm.value.images.length,
    sizeType: ['original'],
    sourceType: ['album', 'camera'],
    success: (res) => res.tempFilePaths.forEach(path => compressAndUploadFix(path))
  })
}

const compressAndUploadFix = async (filePath) => {
  uploadingCount.value++
  uni.showLoading({ title: '压缩中...', mask: true })
  
  try {
    const compressResult = await compressImage(filePath, {
      quality: null,
      maxWidth: 1920,
      maxHeight: 1920,
      enableSmartCompression: true
    })
    
    if (compressResult.success) {
      const compressedPath = compressResult.tempFilePath
      if (!compressResult.skipped && compressResult.compressionRatio > 0) {
        console.log(`图片压缩: ${formatFileSize(compressResult.originalSize)} -> ${formatFileSize(compressResult.compressedSize)}`)
      }
      uni.showLoading({ title: '上传中...', mask: true })
      uploadFixImage(compressedPath)
    } else {
      uni.showLoading({ title: '上传中...', mask: true })
      uploadFixImage(filePath)
    }
  } catch (error) {
    console.error('图片压缩失败:', error)
    uni.showLoading({ title: '上传中...', mask: true })
    uploadFixImage(filePath)
  }
}

const uploadFixImage = (filePath) => {
  uni.uploadFile({
    url: BASE_URL + '/app/upload',
    filePath, name: 'file',
    header: { 'Authorization': `Bearer ${userStore.token}` },
    success: (res) => {
      try {
        const data = JSON.parse(res.data)
        if (data.code === 200) fixForm.value.images.push(data.fileName || data.url)
        else uni.showToast({ title: data.msg || '上传失败', icon: 'none' })
      } catch (e) { uni.showToast({ title: '上传失败', icon: 'none' }) }
    },
    fail: () => uni.showToast({ title: '上传失败', icon: 'none' }),
    complete: () => { uploadingCount.value--; if (uploadingCount.value === 0) uni.hideLoading() }
  })
}

const removeFixImage = (index) => {
  fixForm.value.images.splice(index, 1)
}

const handleSubmitFix = async () => {
  if (fixSubmitting.value) return
  if (uploadingCount.value > 0) {
    uni.showToast({ title: '请等待图片上传完成', icon: 'none' })
    return
  }
  if (!fixForm.value.fixDescription.trim()) {
    uni.showToast({ title: '请输入整改描述', icon: 'none' })
    return
  }

  fixSubmitting.value = true
  try {
    await submitFixApi({
      issueId: currentIssue.value.id,
      fixDescription: fixForm.value.fixDescription,
      images: JSON.stringify(fixForm.value.images),
      status: fixForm.value.status
    })
    uni.showToast({ title: '提交成功', icon: 'success' })
    showFixForm.value = false
    // 刷新问题列表
    loadIssues()
    emit('refresh')
  } catch (error) {
    console.error('提交整改失败:', error)
    uni.showToast({ title: error.message || '提交失败', icon: 'none' })
  } finally {
    fixSubmitting.value = false
  }
}

const handleClose = () => {
  emit('update:visible', false)
}

const handleMaskClick = (e) => {
  if (e.target === e.currentTarget) handleClose()
}

const getStatusClass = (status) => {
  const map = {
    'OPEN': 'status-open',
    'IN_PROGRESS': 'status-progress',
    'RESOLVED': 'status-resolved',
    'CLOSED': 'status-closed'
  }
  return map[status] || ''
}

const formatDate = (date) => {
  if (!date) return ''
  const d = new Date(date)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

const getImageUrl = (img) => {
  if (!img) return ''
  return img.startsWith('http://') || img.startsWith('https://') ? img : BASE_URL + img
}

const previewImage = (images, index) => {
  const urls = images.map(img => getImageUrl(img))
  uni.previewImage({ urls, current: urls[index] })
}
</script>

<style lang="scss" scoped>
.issue-list-mask {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  width: 100%; height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: flex-end;
  justify-content: center;
  z-index: 9999;
  box-sizing: border-box;
}

.issue-list-dialog {
  width: 100%;
  height: 85vh;
  background: #fff;
  border-radius: 24rpx 24rpx 0 0;
  display: flex;
  flex-direction: column;
  box-sizing: border-box;
  overflow: hidden;
}

.dialog-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 100rpx;
  min-height: 100rpx;
  padding: 0 32rpx;
  border-bottom: 1rpx solid #eee;
  flex-shrink: 0;
  box-sizing: border-box;
}

.back-btn {
  width: 60rpx;
  height: 60rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.back-icon {
  font-size: 48rpx;
  color: #333;
}

.dialog-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #333;
  flex-shrink: 0;
}

.placeholder {
  width: 60rpx;
  flex-shrink: 0;
}

.dialog-body {
  flex: 1;
  height: 0;
  box-sizing: border-box;
}

.issue-list {
  padding: 24rpx 32rpx;
  padding-bottom: 0;
}

.bottom-safe-area {
  height: calc(40rpx + constant(safe-area-inset-bottom));
  height: calc(40rpx + env(safe-area-inset-bottom));
}

.loading-container, .empty-container {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 300rpx;
  color: #999;
}

.issue-item {
  background: #f8f8f8;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 24rpx;
}

.issue-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12rpx;
}

.issue-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #333;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.issue-status {
  font-size: 22rpx;
  padding: 4rpx 12rpx;
  border-radius: 8rpx;
  margin-left: 16rpx;
}

.status-open { background: #fff1f0; color: #ff4d4f; }
.status-progress { background: #fff7e6; color: #fa8c16; }
.status-resolved { background: #f6ffed; color: #52c41a; }
.status-closed { background: #f5f5f5; color: #999; }

.issue-info {
  display: flex;
  gap: 16rpx;
  margin-bottom: 12rpx;
}

.issue-category {
  font-size: 24rpx;
  color: #666;
  background: #e6f7ff;
  padding: 2rpx 12rpx;
  border-radius: 6rpx;
}

.issue-date {
  font-size: 24rpx;
  color: #999;
}

.issue-desc {
  font-size: 26rpx;
  color: #666;
  line-height: 1.5;
  margin-bottom: 16rpx;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.issue-actions {
  display: flex;
  gap: 16rpx;
  justify-content: flex-end;
}

.action-btn {
  padding: 12rpx 24rpx;
  border-radius: 8rpx;
  font-size: 26rpx;
}

.history-btn {
  background: #f0f0f0;
  color: #666;
}

.fix-btn {
  background: #AD9B4B;
  color: #fff;
}

/* 整改记录样式 */
.fix-history-container, .fix-form-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
  overflow: hidden;
}

.sub-header {
  display: flex;
  align-items: center;
  height: 88rpx;
  min-height: 88rpx;
  padding: 0 32rpx;
  border-bottom: 1rpx solid #eee;
  flex-shrink: 0;
  box-sizing: border-box;
}

.sub-title {
  font-size: 28rpx;
  color: #333;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.fix-list {
  flex: 1;
  height: 0;
  box-sizing: border-box;
}

.fix-list-content {
  padding: 24rpx 32rpx;
}

.fix-item {
  background: #f8f8f8;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 16rpx;
}

.fix-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12rpx;
}

.fix-status {
  font-size: 22rpx;
  padding: 4rpx 12rpx;
  border-radius: 8rpx;
}

.fix-date {
  font-size: 24rpx;
  color: #999;
}

.fix-desc {
  font-size: 26rpx;
  color: #333;
  line-height: 1.5;
  margin-bottom: 12rpx;
}

.fix-images {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
  margin-bottom: 12rpx;
}

.fix-image {
  width: 120rpx;
  height: 120rpx;
  border-radius: 8rpx;
}

.fix-footer {
  display: flex;
  justify-content: flex-end;
}

.fix-by {
  font-size: 24rpx;
  color: #999;
}

.fix-history-footer, .fix-form-footer {
  display: flex;
  gap: 20rpx;
  padding: 20rpx 32rpx;
  padding-bottom: calc(20rpx + constant(safe-area-inset-bottom));
  padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
  border-top: 1rpx solid #eee;
  background: #fff;
  flex-shrink: 0;
  box-sizing: border-box;
}

/* 整改表单样式 */
.fix-form {
  flex: 1;
  height: 0;
  padding: 32rpx;
  box-sizing: border-box;
}

.fix-form > view {
  padding: 0 32rpx;
}

.fix-form > view:first-child {
  padding-top: 24rpx;
}

.form-bottom-space {
  height: 20rpx;
}

.form-item {
  margin-bottom: 24rpx;
}

.form-label {
  display: flex;
  align-items: center;
  margin-bottom: 12rpx;
  font-size: 28rpx;
  color: #333;
  
  .required { color: #ff4d4f; margin-right: 4rpx; }
  .label-hint { color: #999; font-size: 24rpx; margin-left: 8rpx; }
}

.form-textarea {
  width: 100%;
  height: 200rpx;
  padding: 16rpx 20rpx;
  background: #f5f5f5;
  border-radius: 12rpx;
  font-size: 28rpx;
  box-sizing: border-box;
  line-height: 1.5;
}

.word-count {
  text-align: right;
  font-size: 22rpx;
  color: #999;
  margin-top: 6rpx;
}

.radio-group {
  display: flex;
  gap: 48rpx;
}

.radio-item {
  display: flex;
  align-items: center;
  gap: 10rpx;
  
  .radio-dot {
    width: 32rpx;
    height: 32rpx;
    border: 2rpx solid #ddd;
    border-radius: 50%;
    position: relative;
    
    &::after {
      content: '';
      position: absolute;
      top: 50%; left: 50%;
      transform: translate(-50%, -50%);
      width: 16rpx; height: 16rpx;
      border-radius: 50%;
      background: transparent;
    }
  }
  
  &.active .radio-dot {
    border-color: #AD9B4B;
    &::after { background: #AD9B4B; }
  }
  
  text { font-size: 28rpx; color: #333; }
}

.image-upload-area {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
}

.image-item {
  position: relative;
  width: 140rpx;
  height: 140rpx;
  flex-shrink: 0;
}

.uploaded-image {
  width: 100%;
  height: 100%;
  border-radius: 8rpx;
}

.image-delete {
  position: absolute;
  top: -8rpx;
  right: -8rpx;
  width: 32rpx;
  height: 32rpx;
  background: rgba(0, 0, 0, 0.6);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  
  text { color: #fff; font-size: 22rpx; }
}

.image-add {
  width: 140rpx;
  height: 140rpx;
  background: #f5f5f5;
  border-radius: 8rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2rpx dashed #ddd;
  flex-shrink: 0;
  
  .add-icon { font-size: 48rpx; color: #999; }
}

.btn {
  flex: 1;
  height: 84rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12rpx;
  font-size: 30rpx;
  font-weight: 500;
}

.btn-cancel { background: #f5f5f5; color: #666; }
.btn-primary {
  background: #AD9B4B;
  color: #fff;
  &.disabled { opacity: 0.6; }
}
</style>
