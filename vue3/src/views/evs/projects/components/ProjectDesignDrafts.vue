<template>
  <!-- 设计稿管理对话框 -->
  <el-dialog
    v-model="dialogVisible"
    :title="`${project?.name || ''} - 设计稿管理`"
    width="1000px"
    append-to-body
    :close-on-click-modal="false"
    class="project-design-drafts-dialog"
  >
    <template #header>
      <div style="display: flex; align-items: center; justify-content: space-between; width: 100%;">
        <div style="display: flex; align-items: center; gap: 12px;">
          <el-icon style="font-size: 20px;"><Picture /></el-icon>
          <span style="font-size: 16px; font-weight: 600;">{{ project?.name || '' }} - 设计稿管理</span>
        </div>
        <el-button type="primary" @click="handleAddRoom" :icon="Plus">
          添加房间
        </el-button>
      </div>
    </template>

    <div style="padding: 8px 0; max-height: calc(90vh - 200px); overflow-y: auto;">
      <!-- 提示信息 -->
      <div style="margin-bottom: 16px; padding: 12px; background: #f0f9ff; border: 1px solid #91d5ff; border-radius: 4px; font-size: 13px; color: #666;">
        <el-icon style="vertical-align: middle; margin-right: 8px;"><InfoFilled /></el-icon>
        按房间分类管理设计图纸，可自定义添加房间
      </div>

      <!-- 房间导航控制 -->
      <div v-if="rooms.length > 0" class="room-navigation">
        <div class="nav-header">
          <div class="nav-left">
            <div class="room-stats">
              <el-tag type="info" size="small">
                <el-icon><Folder /></el-icon>
                {{ rooms.length }}个房间
              </el-tag>
              <el-tag type="success" size="small" style="margin-left: 8px;">
                <el-icon><Picture /></el-icon>
                {{ totalImageCount }}张图片
              </el-tag>
            </div>
          </div>
          <div class="nav-right">
            <!-- 房间搜索 -->
            <el-input
              v-model="searchKeyword"
              placeholder="搜索房间..."
              size="small"
              style="width: 200px;"
              clearable
              prefix-icon="Search"
            />
          </div>
        </div>
      </div>

      <!-- 房间列表 -->
      <div v-loading="loading" element-loading-text="正在加载房间列表...">
        <!-- 改进的空状态显示 -->
        <div v-if="rooms.length === 0 && !loading" class="room-list-empty">
          <!-- 加载错误状态 -->
          <div v-if="loadError" class="error-state">
            <el-result
              icon="warning"
              :title="emptyDescription"
              :sub-title="lastError?.message || '网络连接异常，请稍后重试'"
            >
              <template #extra>
                <el-button type="primary" @click="loadRooms" v-if="showRetryButton">
                  <el-icon><Refresh /></el-icon>
                  重新加载
                </el-button>
              </template>
            </el-result>
          </div>

          <!-- 正常空状态 -->
          <div v-else class="empty-state">
            <el-empty
              :description="emptyDescription"
              :image-size="120"
            >
              <template #image>
                <el-icon style="font-size: 64px; color: #c0c4cc;"><House /></el-icon>
              </template>
              <template #description>
                <p class="empty-description">{{ emptyDescription }}</p>
                <p class="empty-tip">{{ emptyTip }}</p>
              </template>
            </el-empty>

          </div>
        </div>
        <!-- 手风琴折叠视图 -->
        <div v-else-if="!loading" class="rooms-accordion">
          <el-collapse v-model="activeAccordionItems" accordion>
            <el-collapse-item
              v-for="room in filteredRooms"
              :key="room.id"
              :name="room.id"
            >
              <template #title>
                <div class="accordion-title">
                  <div class="title-left">
                    <el-icon><Folder /></el-icon>
                    <span class="room-name">{{ room.roomName }}</span>
                    <el-tag size="small" type="info">{{ getRoomTypeText(room.roomType) }}</el-tag>
                    <el-tag size="small" type="success">{{ getRoomImageCount(room) }}张图片</el-tag>
                    <el-tag v-if="room.area" size="small" type="warning">{{ room.area }}㎡</el-tag>
                    <el-tag v-if="room.floor" size="small" type="info">{{ room.floor }}(楼层)</el-tag>
                  </div>
                  <div class="title-right" @click.stop>
                    <el-button
                      type="primary"
                      link
                      size="small"
                      @click="handleEditRoom(room)"
                      :icon="Edit"
                    >
                      编辑
                    </el-button>
                    <el-button
                      type="danger"
                      link
                      size="small"
                      @click="handleDeleteRoom(room.id)"
                      :icon="Delete"
                    >
                      删除
                    </el-button>
                  </div>
                </div>
              </template>

              <div class="accordion-content">
                <!-- 房间描述 - 简洁文本显示 -->
                <div v-if="room.description" class="room-description">
                  <!-- <span class="room-description-label">房间描述：</span> -->
                  <span class="room-description-text">{{ room.description }}</span>
                </div>

                <!-- 图片上传和管理 -->
                <div v-loading="uploadingRoomId === room.id" element-loading-text="正在保存设计稿...">
                  <div style="margin-top: 16px;">
                    <div style="display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px;">
                      <span style="font-size: 14px; color: #666;">设计图管理 ({{ getRoomImageCount(room) }}/20)</span>
                    </div>

                    <ImageUploadCard
                      :ref="el => setUploadRef(el, room.id)"
                      v-model="room.fileList"
                      :upload-url="uploadUrl"
                      :upload-headers="{
                        Authorization: 'Bearer ' + userStore.token
                      }"
                      :disabled="uploadingRoomId === room.id"
                      @success="(data) => handleUploadSuccess(data, room)"
                      @remove="(data) => handleRemove(data, room)"
                      @error="handleUploadError"
                    />
                  </div>
                </div>
              </div>
            </el-collapse-item>
          </el-collapse>
        </div>
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleCancel">关 闭</el-button>
      </div>
    </template>

    <!-- 房间管理对话框 - 新增和编辑共用 -->
    <el-dialog
      v-model="roomDialogVisible"
      :title="roomDialogMode === 'add' ? '添加房间' : '编辑房间'"
      width="800px"
      append-to-body
      :close-on-click-modal="false"
    >
      <el-form
        ref="roomFormRef"
        :model="roomForm"
        :rules="roomRules"
        label-width="100px"
      >
        <el-form-item label="房间类型" prop="roomType" required>
          <el-select
            v-model="roomForm.roomType"
            placeholder="请选择房间类型"
            style="width: 100%"
            @change="handleRoomTypeChange"
          >
            <el-option
              v-for="item in decoration_room_type"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="房间名称" prop="roomName" required>
          <el-input
            v-model="roomForm.roomName"
            placeholder="请输入房间名称，或根据房间类型自动生成"
            style="width: 100%"
          />
        </el-form-item>

        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="房间面积">
              <el-input-number
                v-model="roomForm.area"
                placeholder="平方米"
                :min="0"
                :precision="2"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="16">
            <el-form-item label="房间楼层">
              <el-input-number
                v-model="roomForm.floor"
                type="number"
                placeholder="如：1楼、2楼，主要用于复式、别墅等特殊户型，并非总楼层"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="房间朝向">
          <el-select
            v-model="roomForm.orientation"
            placeholder="请选择朝向"
            style="width: 100%"
          >
            <el-option
              v-for="item in decoration_orientation"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="房间描述">
          <el-input
            v-model="roomForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入房间描述（可选）"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="handleCancelRoom">取 消</el-button>
          <el-button
            v-if="roomDialogMode === 'add'"
            type="primary"
            @click="handleConfirmRoom"
            :loading="addingRoom"
          >
            确认添加
          </el-button>
          <el-button
            v-if="roomDialogMode === 'add'"
            type="success"
            @click="handleAddAndContinue"
            :loading="addingRoom"
          >
            添加并继续
          </el-button>
          <el-button
            v-if="roomDialogMode === 'edit'"
            type="primary"
            @click="handleConfirmRoom"
            :loading="editingRoom"
          >
            确认更新
          </el-button>
        </div>
      </template>
    </el-dialog>
  </el-dialog>
</template>

<script setup>
import { Picture, Plus, Delete, Edit, Folder, InfoFilled, House, Refresh, Search } from '@element-plus/icons-vue'
import { listProjectRooms, addProjectRooms, updateProjectRooms, delProjectRooms } from '@/api/evs/projectRooms'
import useUserStore from '@/store/modules/user'
import ImageUploadCard from '@/components/ImageUploadCard/index.vue'
import { onUnmounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'

// 简单的重试机制，替代 requestHelper.js 中的复杂实现
const retryRequest = async (requestFn, maxRetries = 2) => {
  for (let attempt = 0; attempt <= maxRetries; attempt++) {
    try {
      return await requestFn()
    } catch (error) {
      if (attempt === maxRetries) throw error
      await new Promise(resolve => setTimeout(resolve, 1000 * (attempt + 1)))
    }
  }
}

const { proxy } = getCurrentInstance()
const { decoration_room_type, decoration_orientation } = proxy.useDict('decoration_room_type', 'decoration_orientation')

const userStore = useUserStore()

// 上传相关配置
const uploadUrl = ref(import.meta.env.VITE_APP_BASE_API + '/common/upload')

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  project: {
    type: Object,
    default: () => ({})
  }
})

const emit = defineEmits(['update:modelValue', 'success', 'rooms-updated', 'designs-updated'])

// 响应式数据
const dialogVisible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const rooms = ref([])
const loading = ref(false)
const uploadingRoomId = ref(null) // 正在上传的房间ID
const addingRoom = ref(false)
const editingRoom = ref(false) // 编辑房间状态
const roomDialogVisible = ref(false) // 房间对话框显示状态
const roomDialogMode = ref('add') // add 或 edit
const roomFormRef = ref(null) // 房间表单引用
const uploadRef = ref(null) // ImageUploadCard 组件引用

// 新增：错误处理和状态管理
const loadError = ref(false)
const lastError = ref(null)

// 新增：导航状态
const searchKeyword = ref('')
const activeAccordionItems = ref([])

// 用于取消请求的控制器
let currentRequestController = null
let currentProjectId = null

// 防竞态机制：上传队列管理
const roomUploadQueues = ref(new Map()) // 每个房间的上传队列
const roomUploadTimers = ref(new Map()) // 每个房间的延迟保存定时器

// 房间表单
const roomForm = ref({
  id: '',
  roomType: '',
  roomName: '',
  area: null,
  floor: '',
  orientation: '',
  description: ''
})

// 表单验证规则
const roomRules = {
  roomType: [
    { required: true, message: '请选择房间类型', trigger: 'change' }
  ],
  roomName: [
    { required: true, message: '请输入房间名称', trigger: 'blur' }
  ]
}

// 智能空状态描述
const emptyDescription = computed(() => {
  if (!props.project?.id) {
    return '请先选择项目'
  }
  if (loadError.value) {
    return '加载房间数据失败'
  }
  return '当前项目暂无房间'
})

const emptyTip = computed(() => {
  if (!props.project?.id) {
    return '请在项目详情页面查看房间信息'
  }
  if (loadError.value) {
    return '请检查网络连接或稍后重试'
  }
  return '点击上方按钮添加项目房间'
})

const showAddButton = computed(() => {
  return props.project?.id && !loadError.value
})

const showRetryButton = computed(() => {
  return loadError.value && !loading.value
})

// 计算属性：过滤后的房间列表
const filteredRooms = computed(() => {
  if (!searchKeyword.value) {
    return rooms.value
  }

  return rooms.value.filter(room => {
    const keyword = searchKeyword.value.toLowerCase()
    return room.roomName.toLowerCase().includes(keyword) ||
           getRoomTypeText(room.roomType).toLowerCase().includes(keyword) ||
           (room.description && room.description.toLowerCase().includes(keyword))
  })
})

// 计算属性：总图片数量
const totalImageCount = computed(() => {
  return rooms.value.reduce((total, room) => total + getRoomImageCount(room), 0)
})

// 生成房间名称
function generateRoomName(roomType) {
  if (!roomType) return ''
  
  const typeLabel = decoration_room_type.value.find(item => item.value === roomType)?.label || roomType
  const sameTypeRooms = rooms.value.filter(r => r.roomType === roomType)
  const count = sameTypeRooms.length + 1
  
  return `${typeLabel}-#${count}`
}

// 房间类型改变时自动生成名称
function handleRoomTypeChange() {
  if (!roomForm.value.roomName || roomForm.value.roomName.match(/^.+-\d+$/)) {
    roomForm.value.roomName = generateRoomName(roomForm.value.roomType)
  }
}

// 获取房间文件ID数组（统一JSON解析逻辑）
function getRoomFileIds(room) {
  if (!room.fileIds) return []
  try {
    return Array.isArray(room.fileIds) ? room.fileIds : JSON.parse(room.fileIds)
  } catch {
    return room.fileIds.split(',').filter(Boolean)
  }
}

// 获取房间图片数量
function getRoomImageCount(room) {
  return getRoomFileIds(room).length
}

// 获取房间类型中文文本
function getRoomTypeText(roomType) {
  if (!roomType) return ''

  const typeItem = decoration_room_type.value.find(item => item.value === roomType)
  return typeItem ? typeItem.label : roomType
}

// 设置房间上传组件引用
function setUploadRef(el, roomId) {
  if (el) {
    // 存储每个房间的上传组件引用
    if (!uploadRef.value) {
      uploadRef.value = {}
    }
    uploadRef.value[roomId] = el
  }
}

// 清理上传队列和定时器
function cleanupUploadQueues() {
  console.log('[设计稿管理] 清理上传队列和定时器')

  // 清理所有定时器
  roomUploadTimers.value.forEach((timer, roomId) => {
    clearTimeout(timer)
    console.log(`[设计稿管理] 清理房间 ${roomId} 的上传定时器`)
  })
  roomUploadTimers.value.clear()

  // 执行剩余的上传队列
  roomUploadQueues.value.forEach((queue, roomId) => {
    if (queue.size > 0) {
      console.log(`[设计稿管理] 执行房间 ${roomId} 剩余 ${queue.size} 张图片的上传`)
      const room = rooms.value.find(r => r.id === roomId)
      if (room) {
        executeRoomFileIdsUpdate(room)
      }
    }
  })
  roomUploadQueues.value.clear()
}

// 工具函数：解析房间fileIds为el-upload的fileList格式
function parseFileIdsToListForRoom(room) {
  const fileIdsArray = getRoomFileIds(room)
  const baseUrl = import.meta.env.VITE_APP_BASE_API

  return fileIdsArray.map((fileId, index) => {
    let fullUrl = fileId

    if (fileId.startsWith('http')) {
      // 完整URL直接使用
      fullUrl = fileId
    } else if (fileId.startsWith(baseUrl)) {
      // 已包含baseUrl的路径直接使用
      fullUrl = fileId
    } else {
      // 纯粹的相对路径需要拼接baseUrl
      let path = fileId
      if (!path.startsWith('/')) {
        path = '/' + path
      }
      const cleanBaseUrl = baseUrl.endsWith('/') ? baseUrl.slice(0, -1) : baseUrl
      fullUrl = cleanBaseUrl + path
    }

    return {
      uid: `existing-${index}`,
      name: `design-image-${index}.jpg`,
      url: fullUrl,
      status: 'success'
    }
  })
}

// 上传相关函数 - 防竞态版本
/** 上传成功回调 - 使用队列机制防止竞态条件 */
function handleUploadSuccess({ response, imageUrl }, room) {
  if (response.code === 200) {
    console.log(`[设计稿上传] 房间 ${room.id} 上传成功:`, imageUrl)

    // 获取或创建该房间的上传队列
    if (!roomUploadQueues.value.has(room.id)) {
      roomUploadQueues.value.set(room.id, new Set())
    }

    // 处理URL格式
    if (imageUrl && !imageUrl.startsWith('http')) {
      // 移除开头的斜杠，与后端存储格式一致
      const normalizedImageId = imageUrl.startsWith('/') ? imageUrl.substring(1) : imageUrl
      roomUploadQueues.value.get(room.id).add(normalizedImageId)
    }

    // 延迟保存，防止单个上传触发多次数据库操作
    scheduleRoomFileIdsUpdate(room)
  }
}

/** 延迟保存房间文件ID，批量处理上传结果 */
function scheduleRoomFileIdsUpdate(room) {
  const roomId = room.id

  // 清除之前的定时器
  if (roomUploadTimers.value.has(roomId)) {
    clearTimeout(roomUploadTimers.value.get(roomId))
  }

  // 设置新的延迟定时器
  const timer = setTimeout(() => {
    executeRoomFileIdsUpdate(room)
    roomUploadTimers.value.delete(roomId)
  }, 500) // 延迟500ms，批量处理连续的上传操作

  roomUploadTimers.value.set(roomId, timer)
}

/** 执行房间文件ID更新 */
async function executeRoomFileIdsUpdate(room) {
  const roomId = room.id
  const uploadQueue = roomUploadQueues.value.get(roomId)

  if (!uploadQueue || uploadQueue.size === 0) {
    console.log(`[设计稿上传] 房间 ${roomId} 没有待处理的图片，跳过更新`)
    return
  }

  try {
    console.log(`[设计稿上传] 开始批量更新房间 ${roomId} 的 ${uploadQueue.size} 张图片`)

    // 获取当前房间的现有文件ID
    const existingFileIds = getRoomFileIds(room)

    // 合并现有文件ID和新上传的文件ID
    const newFileIds = Array.from(existingFileIds)
    uploadQueue.forEach(imageId => {
      if (!newFileIds.includes(imageId)) {
        newFileIds.push(imageId)
      }
    })

    // 清空该房间的上传队列
    uploadQueue.clear()

    // 更新数据库
    await updateRoomFileIds(room, newFileIds)

    console.log(`[设计稿上传] 房间 ${roomId} 批量更新完成，当前图片数量: ${newFileIds.length}`)

  } catch (error) {
    console.error(`[设计稿上传] 房间 ${roomId} 批量更新失败:`, error)
    proxy.$modal.msgError('保存图片失败，请重试')
  }
}

/** 上传失败回调 */
function handleUploadError({ file, message }) {
  // 新组件已经处理了错误提示，这里可以添加特殊逻辑
  console.error('设计稿上传失败:', { file, message })
}

/** 移除图片回调 - 优化版本，支持乐观更新和重试机制 */
function handleRemove({ file, fileList }, room) {
  console.log(`[设计稿删除] 房间 ${room.id} 删除图片:`, file.name)

  try {
    // 获取当前房间的上传组件引用
    const uploadComponent = uploadRef.value?.[room.id]
    if (!uploadComponent) {
      console.error('[设计稿删除] 无法找到房间上传组件引用')
      proxy.$modal.msgError('删除图片失败')
      return
    }

    // 使用组件暴露的工具函数提取当前剩余图片的URL
    if (!uploadComponent.extractImageUrls) {
      console.error('[设计稿删除] extractImageUrls 工具函数不可用')
      proxy.$modal.msgError('删除图片失败')
      return
    }

    // 获取当前fileList中所有图片的相对路径
    const remainingUrls = uploadComponent.extractImageUrls(fileList)
    console.log(`[设计稿删除] 房间 ${room.id} 剩余图片URL:`, remainingUrls)

    // 从当前房间的fileIds中筛选出剩余图片的ID
    const currentFileIds = getRoomFileIds(room)

    // 优化URL比较逻辑
    const filteredFileIds = currentFileIds.filter(fileId => {
      // 检查这个fileId是否还在剩余图片列表中
      return remainingUrls.some(remainingUrl => {
        return normalizeImagePath(fileId) === normalizeImagePath(remainingUrl)
      })
    })

    console.log(`[设计稿删除] 房间 ${room.id} 过滤前: ${currentFileIds.length}, 过滤后: ${filteredFileIds.length}`)

    // 乐观更新：先更新UI，再更新数据库
    const roomIndex = rooms.value.findIndex(r => r.id === room.id)
    if (roomIndex > -1) {
      rooms.value[roomIndex].fileIds = JSON.stringify(filteredFileIds)
    }

    // 异步更新数据库，带重试机制
    updateRoomFileIdsWithRetry(room, filteredFileIds, currentFileIds.length)

  } catch (error) {
    console.error('[设计稿删除] 删除图片失败:', error)
    proxy.$modal.msgError('删除图片失败，请重试')
  }
}

/** 统一图片路径格式的工具函数 */
function normalizeImagePath(imagePath) {
  if (!imagePath) return ''

  const baseUrl = import.meta.env.VITE_APP_BASE_API
  let normalizedPath = imagePath

  // 移除baseUrl前缀
  if (imagePath.startsWith(baseUrl)) {
    normalizedPath = imagePath.substring(baseUrl.length)
  }

  // 移除开头的斜杠
  if (normalizedPath.startsWith('/')) {
    normalizedPath = normalizedPath.substring(1)
  }

  return normalizedPath.toLowerCase()
}

/** 带重试机制的房间文件ID更新 */
async function updateRoomFileIdsWithRetry(room, fileIds, originalCount, attempt = 1) {
  try {
    await updateRoomFileIds(room, fileIds)
    console.log(`[设计稿删除] 房间 ${room.id} 删除完成，从 ${originalCount} 张减少到 ${fileIds.length} 张`)
  } catch (error) {
    console.error(`[设计稿删除] 房间 ${room.id} 更新失败 (尝试 ${attempt}/3):`, error)

    if (attempt < 3) {
      // 重试前等待一段时间
      setTimeout(() => {
        updateRoomFileIdsWithRetry(room, fileIds, originalCount, attempt + 1)
      }, 1000 * attempt) // 递增延迟
    } else {
      // 3次重试失败，回滚UI状态
      console.error('[设计稿删除] 更新失败，已达到最大重试次数')
      proxy.$modal.msgError('删除图片失败，请刷新页面重试')

      // 回滚到原始状态
      const roomIndex = rooms.value.findIndex(r => r.id === room.id)
      if (roomIndex > -1) {
        // 重新加载该房间数据
        loadRooms()
      }
    }
  }
}

/** 更新房间文件ID */
async function updateRoomFileIds(room, fileIds) {
  try {
    const updateData = {
      id: room.id,
      fileIds: JSON.stringify(fileIds)
    }

    const res = await retryRequest(() => updateProjectRooms(updateData))

    if (res.code === 200) {
      // 更新本地数据
      const roomIndex = rooms.value.findIndex(r => r.id === room.id)
      if (roomIndex > -1) {
        rooms.value[roomIndex].fileIds = JSON.stringify(fileIds)
      }

      // 通知父组件设计稿已更新
      emit('designs-updated', {
        action: 'update',
        roomId: room.id,
        projectId: props.project.id,
        designCount: fileIds.length
      })
    }
  } catch (error) {
    console.error('更新房间文件ID失败:', error)
    proxy.$modal.msgError('更新图片失败')
  }
}



// 打开添加房间对话框
function handleAddRoom() {
  roomDialogMode.value = 'add'
  roomForm.value = {
    id: '',
    roomType: '',
    roomName: '',
    area: null,
    floor: '',
    orientation: '',
    description: ''
  }
  roomDialogVisible.value = true
}

// 打开编辑房间对话框
function handleEditRoom(room) {
  roomDialogMode.value = 'edit'
  roomForm.value = {
    id: room.id,
    roomType: room.roomType,
    roomName: room.roomName,
    area: room.area,
    floor: room.floor,
    orientation: room.orientation,
    description: room.description || ''
  }
  roomDialogVisible.value = true
}

// 取消
function handleCancel() {
  // 清理上传队列，执行未完成的保存操作
  cleanupUploadQueues()
  dialogVisible.value = false
}

// 取消房间操作
function handleCancelRoom() {
  roomDialogVisible.value = false
  roomFormRef.value?.resetFields()
}

// 确认房间操作
async function handleConfirmRoom() {
  if (!roomFormRef.value) return

  try {
    await roomFormRef.value.validate()

    if (roomDialogMode.value === 'add') {
      // 新增房间
      addingRoom.value = true
      await handleConfirmAddRoom()
    } else {
      // 编辑房间
      editingRoom.value = true
      await handleConfirmEditRoom()
    }
  } catch (error) {
    if (error !== false) { // 表单验证失败会返回false
      proxy.$modal.msgError((roomDialogMode.value === 'add' ? '添加' : '更新') + '房间失败：' + (error.msg || error.message || '未知错误'))
    }
  } finally {
    addingRoom.value = false
    editingRoom.value = false
  }
}

// 添加并继续
async function handleAddAndContinue() {
  if (!roomFormRef.value) return

  try {
    await roomFormRef.value.validate()
    addingRoom.value = true
    await handleConfirmAddRoomContinue()
  } catch (error) {
    if (error !== false) {
      proxy.$modal.msgError('添加房间失败：' + (error.msg || error.message || '未知错误'))
    }
  } finally {
    addingRoom.value = false
  }
}

// 确认添加房间
async function handleConfirmAddRoom() {
  const roomData = {
    projectId: props.project.id,
    roomType: roomForm.value.roomType,
    roomName: roomForm.value.roomName,
    area: roomForm.value.area,
    floor: roomForm.value.floor,
    orientation: roomForm.value.orientation,
    description: roomForm.value.description,
    fileIds: JSON.stringify([])
  }

  const res = await addProjectRooms(roomData)

  if (res.code === 200) {
    proxy.$modal.msgSuccess('房间添加成功')
    roomDialogVisible.value = false
    roomFormRef.value?.resetFields()
    await loadRooms()

    // 通知父组件房间列表已更新
    emit('success', {
      type: 'room-added',
      roomId: res.data?.id,
      roomName: roomForm.value.roomName
    })

    emit('rooms-updated', {
      action: 'add',
      roomId: res.data?.id,
      projectId: props.project.id
    })
  } else {
    throw new Error(res.msg || '添加失败')
  }
}

// 确认添加并继续
async function handleConfirmAddRoomContinue() {
  const roomData = {
    projectId: props.project.id,
    roomType: roomForm.value.roomType,
    roomName: roomForm.value.roomName,
    area: roomForm.value.area,
    floor: roomForm.value.floor,
    orientation: roomForm.value.orientation,
    description: roomForm.value.description,
    fileIds: JSON.stringify([])
  }

  const res = await addProjectRooms(roomData)

  if (res.code === 200) {
    proxy.$modal.msgSuccess('房间添加成功')
    // 重置表单但保持房间类型，以便继续添加同类型房间
    const currentRoomType = roomForm.value.roomType
    const addedRoomName = roomForm.value.roomName
    roomForm.value = {
      id: '',
      roomType: currentRoomType,
      roomName: generateRoomName(currentRoomType),
      area: null,
      floor: '',
      orientation: '',
      description: ''
    }
    await loadRooms()

    // 通知父组件房间列表已更新
    emit('success', {
      type: 'room-added',
      roomId: res.data?.id,
      roomName: addedRoomName
    })

    emit('rooms-updated', {
      action: 'add',
      roomId: res.data?.id,
      projectId: props.project.id
    })
  } else {
    throw new Error(res.msg || '添加失败')
  }
}

// 确认编辑房间
async function handleConfirmEditRoom() {
  const updateData = {
    id: roomForm.value.id,
    projectId: props.project.id,
    roomType: roomForm.value.roomType,
    roomName: roomForm.value.roomName,
    area: roomForm.value.area,
    floor: roomForm.value.floor,
    orientation: roomForm.value.orientation,
    description: roomForm.value.description
  }

  const res = await updateProjectRooms(updateData)

  if (res.code === 200) {
    proxy.$modal.msgSuccess('房间更新成功')
    roomDialogVisible.value = false
    roomFormRef.value?.resetFields()
    await loadRooms()

    // 通知父组件房间列表已更新
    emit('success', {
      type: 'room-updated',
      roomId: roomForm.value.id,
      roomName: roomForm.value.roomName
    })

    emit('rooms-updated', {
      action: 'update',
      roomId: roomForm.value.id,
      projectId: props.project.id
    })
  } else {
    throw new Error(res.msg || '更新失败')
  }
}
async function handleDeleteRoom(roomId) {
  try {
    // 获取要删除的房间信息
    const roomToDelete = rooms.value.find(r => r.id === roomId)
    if (!roomToDelete) {
      proxy.$modal.msgError('房间不存在')
      return
    }

    const imageCount = getRoomImageCount(roomToDelete)

    // 构建确认消息
    let confirmMessage = '确定要删除该房间吗？'
    if (imageCount > 0) {
      confirmMessage += `\n\n⚠️ 该房间包含 ${imageCount} 张设计图，删除后将无法恢复！`
    }
    confirmMessage += '\n\n此操作不可撤销，请谨慎操作。'

    await proxy.$modal.confirm(confirmMessage, '删除房间确认', {
      confirmButtonText: '确认删除',
      cancelButtonText: '取消',
      type: 'warning',
      dangerouslyUseHTMLString: true
    })

    // 检查网络状态
    if (!navigator.onLine) {
      proxy.$modal.msgError('网络连接已断开，无法删除房间')
      return
    }

    const res = await retryRequest(() => delProjectRooms(roomId))

    if (res.code === 200) {
      proxy.$modal.msgSuccess('删除成功')
      await loadRooms()

      // 通知父组件房间列表已更新，包含删除的房间信息用于日志记录
      emit('success', {
        type: 'room-deleted',
        roomId: roomId,
        roomName: roomToDelete.roomName,
        deletedImageCount: imageCount
      })

      emit('rooms-updated', {
        action: 'delete',
        roomId: roomId,
        roomName: roomToDelete.roomName,
        projectId: props.project.id,
        deletedImageCount: imageCount
      })
    } else {
      throw new Error(res.msg || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      proxy.$modal.msgError(error.message || '删除房间失败')
    }
  }
}

// 加载房间列表 - 优化版本，包含更好的错误处理
async function loadRooms() {
  if (!props.project?.id) {
    console.warn('[设计稿管理] 项目ID不存在，无法加载房间列表')
    rooms.value = []
    loadError.value = false
    lastError.value = null
    return
  }

  const projectId = props.project.id
  console.log(`[设计稿管理] 开始加载项目 ${projectId} 的房间列表`)

  // 重置状态
  loadError.value = false
  lastError.value = null

  // 取消之前的请求
  if (currentRequestController) {
    console.log(`[设计稿管理] 取消之前的加载请求: ${currentProjectId}`)
    currentRequestController.abort()
    currentRequestController = null
  }

  try {
    loading.value = true
    currentProjectId = projectId

    // 检查网络状态
    if (!navigator.onLine) {
      const errorMsg = '网络连接已断开，请检查网络后重试'
      loadError.value = true
      lastError.value = { message: errorMsg }
      proxy.$modal.msgError(errorMsg)
      return
    }

    // 创建新的请求控制器
    currentRequestController = new AbortController()

    const res = await retryRequest(() => {
      // 检查请求是否被取消
      if (currentRequestController?.signal.aborted) {
        throw new Error('请求已取消')
      }
      return listProjectRooms({ projectId })
    })

    // 检查请求是否被取消
    if (currentRequestController?.signal.aborted) {
      console.log(`[设计稿管理] 项目 ${projectId} 的加载请求被取消`)
      return
    }

    if (res.code === 200) {
      const loadedRooms = res.rows || res.data || []
      console.log(`[设计稿管理] 成功加载项目 ${projectId} 的 ${loadedRooms.length} 个房间`)

      // 只在当前请求有效时更新数据
      if (currentProjectId === projectId) {
        rooms.value = loadedRooms.map(room => ({
          ...room,
          fileList: room.fileIds ? parseFileIdsToListForRoom(room) : []
        }))

        // 如果确实没有数据（不是错误），显示正常提示
        if (loadedRooms.length === 0) {
          console.log(`项目 ${projectId} 暂无房间数据，这是正常情况`)
        }
      } else {
        console.log(`[设计稿管理] 忽略过期响应，当前项目: ${currentProjectId}, 响应项目: ${projectId}`)
      }
    } else {
      throw new Error(res.msg || '服务器返回错误')
    }
  } catch (error) {
    // 如果是手动取消的错误，不显示错误提示
    if (error.name === 'AbortError' || error.message === '请求已取消') {
      console.log(`[设计稿管理] 项目 ${projectId} 的加载请求被正常取消`)
      return
    }

    console.error(`[设计稿管理] 加载项目 ${projectId} 房间列表失败:`, error)

    // 只在当前请求有效时设置错误状态
    if (currentProjectId === projectId) {
      loadError.value = true
      lastError.value = {
        message: error.message || '加载失败',
        details: error.response?.data?.msg || error.message || '网络请求失败'
      }

      // 显示友好的错误提示
      const errorMsg = `加载房间数据失败：${lastError.value.details}`
      ElMessage({
        message: errorMsg,
        type: 'error',
        duration: 3000,
        showClose: true
      })
    }
  } finally {
    // 只在当前请求有效时重置加载状态
    if (currentProjectId === projectId) {
      loading.value = false
      currentRequestController = null
    }
  }
}


// 查看模板功能
function handleViewTemplate() {
  // 显示房间类型模板提示
  proxy.$alert(
    '房间类型参考：\n\n' +
    '• 客厅 - 家庭活动空间\n' +
    '• 主卧 - 主要卧室\n' +
    '• 次卧 - 次要卧室\n' +
    '• 厨房 - 烹饪空间\n' +
    '• 卫生间 - 卫浴空间\n' +
    '• 书房 - 工作学习空间\n' +
    '• 餐厅 - 用餐空间\n' +
    '• 阳台 - 户外休闲空间\n\n' +
    '每个房间都可以上传设计图、施工图、效果图等文件。',
    '房间类型模板',
    {
      confirmButtonText: '知道了',
      type: 'info'
    }
  )
}


// 组件卸载时清理资源
onUnmounted(() => {
  console.log('[设计稿管理] 组件卸载，清理资源')
  cleanupUploadQueues()
})

// 监听项目ID变化，重置状态
watch(() => props.project?.id, (newProjectId, oldProjectId) => {
  if (newProjectId !== oldProjectId) {
    console.log(`[设计稿管理] 项目切换: ${oldProjectId} -> ${newProjectId}`)

    // 完全重置组件状态，防止数据混淆
    rooms.value = []
    loading.value = false
    uploadingRoomId.value = null
    addingRoom.value = false
    editingRoom.value = false
    roomDialogVisible.value = false

    // 重置表单
    if (roomFormRef.value) {
      roomFormRef.value.resetFields()
    }

    roomForm.value = {
      id: '',
      roomType: '',
      roomName: '',
      area: null,
      floor: '',
      orientation: '',
      description: ''
    }
  }
}, { immediate: true })

// 监听对话框打开，加载数据
watch(() => props.modelValue, (val) => {
  if (val && props.project?.id) {
    console.log(`[设计稿管理] 打开项目 ${props.project.id} 的设计稿管理`)
    loadRooms()
  }
}, { immediate: true })
</script>

<style scoped lang="scss">
.project-design-drafts-dialog {
  :deep(.el-dialog__body) {
    padding: 20px;
  }
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.vue-viewer {
  display: contents; // 让子元素直接参与grid布局
}

.image-item {
  position: relative;
  border-radius: 4px;
  overflow: hidden;
  border: 1px solid #e8e8e8;
  background: #f5f5f5;
  transition: all 0.3s ease;

  img {
    width: 100%;
    height: 100px;
    object-fit: cover;
    transition: all 0.3s ease;
  }

  &:hover {
    border-color: #1677ff;
    box-shadow: 0 2px 8px rgba(64, 158, 255, 0.2);
    transform: translateY(-1px);

    img {
      transform: scale(1.05);
    }

    .image-overlay {
      opacity: 1;
    }
  }
}

.image-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
  pointer-events: none; // 让点击事件穿透到下层图片

  .el-button {
    margin: 0 4px;
    pointer-events: auto; // 按钮需要响应点击事件
  }
}

// 新增的空状态和错误状态样式
.room-list-empty {
  padding: 40px 20px;
}

.empty-state {
  text-align: center;

  .empty-description {
    font-size: 16px;
    color: #606266;
    margin: 16px 0 8px 0;
  }

  .empty-tip {
    font-size: 14px;
    color: #909399;
    margin: 0 0 24px 0;
  }
}

.error-state {
  padding: 40px 20px;
}

.quick-actions {
  margin-top: 24px;

  .divider-text {
    color: #909399;
    font-size: 14px;
  }

  .action-buttons {
    display: flex;
    justify-content: center;
    gap: 12px;
    margin-top: 16px;
  }
}

// 骨架屏加载状态
.loading-state {
  padding: 20px;

  .skeleton-room {
    padding: 16px;
    border: 1px solid #ebeef5;
    border-radius: 8px;
    margin-bottom: 16px;
  }
}

// 新增：房间导航和视图控制样式
.room-navigation {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 16px;
  border: 1px solid #e9ecef;

  .nav-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 12px;

    .nav-left {
      .room-stats {
        display: flex;
        align-items: center;
        gap: 8px;
      }
    }

    .nav-right {
      display: flex;
      align-items: center;
    }
  }

  .room-tabs-nav {
    .room-tabs {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 8px 0;

      .room-tab {
        position: relative;
        padding: 6px 12px;
        background: #fff;
        border: 1px solid #e0e0e0;
        border-radius: 16px;
        cursor: pointer;
        transition: all 0.2s ease;
        font-size: 13px;
        color: #666;
        display: flex;
        align-items: center;
        gap: 4px;

        &:hover {
          background: #f5f5f5;
          border-color: #d0d0d0;
          color: #333;
        }

        &.active {
          background: #1677ff;
          border-color: #1677ff;
          color: #fff;
        }
      }
    }
  }
}


// 手风琴折叠视图样式
.rooms-accordion {
  .accordion-title {
    display: flex;
    align-items: center;
    justify-content: space-between;
    width: 100%;
    padding-right: 8px;

    .title-left {
      display: flex;
      align-items: center;
      gap: 8px;
      flex: 1;
      flex-wrap: wrap;

      .room-name {
        font-weight: 600;
        color: #333;
        font-size: 15px;
      }
    }

    .title-right {
      display: flex;
      align-items: center;
      gap: 4px;
      opacity: 0;
      transition: opacity 0.2s ease;

      &:hover {
        opacity: 1;
      }
    }

    &:hover .title-right {
      opacity: 1;
    }
  }

  .accordion-content {
    padding: 16px 0;
  }

  .room-description {
    margin-bottom: 16px;
    padding: 12px 16px;
    background: #f8f9fa;
    border-left: 3px solid #409EFF;
    border-radius: 4px;
    font-size: 14px;
    line-height: 1.6;

    .room-description-label {
      font-weight: 600;
      color: #303133;
      margin-right: 8px;
    }

    .room-description-text {
      color: #606266;
      display: inline;
      word-break: break-all;
      overflow: hidden;
      text-overflow: ellipsis;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .room-navigation {
    .nav-header {
      flex-direction: column;
      align-items: stretch;
      gap: 12px;

      .nav-left {
        .room-stats {
          justify-content: center;
        }
      }

      .nav-right {
        justify-content: space-between;

        .el-input {
          flex: 1;
          margin-right: 8px !important;
        }
      }
    }
  }

  
  .room-tabs-nav {
    .room-tabs {
      flex-wrap: wrap;
      justify-content: center;
    }
  }

  .room-detail-info {
    :deep(.el-descriptions) {
      .el-descriptions__cell {
        padding: 10px 12px;
      }

      .el-descriptions__label {
        width: 70px;
        font-size: 13px;
      }

      .el-descriptions__content {
        font-size: 13px;
      }
    }
  }
}

@media (max-width: 480px) {
  .room-navigation {
    padding: 12px;
  }

  .room-detail-info {
    :deep(.el-descriptions) {
      .el-descriptions__cell {
        padding: 8px 12px;
      }

      .el-descriptions__label {
        width: 60px;
        font-size: 12px;
      }

      .el-descriptions__content {
        font-size: 12px;
      }
    }
  }
}
  </style>

