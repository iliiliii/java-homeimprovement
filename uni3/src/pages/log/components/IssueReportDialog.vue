<template>
  <view v-if="visible" class="issue-dialog-mask" :style="{ zIndex: dialogZIndex }" @tap="handleMaskClick">
    <view class="issue-dialog" @tap.stop="preventClose">
      <!-- 头部 -->
      <view class="dialog-header">
        <view class="header-left" @tap="openIssueList">
          <text class="list-icon">☰</text>
          <text class="list-text">问题列表</text>
        </view>
        <text class="dialog-title">问题上报</text>
        <view class="close-btn" @tap="handleClose">
          <text class="close-icon">×</text>
        </view>
      </view>
      
      <!-- 表单内容 -->
      <scroll-view scroll-y class="dialog-body">
        <!-- 问题标题 -->
        <view class="form-item">
          <view class="form-label">
            <text class="required">*</text>
            <text>问题标题</text>
          </view>
          <input 
            class="form-input"
            v-model="form.title"
            placeholder="例如: 墙面平整度问题"
            :maxlength="50"
          />
        </view>
        
        <!-- 问题描述 -->
        <view class="form-item">
          <view class="form-label">
            <text class="required">*</text>
            <text>问题描述</text>
          </view>
          <textarea 
            class="form-textarea"
            v-model="form.description"
            placeholder="请详细描述质量问题（5-500字）"
            :maxlength="500"
            :auto-height="false"
          />
          <view class="word-count">{{ form.description.length }}/500</view>
        </view>

        <!-- 质检类型 -->
        <view class="form-item">
          <view class="form-label">
            <text class="required">*</text>
            <text>质检类型</text>
          </view>
          <view class="form-picker" @tap="showTypePicker = true">
            <text :class="{ placeholder: !form.inspectionType }">
              {{ form.inspectionType ? getTypeLabel(form.inspectionType) : '请选择质检类型' }}
            </text>
            <text class="picker-arrow">›</text>
          </view>
        </view>
        
        <!-- 问题分类 -->
        <view class="form-item">
          <view class="form-label">
            <text class="required">*</text>
            <text>问题分类</text>
          </view>
          <view class="form-picker" @tap="showCategoryPicker = true">
            <text :class="{ placeholder: !form.category }">
              {{ form.category ? getCategoryLabel(form.category) : '请选择问题分类' }}
            </text>
            <text class="picker-arrow">›</text>
          </view>
        </view>
        
        <!-- 问题位置 -->
        <view class="form-item">
          <view class="form-label">
            <text>问题位置</text>
          </view>
          <input 
            class="form-input"
            v-model="form.location"
            placeholder="请输入问题具体位置（如：主卧墙面）"
            :maxlength="100"
          />
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

        <!-- 整改期限 -->
        <view class="form-item">
          <view class="form-label">
            <text>整改期限</text>
          </view>
          <view class="form-picker" @tap="showDueDateSelector = true">
            <text :class="{ placeholder: !form.dueDate }">{{ form.dueDate || '请选择整改期限' }}</text>
            <text class="picker-arrow">›</text>
          </view>
        </view>
        
        <!-- 检查日期 -->
        <view class="form-item">
          <view class="form-label">
            <text class="required">*</text>
            <text>检查日期</text>
          </view>
          <view class="form-picker" @tap="showDateSelector = true">
            <text>{{ form.inspectionDate || '请选择日期' }}</text>
            <text class="picker-arrow">›</text>
          </view>
        </view>
        
        <!-- 上报人（只读） -->
        <view class="form-item">
          <view class="form-label">
            <text class="required">*</text>
            <text>上报人</text>
          </view>
          <view class="form-input readonly">
            <text>{{ form.reporter }}</text>
          </view>
        </view>
        
        <!-- 底部占位 -->
        <view class="bottom-space"></view>
      </scroll-view>
      
      <!-- 底部按钮 -->
      <view class="dialog-footer">
        <view class="btn btn-cancel" @tap="handleClose">取消</view>
        <view class="btn btn-submit" :class="{ disabled: submitting }" @tap="handleSubmit">
          {{ submitting ? '提交中...' : '提交上报' }}
        </view>
      </view>

      <!-- 质检类型选择器 -->
      <view v-if="showTypePicker" class="picker-mask" @tap="showTypePicker = false">
        <view class="picker-container" @tap.stop>
          <view class="picker-header">
            <text class="picker-cancel" @tap="showTypePicker = false">取消</text>
            <text class="picker-title">选择质检类型</text>
            <text class="picker-confirm" @tap="confirmTypePicker">确定</text>
          </view>
          <scroll-view scroll-y class="picker-list">
            <view 
              v-for="item in inspectionTypes" 
              :key="item.value"
              class="picker-item"
              :class="{ active: tempType === item.value }"
              @tap="tempType = item.value"
            >
              <text>{{ item.label }}</text>
              <text v-if="tempType === item.value" class="check-icon">✓</text>
            </view>
          </scroll-view>
        </view>
      </view>
      
      <!-- 问题分类选择器 -->
      <view v-if="showCategoryPicker" class="picker-mask" @tap="showCategoryPicker = false">
        <view class="picker-container" @tap.stop>
          <view class="picker-header">
            <text class="picker-cancel" @tap="showCategoryPicker = false">取消</text>
            <text class="picker-title">选择问题分类</text>
            <text class="picker-confirm" @tap="confirmCategoryPicker">确定</text>
          </view>
          <scroll-view scroll-y class="picker-list">
            <view 
              v-for="item in categories" 
              :key="item.value"
              class="picker-item"
              :class="{ active: tempCategory === item.value }"
              @tap="tempCategory = item.value"
            >
              <text>{{ item.label }}</text>
              <text v-if="tempCategory === item.value" class="check-icon">✓</text>
            </view>
          </scroll-view>
        </view>
      </view>

      <!-- 检查日期选择器 -->
      <view v-if="showDateSelector" class="date-selector-mask" @tap="showDateSelector = false">
        <view class="date-selector" @tap.stop>
          <view class="date-selector-header">
            <text class="date-selector-cancel" @tap="showDateSelector = false">取消</text>
            <text class="date-selector-title">选择检查日期</text>
            <text class="date-selector-confirm" @tap="confirmDate">确定</text>
          </view>
          <view class="date-picker-columns">
            <picker-view class="picker-column" :value="[yearIndex]" @change="onYearChange">
              <picker-view-column>
                <view 
                  v-for="(year, index) in years" 
                  :key="year" 
                  class="picker-date-item"
                  :class="{ 'picker-item-selected': index === yearIndex }"
                >{{ year }}年</view>
              </picker-view-column>
            </picker-view>
            <picker-view class="picker-column" :value="[monthIndex]" @change="onMonthChange">
              <picker-view-column>
                <view 
                  v-for="(month, index) in months" 
                  :key="month" 
                  class="picker-date-item"
                  :class="{ 'picker-item-selected': index === monthIndex }"
                >{{ month }}月</view>
              </picker-view-column>
            </picker-view>
            <picker-view class="picker-column" :value="[dayIndex]" @change="onDayChange">
              <picker-view-column>
                <view 
                  v-for="(day, index) in days" 
                  :key="day" 
                  class="picker-date-item"
                  :class="{ 'picker-item-selected': index === dayIndex }"
                >{{ day }}日</view>
              </picker-view-column>
            </picker-view>
          </view>
        </view>
      </view>
      
      <!-- 整改期限选择器 -->
      <view v-if="showDueDateSelector" class="date-selector-mask" @tap="showDueDateSelector = false">
        <view class="date-selector" @tap.stop>
          <view class="date-selector-header">
            <text class="date-selector-cancel" @tap="showDueDateSelector = false">取消</text>
            <text class="date-selector-title">选择整改期限</text>
            <text class="date-selector-confirm" @tap="confirmDueDate">确定</text>
          </view>
          <view class="date-picker-columns">
            <picker-view class="picker-column" :value="[dueYearIndex]" @change="onDueYearChange">
              <picker-view-column>
                <view 
                  v-for="(year, index) in dueYears" 
                  :key="year" 
                  class="picker-date-item"
                  :class="{ 'picker-item-selected': index === dueYearIndex }"
                >{{ year }}年</view>
              </picker-view-column>
            </picker-view>
            <picker-view class="picker-column" :value="[dueMonthIndex]" @change="onDueMonthChange">
              <picker-view-column>
                <view 
                  v-for="(month, index) in months" 
                  :key="month" 
                  class="picker-date-item"
                  :class="{ 'picker-item-selected': index === dueMonthIndex }"
                >{{ month }}月</view>
              </picker-view-column>
            </picker-view>
            <picker-view class="picker-column" :value="[dueDayIndex]" @change="onDueDayChange">
              <picker-view-column>
                <view 
                  v-for="(day, index) in dueDays" 
                  :key="day" 
                  class="picker-date-item"
                  :class="{ 'picker-item-selected': index === dueDayIndex }"
                >{{ day }}日</view>
              </picker-view-column>
            </picker-view>
          </view>
        </view>
      </view>
    </view>

    <!-- 问题列表对话框 -->
    <IssueListDialog 
      v-model:visible="showIssueList"
      @refresh="handleIssueListRefresh"
    />
  </view>
</template>

<script setup>
import { ref, watch, computed } from 'vue'
import { useUserStore } from '@/store/user.js'
import { reportQualityIssue, getDictData } from '@/api/qualityIssue'
import { BASE_URL } from '@/utils/request'
import { compressImage, formatFileSize } from '@/utils/imageCompress'
import IssueListDialog from './IssueListDialog.vue'

const props = defineProps({
  visible: { type: Boolean, default: false }
})

const emit = defineEmits(['update:visible', 'success'])

const userStore = useUserStore()
const showIssueList = ref(false)
const submitting = ref(false)
const dialogZIndex = ref(9999)
const uploadingCount = ref(0)

// 选择器状态
const showTypePicker = ref(false)
const showCategoryPicker = ref(false)
const showDateSelector = ref(false)
const showDueDateSelector = ref(false)
const tempType = ref('')
const tempCategory = ref('')

// 质检类型选项（施工阶段）- 从字典加载
const inspectionTypes = ref([])

// 问题分类选项 - 从字典加载
const categories = ref([])

// 加载字典数据
const loadDictData = async () => {
  try {
    // 加载施工阶段字典
    const stageRes = await getDictData('decoration_construction_stage')
    if (stageRes && stageRes.length > 0) {
      inspectionTypes.value = stageRes
    } else {
      // 后备数据
      inspectionTypes.value = [
        { value: 'DISMANTLING', label: '拆除工程' },
        { value: 'WATER_ELECTRIC', label: '水电改造' },
        { value: 'WATERPROOF', label: '防水工程' },
        { value: 'TILING', label: '瓦工工程' },
        { value: 'CARPENTRY', label: '木工工程' },
        { value: 'PAINTING', label: '油漆工程' },
        { value: 'INSTALLATION', label: '安装工程' },
        { value: 'COMPLETION', label: '竣工验收' }
      ]
    }
    
    // 加载问题分类字典
    const severityRes = await getDictData('decoration_issue_severity')
    if (severityRes && severityRes.length > 0) {
      categories.value = severityRes
    } else {
      // 后备数据
      categories.value = [
        { value: 'GENERAL', label: '一般问题' },
        { value: 'CRITICAL', label: '红线问题' },
        { value: 'URGENT', label: '紧急问题' }
      ]
    }
  } catch (error) {
    console.error('加载字典数据失败:', error)
    // 使用后备数据
    inspectionTypes.value = [
      { value: 'DISMANTLING', label: '拆除工程' },
      { value: 'WATER_ELECTRIC', label: '水电改造' },
      { value: 'WATERPROOF', label: '防水工程' },
      { value: 'TILING', label: '瓦工工程' },
      { value: 'CARPENTRY', label: '木工工程' },
      { value: 'PAINTING', label: '油漆工程' },
      { value: 'INSTALLATION', label: '安装工程' },
      { value: 'COMPLETION', label: '竣工验收' }
    ]
    categories.value = [
      { value: 'GENERAL', label: '一般问题' },
      { value: 'CRITICAL', label: '红线问题' },
      { value: 'URGENT', label: '紧急问题' }
    ]
  }
}

// 检查日期选择器
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

// 整改期限选择器
const selectedDueYear = ref(new Date().getFullYear())
const selectedDueMonth = ref(new Date().getMonth() + 1)
const selectedDueDay = ref(new Date().getDate() + 7 > 28 ? 28 : new Date().getDate() + 7)

// 添加整改期限防抖定时器
const dueDateChangeTimer = ref(null)

const dueYears = computed(() => {
  const currentYear = new Date().getFullYear()
  return Array.from({ length: 3 }, (_, i) => currentYear + i)
})
const dueDays = computed(() => {
  const daysInMonth = new Date(selectedDueYear.value, selectedDueMonth.value, 0).getDate()
  return Array.from({ length: daysInMonth }, (_, i) => i + 1)
})

const dueYearIndex = computed(() => {
  const index = dueYears.value.indexOf(selectedDueYear.value)
  return index >= 0 ? index : 0
})
const dueMonthIndex = computed(() => {
  const index = selectedDueMonth.value - 1
  return index >= 0 && index < 12 ? index : 0
})
const dueDayIndex = computed(() => {
  const index = selectedDueDay.value - 1
  const maxDays = new Date(selectedDueYear.value, selectedDueMonth.value, 0).getDate()
  return index >= 0 && index < maxDays ? index : 0
})

// 防抖处理整改期限变更
const debounceDueDateChange = (callback) => {
  if (dueDateChangeTimer.value) {
    clearTimeout(dueDateChangeTimer.value)
  }
  dueDateChangeTimer.value = setTimeout(callback, 150) // 150ms 防抖延迟
}

const onDueYearChange = (e) => {
  debounceDueDateChange(() => {
    const newYear = dueYears.value[e.detail.value[0]]
    if (newYear) {
      selectedDueYear.value = newYear
      // 检查当前选择的天数是否在新年份的当前月份中有效
      const maxDay = new Date(selectedDueYear.value, selectedDueMonth.value, 0).getDate()
      if (selectedDueDay.value > maxDay) {
        selectedDueDay.value = maxDay
      }
    }
  })
}
const onDueMonthChange = (e) => {
  debounceDueDateChange(() => {
    const newMonth = e.detail.value[0] + 1
    if (newMonth >= 1 && newMonth <= 12) {
      selectedDueMonth.value = newMonth
      // 检查当前选择的天数是否在新月份中有效
      const maxDay = new Date(selectedDueYear.value, selectedDueMonth.value, 0).getDate()
      if (selectedDueDay.value > maxDay) {
        selectedDueDay.value = maxDay
      }
    }
  })
}
const onDueDayChange = (e) => { 
  debounceDueDateChange(() => {
    const newDay = e.detail.value[0] + 1
    const maxDay = new Date(selectedDueYear.value, selectedDueMonth.value, 0).getDate()
    if (newDay >= 1 && newDay <= maxDay) {
      selectedDueDay.value = newDay
    }
  })
}

// 表单数据
const form = ref({
  title: '', description: '', inspectionType: '', category: '',
  location: '', images: [], dueDate: '', inspectionDate: '', reporter: ''
})

watch(() => props.visible, (val) => {
  if (val) {
    loadDictData()
    initForm()
  }
})

const initForm = () => {
  const now = new Date()
  const dateStr = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')}`
  
  // 默认整改期限为7天后
  const dueDate = new Date(now.getTime() + 7 * 24 * 60 * 60 * 1000)
  const dueDateStr = `${dueDate.getFullYear()}-${String(dueDate.getMonth() + 1).padStart(2, '0')}-${String(dueDate.getDate()).padStart(2, '0')}`
  
  form.value = {
    title: '', description: '', inspectionType: '', category: '',
    location: '', images: [], dueDate: dueDateStr, inspectionDate: dateStr,
    reporter: userStore.userInfo?.name || ''
  }
  
  selectedYear.value = now.getFullYear()
  selectedMonth.value = now.getMonth() + 1
  selectedDay.value = now.getDate()
  
  selectedDueYear.value = dueDate.getFullYear()
  selectedDueMonth.value = dueDate.getMonth() + 1
  selectedDueDay.value = dueDate.getDate()
  
  tempType.value = ''
  tempCategory.value = ''
}

const getTypeLabel = (value) => {
  const item = inspectionTypes.value.find(t => t.value === value)
  return item ? item.label : value
}

const getCategoryLabel = (value) => {
  const item = categories.value.find(c => c.value === value)
  return item ? item.label : value
}

const confirmTypePicker = () => {
  form.value.inspectionType = tempType.value
  showTypePicker.value = false
}

const confirmCategoryPicker = () => {
  form.value.category = tempCategory.value
  showCategoryPicker.value = false
}

const confirmDate = () => {
  form.value.inspectionDate = `${selectedYear.value}-${String(selectedMonth.value).padStart(2, '0')}-${String(selectedDay.value).padStart(2, '0')}`
  showDateSelector.value = false
}

const confirmDueDate = () => {
  form.value.dueDate = `${selectedDueYear.value}-${String(selectedDueMonth.value).padStart(2, '0')}-${String(selectedDueDay.value).padStart(2, '0')}`
  showDueDateSelector.value = false
}

const preventClose = () => {}
const handleClose = () => { if (!submitting.value) emit('update:visible', false) }
const handleMaskClick = (e) => { if (e.target === e.currentTarget) handleClose() }

// 打开问题列表
const openIssueList = () => {
  showIssueList.value = true
}

// 问题列表刷新回调
const handleIssueListRefresh = () => {
  // 可以在这里处理刷新逻辑
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

const validateForm = () => {
  if (!form.value.title.trim()) { uni.showToast({ title: '请输入问题标题', icon: 'none' }); return false }
  if (!form.value.description.trim()) { uni.showToast({ title: '请输入问题描述', icon: 'none' }); return false }
  if (form.value.description.trim().length < 5) { uni.showToast({ title: '问题描述至少5个字', icon: 'none' }); return false }
  if (!form.value.inspectionType) { uni.showToast({ title: '请选择质检类型', icon: 'none' }); return false }
  if (!form.value.category) { uni.showToast({ title: '请选择问题分类', icon: 'none' }); return false }
  if (!form.value.inspectionDate) { uni.showToast({ title: '请选择检查日期', icon: 'none' }); return false }
  return true
}

const handleSubmit = async () => {
  if (submitting.value) return
  if (uploadingCount.value > 0) { uni.showToast({ title: '请等待图片上传完成', icon: 'none' }); return }
  if (!validateForm()) return
  
  submitting.value = true
  try {
    const data = {
      title: form.value.title,
      description: form.value.description,
      inspectionType: form.value.inspectionType,
      category: form.value.category,
      location: form.value.location,
      dueDate: form.value.dueDate,
      images: JSON.stringify(form.value.images),
      inspectionDate: form.value.inspectionDate + ' 00:00:00'
    }
    
    await reportQualityIssue(data)
    uni.showToast({ title: '上报成功', icon: 'success' })
    emit('success')
    emit('update:visible', false)
  } catch (error) {
    console.error('问题上报失败:', error)
    uni.showToast({ title: error.message || '上报失败', icon: 'none' })
  } finally { submitting.value = false }
}
</script>

<style lang="scss" scoped>
.issue-dialog-mask {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  width: 100vw; height: 100vh;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: flex-end;
  justify-content: center;
}

.issue-dialog {
  width: 100vw;
  max-height: 85vh;
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
  border-bottom: 1rpx solid #eee;
  position: relative;
  flex-shrink: 0;
  padding: 0 20rpx;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 10rpx;
  
  .list-icon {
    font-size: 32rpx;
    color: #AD9B4B;
  }
  
  .list-text {
    font-size: 26rpx;
    color: #AD9B4B;
  }
}

.dialog-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #333;
}

.close-btn {
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
  height: 180rpx;
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
  
  .placeholder { color: #999; }
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

// 选择器样式
.picker-mask, .date-selector-mask {
  position: absolute;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0, 0, 0, 0.3);
  display: flex;
  align-items: flex-end;
  z-index: 10;
}

.picker-container, .date-selector {
  width: 100%;
  background: #fff;
  border-radius: 24rpx 24rpx 0 0;
}

.picker-header, .date-selector-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24rpx 32rpx;
  border-bottom: 1rpx solid #eee;
}

.picker-title, .date-selector-title { font-size: 30rpx; font-weight: 600; color: #333; }
.picker-cancel, .date-selector-cancel { font-size: 28rpx; color: #999; padding: 10rpx; }
.picker-confirm, .date-selector-confirm { font-size: 28rpx; color: #AD9B4B; font-weight: 500; padding: 10rpx; }

.picker-list {
  max-height: 500rpx;
}

.picker-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 28rpx 32rpx;
  font-size: 28rpx;
  color: #333;
  border-bottom: 1rpx solid #f5f5f5;
  
  &.active { color: #AD9B4B; background: #faf8f0; }
  .check-icon { color: #AD9B4B; font-size: 28rpx; }
}

.date-picker-columns { display: flex; height: 400rpx; }
.picker-column { flex: 1; height: 100%; }
.picker-date-item {
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
