<template>
  <view v-if="visible" class="acceptance-dialog-mask" :style="{ zIndex: dialogZIndex }" @tap="handleMaskClick">
    <view class="acceptance-dialog" @tap.stop="preventClose">
      <!-- 头部 -->
      <view class="dialog-header">
        <text class="dialog-title">{{ isEdit ? '编辑验收' : '验收上报' }}</text>
        <view class="close-btn" @tap="handleClose">
          <text class="close-icon">×</text>
        </view>
      </view>
      
      <!-- 表单内容 -->
      <scroll-view scroll-y class="dialog-body">
        <!-- 验收标题 -->
        <view class="form-item">
          <view class="form-label">
            <text class="required">*</text>
            <text>验收标题</text>
          </view>
          <input 
            class="form-input"
            v-model="form.title"
            placeholder="请输入验收标题"
            :maxlength="50"
          />
        </view>
        
        <!-- 验收内容 -->
        <view class="form-item">
          <view class="form-label">
            <text class="required">*</text>
            <text>验收内容</text>
          </view>
          <textarea 
            class="form-textarea"
            v-model="form.content"
            placeholder="请描述验收情况（5-200字）"
            :maxlength="200"
            :auto-height="false"
          />
          <view class="word-count">{{ form.content.length }}/200</view>
        </view>
        
        <!-- 现场照片 -->
        <view class="form-item">
          <view class="form-label">
            <text>现场照片</text>
            <text class="label-hint">（最多9张）</text>
          </view>
          <view class="image-upload-area">
            <view 
              v-for="(img, index) in form.images" 
              :key="index"
              class="image-item"
            >
              <image :src="getImageUrl(img)" mode="aspectFill" class="uploaded-image" />
              <view class="image-delete" @tap="removeImage(index)">
                <text>×</text>
              </view>
            </view>
            <view 
              v-if="form.images.length < 9"
              class="image-add"
              @tap="chooseImage"
            >
              <text class="add-icon">+</text>
            </view>
          </view>
        </view>
        
        <!-- 验收结果 -->
        <view class="form-item">
          <view class="form-label">
            <text class="required">*</text>
            <text>验收结果</text>
          </view>
          <view class="radio-group">
            <view 
              class="radio-item"
              :class="{ active: form.result === 'QUALIFIED' }"
              @tap="setResult('QUALIFIED')"
            >
              <view class="radio-dot"></view>
              <text>合格</text>
            </view>
            <view 
              class="radio-item"
              :class="{ active: form.result === 'UNQUALIFIED' }"
              @tap="setResult('UNQUALIFIED')"
            >
              <view class="radio-dot"></view>
              <text>不合格</text>
            </view>
          </view>
        </view>
        
        <!-- 验收时间 -->
        <view class="form-item">
          <view class="form-label">
            <text class="required">*</text>
            <text>验收时间</text>
          </view>
          <view class="form-picker" @tap="showDateSelector = true">
            <text>{{ form.acceptanceDate || '请选择日期' }}</text>
            <text class="picker-arrow">›</text>
          </view>
        </view>
        
        <!-- 验收人（只读） -->
        <view class="form-item">
          <view class="form-label">
            <text class="required">*</text>
            <text>验收人</text>
          </view>
          <view class="form-input readonly">
            <text>{{ form.acceptor }}</text>
          </view>
        </view>
        
        <!-- 底部占位 -->
        <view class="bottom-space"></view>
      </scroll-view>
      
      <!-- 底部按钮 -->
      <view class="dialog-footer">
        <view class="btn btn-cancel" @tap="handleClose">取消</view>
        <view class="btn btn-submit" :class="{ disabled: submitting }" @tap="handleSubmit">
          {{ submitting ? '提交中...' : (isEdit ? '保存修改' : '提交验收') }}
        </view>
      </view>
      
      <!-- 自定义日期选择器 -->
      <view v-if="showDateSelector" class="date-selector-mask" @tap="showDateSelector = false">
        <view class="date-selector" @tap.stop>
          <view class="date-selector-header">
            <text class="date-selector-cancel" @tap="showDateSelector = false">取消</text>
            <text class="date-selector-title">选择日期</text>
            <text class="date-selector-confirm" @tap="confirmDate">确定</text>
          </view>
          <view class="date-picker-columns">
            <picker-view class="picker-column" :value="[yearIndex]" @change="onYearChange">
              <picker-view-column>
                <view 
                  v-for="(year, index) in years" 
                  :key="year" 
                  class="picker-item"
                  :class="{ 'picker-item-selected': index === yearIndex }"
                >{{ year }}年</view>
              </picker-view-column>
            </picker-view>
            <picker-view class="picker-column" :value="[monthIndex]" @change="onMonthChange">
              <picker-view-column>
                <view 
                  v-for="(month, index) in months" 
                  :key="month" 
                  class="picker-item"
                  :class="{ 'picker-item-selected': index === monthIndex }"
                >{{ month }}月</view>
              </picker-view-column>
            </picker-view>
            <picker-view class="picker-column" :value="[dayIndex]" @change="onDayChange">
              <picker-view-column>
                <view 
                  v-for="(day, index) in days" 
                  :key="day" 
                  class="picker-item"
                  :class="{ 'picker-item-selected': index === dayIndex }"
                >{{ day }}日</view>
              </picker-view-column>
            </picker-view>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, watch, computed } from 'vue'
import { useUserStore } from '@/store/user.js'
import { addAcceptanceRecord, updateAcceptanceRecord } from '@/api/projectSchedule'
import { BASE_URL } from '@/utils/request'
import { compressImage, formatFileSize } from '@/utils/imageCompress'

const props = defineProps({
  visible: { type: Boolean, default: false },
  schedule: { type: Object, default: null },
  editRecord: { type: Object, default: null }
})

const emit = defineEmits(['update:visible', 'success'])

const userStore = useUserStore()
const submitting = ref(false)
const showDateSelector = ref(false)
const dialogZIndex = ref(9999)
const uploadingCount = ref(0)

const isEdit = computed(() => !!props.editRecord)

// 日期选择器
const selectedYear = ref(new Date().getFullYear())
const selectedMonth = ref(new Date().getMonth() + 1)
const selectedDay = ref(new Date().getDate())

// 添加防抖定时器
const dateChangeTimer = ref(null)

const years = computed(() => {
  const currentYear = new Date().getFullYear()
  return Array.from({ length: 7 }, (_, i) => currentYear - 5 + i)
})
const months = computed(() => Array.from({ length: 12 }, (_, i) => i + 1))
const days = computed(() => {
  const daysInMonth = new Date(selectedYear.value, selectedMonth.value, 0).getDate()
  return Array.from({ length: daysInMonth }, (_, i) => i + 1)
})

const yearIndex = computed(() => {
  const index = years.value.indexOf(selectedYear.value)
  return index >= 0 ? index : 0
})
const monthIndex = computed(() => {
  const index = selectedMonth.value - 1
  return index >= 0 && index < 12 ? index : 0
})
const dayIndex = computed(() => {
  const index = selectedDay.value - 1
  const maxDays = new Date(selectedYear.value, selectedMonth.value, 0).getDate()
  return index >= 0 && index < maxDays ? index : 0
})

// 防抖处理日期变更
const debounceDateChange = (callback) => {
  if (dateChangeTimer.value) {
    clearTimeout(dateChangeTimer.value)
  }
  dateChangeTimer.value = setTimeout(callback, 150) // 150ms 防抖延迟
}

const onYearChange = (e) => {
  debounceDateChange(() => {
    const newYear = years.value[e.detail.value[0]]
    if (newYear) {
      selectedYear.value = newYear
      // 检查当前选择的天数是否在新年份的当前月份中有效
      const maxDay = new Date(selectedYear.value, selectedMonth.value, 0).getDate()
      if (selectedDay.value > maxDay) {
        selectedDay.value = maxDay
      }
    }
  })
}
const onMonthChange = (e) => {
  debounceDateChange(() => {
    const newMonth = e.detail.value[0] + 1
    if (newMonth >= 1 && newMonth <= 12) {
      selectedMonth.value = newMonth
      // 检查当前选择的天数是否在新月份中有效
      const maxDay = new Date(selectedYear.value, selectedMonth.value, 0).getDate()
      if (selectedDay.value > maxDay) {
        selectedDay.value = maxDay
      }
    }
  })
}
const onDayChange = (e) => { 
  debounceDateChange(() => {
    const newDay = e.detail.value[0] + 1
    const maxDay = new Date(selectedYear.value, selectedMonth.value, 0).getDate()
    if (newDay >= 1 && newDay <= maxDay) {
      selectedDay.value = newDay
    }
  })
}

// 表单数据
const form = ref({
  title: '', content: '', images: [], result: 'QUALIFIED', acceptanceDate: '', acceptor: ''
})

watch(() => props.visible, (val) => {
  if (val) {
    props.editRecord ? initEditForm() : initForm()
  }
})

const initForm = () => {
  const now = new Date()
  const dateStr = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')}`
  const stageName = props.schedule?.stageName || '验收'
  const recordCount = props.schedule?.recordCount || 0
  
  form.value = {
    title: `检查验收-${stageName}-#${recordCount + 1}`,
    content: '', images: [], result: 'QUALIFIED',
    acceptanceDate: dateStr, acceptor: userStore.userInfo?.name || ''
  }
  selectedYear.value = now.getFullYear()
  selectedMonth.value = now.getMonth() + 1
  selectedDay.value = now.getDate()
}

const initEditForm = () => {
  const record = props.editRecord
  let dateStr = ''
  let dateToUse = null
  
  // 优先使用 acceptanceTime，其次使用 createTime
  if (record.acceptanceTime) {
    dateToUse = new Date(record.acceptanceTime)
  } else if (record.createTime) {
    dateToUse = new Date(record.createTime)
  } else {
    dateToUse = new Date()
  }
  
  // 确保日期有效
  if (isNaN(dateToUse.getTime())) {
    dateToUse = new Date()
  }
  
  dateStr = `${dateToUse.getFullYear()}-${String(dateToUse.getMonth() + 1).padStart(2, '0')}-${String(dateToUse.getDate()).padStart(2, '0')}`
  selectedYear.value = dateToUse.getFullYear()
  selectedMonth.value = dateToUse.getMonth() + 1
  selectedDay.value = dateToUse.getDate()
  
  form.value = {
    title: record.title || '',
    content: record.description || '',
    images: record.images || [],
    result: record.inspectionStatus || 'QUALIFIED',
    acceptanceDate: dateStr,
    acceptor: record.createByName || userStore.userInfo?.name || ''
  }
}

const preventClose = () => {}
const setResult = (result) => { form.value.result = result }
const confirmDate = () => {
  form.value.acceptanceDate = `${selectedYear.value}-${String(selectedMonth.value).padStart(2, '0')}-${String(selectedDay.value).padStart(2, '0')}`
  showDateSelector.value = false
}

const chooseImage = () => {
  uni.chooseImage({
    count: 9 - form.value.images.length,
    sizeType: ['original'], // 选择原图，由我们自己压缩
    sourceType: ['album', 'camera'],
    success: (res) => res.tempFilePaths.forEach(path => compressAndUpload(path))
  })
}

// 压缩并上传图片
const compressAndUpload = async (filePath) => {
  uploadingCount.value++
  uni.showLoading({ title: '压缩中...', mask: true })
  
  try {
    // 压缩图片
    const compressResult = await compressImage(filePath, {
      quality: null, // 使用智能压缩
      maxWidth: 1920,
      maxHeight: 1920,
      enableSmartCompression: true
    })
    
    if (compressResult.success) {
      const compressedPath = compressResult.tempFilePath
      
      // 显示压缩信息
      if (!compressResult.skipped && compressResult.compressionRatio > 0) {
        console.log(`图片压缩: ${formatFileSize(compressResult.originalSize)} -> ${formatFileSize(compressResult.compressedSize)} (节省${compressResult.compressionRatio}%)`)
      }
      
      // 上传压缩后的图片
      uni.showLoading({ title: '上传中...', mask: true })
      uploadImage(compressedPath)
    } else {
      // 压缩失败，上传原图
      uni.showLoading({ title: '上传中...', mask: true })
      uploadImage(filePath)
    }
  } catch (error) {
    console.error('图片压缩失败:', error)
    // 压缩失败，上传原图
    uni.showLoading({ title: '上传中...', mask: true })
    uploadImage(filePath)
  }
}

const uploadImage = (filePath) => {
  uni.uploadFile({
    url: BASE_URL + '/app/upload',
    filePath, name: 'file',
    header: { 'Authorization': `Bearer ${userStore.token}` },
    success: (res) => {
      try {
        const data = JSON.parse(res.data)
        if (data.code === 200) form.value.images.push(data.fileName || data.url)
        else uni.showToast({ title: data.msg || '上传失败', icon: 'none' })
      } catch (e) { uni.showToast({ title: '上传失败', icon: 'none' }) }
    },
    fail: () => uni.showToast({ title: '上传失败', icon: 'none' }),
    complete: () => { uploadingCount.value--; if (uploadingCount.value === 0) uni.hideLoading() }
  })
}

const getImageUrl = (img) => {
  if (!img) return ''
  return img.startsWith('http://') || img.startsWith('https://') ? img : BASE_URL + img
}

const removeImage = (index) => { form.value.images.splice(index, 1) }
const handleClose = () => { if (!submitting.value) emit('update:visible', false) }
const handleMaskClick = (e) => { if (e.target === e.currentTarget) handleClose() }

const validateForm = () => {
  if (!form.value.title.trim()) { uni.showToast({ title: '请输入验收标题', icon: 'none' }); return false }
  if (!form.value.content.trim()) { uni.showToast({ title: '请输入验收内容', icon: 'none' }); return false }
  if (form.value.content.trim().length < 5) { uni.showToast({ title: '验收内容至少5个字', icon: 'none' }); return false }
  if (!form.value.acceptanceDate) { uni.showToast({ title: '请选择验收时间', icon: 'none' }); return false }
  return true
}

const handleSubmit = async () => {
  if (submitting.value) return
  if (uploadingCount.value > 0) { uni.showToast({ title: '请等待图片上传完成', icon: 'none' }); return }
  if (!validateForm()) return
  
  submitting.value = true
  try {
    const data = {
      scheduleId: props.schedule?.id || props.editRecord?.scheduleId,
      recordType: 'ACCEPTANCE',
      acceptanceTitle: form.value.title,
      acceptanceContent: form.value.content,
      images: JSON.stringify(form.value.images),
      acceptanceResult: form.value.result,
      acceptanceTime: form.value.acceptanceDate + ' 00:00:00',
      acceptor: form.value.acceptor
    }
    
    if (isEdit.value) {
      await updateAcceptanceRecord(props.editRecord.id, data)
      uni.showToast({ title: '修改成功', icon: 'success' })
    } else {
      await addAcceptanceRecord(data)
      uni.showToast({ title: '提交成功', icon: 'success' })
    }
    emit('success')
    emit('update:visible', false)
  } catch (error) {
    console.error('提交验收失败:', error)
    uni.showToast({ title: error.message || '提交失败', icon: 'none' })
  } finally { submitting.value = false }
}
</script>

<style lang="scss" scoped>
.acceptance-dialog-mask {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  width: 100vw; height: 100vh;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: flex-end;
  justify-content: center;
}

.acceptance-dialog {
  width: 100vw;
  max-height: 75vh;
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
  justify-content: center;
  height: 100rpx;
  border-bottom: 1rpx solid #eee;
  position: relative;
  flex-shrink: 0;
}

.dialog-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #333;
}

.close-btn {
  position: absolute;
  right: 20rpx;
  top: 50%;
  transform: translateY(-50%);
  width: 60rpx;
  height: 60rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.close-icon {
  font-size: 40rpx;
  color: #999;
}

.dialog-body {
  flex: 1;
  padding: 24rpx 32rpx;
  box-sizing: border-box;
  overflow-y: auto;
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

.form-input {
  width: 100%;
  height: 80rpx;
  padding: 0 20rpx;
  background: #f5f5f5;
  border-radius: 12rpx;
  font-size: 28rpx;
  box-sizing: border-box;
  
  &.readonly {
    display: flex;
    align-items: center;
    color: #999;
    background: #f0f0f0;
  }
}

.form-textarea {
  width: 100%;
  height: 140rpx;
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

.form-picker {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 80rpx;
  padding: 0 20rpx;
  background: #f5f5f5;
  border-radius: 12rpx;
  font-size: 28rpx;
  color: #333;
  box-sizing: border-box;
  
  .picker-arrow { color: #999; font-size: 32rpx; }
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
  
  text { color: #fff; font-size: 22rpx; line-height: 1; }
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
    box-sizing: border-box;
    
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

.bottom-space { height: 20rpx; }

.dialog-footer {
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
.btn-submit {
  background: #AD9B4B;
  color: #fff;
  &.disabled { opacity: 0.6; }
}

.date-selector-mask {
  position: absolute;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0, 0, 0, 0.3);
  display: flex;
  align-items: flex-end;
  z-index: 10;
}

.date-selector {
  width: 100%;
  background: #fff;
  border-radius: 24rpx 24rpx 0 0;
}

.date-selector-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24rpx 32rpx;
  border-bottom: 1rpx solid #eee;
}

.date-selector-title { font-size: 30rpx; font-weight: 600; color: #333; }
.date-selector-cancel { font-size: 28rpx; color: #999; padding: 10rpx; }
.date-selector-confirm { font-size: 28rpx; color: #AD9B4B; font-weight: 500; padding: 10rpx; }

.date-picker-columns { display: flex; height: 400rpx; }
.picker-column { flex: 1; height: 100%; }
.picker-item {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 80rpx;
  font-size: 30rpx;
  color: #666;
  font-weight: normal;
  transition: all 0.2s ease;
  
  &.picker-item-selected {
    font-size: 36rpx;
    color: #AD9B4B;
    font-weight: 600;
    background: rgba(173, 155, 75, 0.1);
    border-radius: 8rpx;
    margin: 0 20rpx;
  }
}
</style>
