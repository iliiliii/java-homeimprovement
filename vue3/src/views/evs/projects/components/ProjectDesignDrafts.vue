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

      <!-- 房间列表 -->
      <div v-if="rooms.length === 0" style="text-align: center; padding: 60px 0; color: #999;">
        <el-icon style="font-size: 48px; margin-bottom: 16px;"><FolderOpened /></el-icon>
        <div>暂无房间，请点击"添加房间"开始管理设计稿</div>
      </div>

      <!-- 房间卡片 -->
      <div v-else>
        <div
          v-for="room in rooms"
          :key="room.id"
          style="margin-bottom: 24px; border: 1px solid #e8e8e8; border-radius: 8px; padding: 16px; background: #fff;"
        >
          <!-- 房间头部 -->
          <div style="display: flex; align-items: center; justify-content: space-between; margin-bottom: 16px; padding-bottom: 12px; border-bottom: 1px solid #f0f0f0;">
            <div style="display: flex; align-items: center; gap: 12px; flex: 1;">
              <el-icon style="font-size: 20px; color: #1677ff;"><Folder /></el-icon>
              <span style="font-size: 16px; font-weight: 600;">{{ room.roomName }}</span>
              <span style="font-size: 13px; color: #999;">
                {{ getRoomImageCount(room) }}张图片
              </span>
            </div>
            <el-button
              type="danger"
              link
              size="small"
              @click="handleDeleteRoom(room.id)"
              :icon="Delete"
            >
              删除房间
            </el-button>
          </div>

          <!-- 房间信息展示 -->
          <div style="margin-bottom: 16px; padding: 12px; background: #fafafa; border-radius: 4px; font-size: 13px;">
            <el-row :gutter="16">
              <el-col :span="6" v-if="room.roomType">
                <span style="color: #999;">房间类型：</span>
                <dict-tag :options="decoration_room_type" :value="room.roomType" />
              </el-col>
              <el-col :span="6" v-if="room.area">
                <span style="color: #999;">面积：</span>
                <span>{{ room.area }}㎡</span>
              </el-col>
              <el-col :span="6" v-if="room.floor">
                <span style="color: #999;">楼层：</span>
                <span>{{ room.floor }}</span>
              </el-col>
              <el-col :span="6" v-if="room.orientation">
                <span style="color: #999;">朝向：</span>
                <span>{{ room.orientation }}</span>
              </el-col>
            </el-row>
            <div v-if="room.description" style="margin-top: 8px; color: #666;">
              <span style="color: #999;">描述：</span>
              {{ room.description }}
            </div>
          </div>

          <!-- 设计稿上传区域 -->
          <div>
            <div v-if="getRoomImageCount(room) === 0" style="text-align: center; padding: 40px 0; border: 1px dashed #d9d9d9; border-radius: 4px; background: #fafafa;">
              <el-icon style="font-size: 48px; color: #d9d9d9; margin-bottom: 12px;"><Picture /></el-icon>
              <div style="color: #999; margin-bottom: 16px;">暂无{{ room.roomName }}的设计图</div>
              <ImageUpload
                :model-value="getRoomFileIds(room)"
                @update:model-value="(val) => handleImageUpload(room, val)"
                :limit="20"
                :compress="true"
                :compress-quality="0.6"
                :compress-max-size="2"
                :compress-max-width-or-height="1920"
                action="/common/upload"
              />
            </div>
            <div v-else>
              <ImageUpload
                :model-value="getRoomFileIds(room)"
                @update:model-value="(val) => handleImageUpload(room, val)"
                :limit="20"
                :compress="true"
                :compress-quality="0.6"
                :compress-max-size="2"
                :compress-max-width-or-height="1920"
                action="/common/upload"
              />
            </div>
          </div>
        </div>
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleCancel">关 闭</el-button>
      </div>
    </template>

    <!-- 添加房间对话框 -->
    <el-dialog
      v-model="addRoomDialogVisible"
      title="添加房间"
      width="800px"
      append-to-body
      :close-on-click-modal="false"
    >
      <el-form
        ref="addRoomFormRef"
        :model="addRoomForm"
        :rules="addRoomRules"
        label-width="100px"
      >
        <el-form-item label="房间类型" prop="roomType" required>
          <el-select
            v-model="addRoomForm.roomType"
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
            v-model="addRoomForm.roomName"
            placeholder="请输入房间名称，或根据房间类型自动生成"
            style="width: 100%"
          />
        </el-form-item>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="房间面积">
              <el-input-number
                v-model="addRoomForm.area"
                placeholder="平方米"
                :min="0"
                :precision="2"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="房间楼层">
              <el-input
                v-model="addRoomForm.floor"
                placeholder="如：1楼、2楼"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="房间朝向">
          <el-select
            v-model="addRoomForm.orientation"
            placeholder="请选择朝向"
            style="width: 100%"
          >
            <el-option label="北" value="N" />
            <el-option label="南" value="S" />
            <el-option label="东" value="E" />
            <el-option label="西" value="W" />
            <el-option label="东北" value="NE" />
            <el-option label="西北" value="NW" />
            <el-option label="东南" value="SE" />
            <el-option label="西南" value="SW" />
          </el-select>
        </el-form-item>

        <el-form-item label="房间描述">
          <el-input
            v-model="addRoomForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入房间描述（可选）"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="handleCancelAddRoom">取 消</el-button>
          <el-button type="primary" @click="handleConfirmAddRoom" :loading="addingRoom">
            确认添加
          </el-button>
          <el-button type="success" @click="handleAddAndContinue" :loading="addingRoom">
            添加并继续
          </el-button>
        </div>
      </template>
    </el-dialog>
  </el-dialog>
</template>

<script setup>
import { Picture, Plus, Delete, Folder, FolderOpened, InfoFilled } from '@element-plus/icons-vue'
import { listProjectRooms, addProjectRooms, updateProjectRooms, delProjectRooms } from '@/api/evs/projectRooms'
import ImageUpload from '@/components/ImageUpload/index.vue'

const { proxy } = getCurrentInstance()
const { decoration_room_type } = proxy.useDict('decoration_room_type')

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

const emit = defineEmits(['update:modelValue', 'success'])

// 响应式数据
const dialogVisible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const rooms = ref([])
const loading = ref(false)
const addingRoom = ref(false)
const addRoomDialogVisible = ref(false)
const addRoomFormRef = ref(null)

// 添加房间表单
const addRoomForm = ref({
  roomType: '',
  roomName: '',
  area: null,
  floor: '',
  orientation: '',
  description: ''
})

// 表单验证规则
const addRoomRules = {
  roomType: [
    { required: true, message: '请选择房间类型', trigger: 'change' }
  ],
  roomName: [
    { required: true, message: '请输入房间名称', trigger: 'blur' }
  ]
}

// 生成房间名称
function generateRoomName(roomType) {
  if (!roomType) return ''
  
  const typeLabel = decoration_room_type.value.find(item => item.value === roomType)?.label || roomType
  const sameTypeRooms = rooms.value.filter(r => r.roomType === roomType)
  const count = sameTypeRooms.length + 1
  
  return `${typeLabel}-${count}`
}

// 房间类型改变时自动生成名称
function handleRoomTypeChange() {
  if (!addRoomForm.value.roomName || addRoomForm.value.roomName.match(/^.+-\d+$/)) {
    addRoomForm.value.roomName = generateRoomName(addRoomForm.value.roomType)
  }
}

// 获取房间图片数量
function getRoomImageCount(room) {
  if (!room.fileIds) return 0
  try {
    const ids = typeof room.fileIds === 'string' ? JSON.parse(room.fileIds) : room.fileIds
    return Array.isArray(ids) ? ids.length : 0
  } catch {
    return 0
  }
}

// 获取房间文件ID列表（用于ImageUpload组件）
function getRoomFileIds(room) {
  if (!room.fileIds) return ''
  try {
    const ids = typeof room.fileIds === 'string' ? JSON.parse(room.fileIds) : room.fileIds
    if (Array.isArray(ids) && ids.length > 0) {
      const baseUrl = import.meta.env.VITE_APP_BASE_API
      // ImageUpload组件期望的是逗号分隔的URL字符串
      return ids.map(id => {
        // 如果id已经是完整URL，直接返回；否则拼接baseUrl
        if (id.startsWith('http://') || id.startsWith('https://')) {
          return id
        }
        // 如果id以/开头，直接拼接；否则添加/
        return baseUrl + (id.startsWith('/') ? id : '/' + id)
      }).join(',')
    }
  } catch (e) {
    console.error('解析文件ID失败:', e)
  }
  return ''
}

// 处理图片上传
async function handleImageUpload(room, fileIdsString) {
  try {
    // ImageUpload组件返回的是逗号分隔的文件路径字符串（可能包含baseUrl）
    const baseUrl = import.meta.env.VITE_APP_BASE_API
    let fileIds = []
    
    if (fileIdsString) {
      const urls = fileIdsString.split(',').map(url => url.trim()).filter(url => url)
      fileIds = urls.map(url => {
        // 移除baseUrl前缀，只保留文件路径（以/开头）
        if (url.startsWith(baseUrl)) {
          const path = url.substring(baseUrl.length)
          return path.startsWith('/') ? path : '/' + path
        }
        // 如果已经是相对路径，确保以/开头
        if (url.startsWith('/')) {
          return url
        }
        // 否则添加/
        return '/' + url
      }).filter(id => id)
    }

    // 更新房间的fileIds字段（JSON格式）
    const updateData = {
      id: room.id,
      fileIds: JSON.stringify(fileIds)
    }

    const res = await updateProjectRooms(updateData)
    
    if (res.code === 200) {
      // 更新本地数据
      const roomIndex = rooms.value.findIndex(r => r.id === room.id)
      if (roomIndex > -1) {
        rooms.value[roomIndex].fileIds = JSON.stringify(fileIds)
      }
      proxy.$modal.msgSuccess('设计稿保存成功')
    } else {
      throw new Error(res.msg || '保存失败')
    }
  } catch (error) {
    console.error('保存设计稿失败:', error)
    proxy.$modal.msgError('保存设计稿失败：' + (error.msg || error.message || '未知错误'))
  }
}

// 打开添加房间对话框
function handleAddRoom() {
  addRoomForm.value = {
    roomType: '',
    roomName: '',
    area: null,
    floor: '',
    orientation: '',
    description: ''
  }
  addRoomDialogVisible.value = true
}

// 取消添加房间
function handleCancelAddRoom() {
  addRoomDialogVisible.value = false
  addRoomFormRef.value?.resetFields()
}

// 确认添加房间
async function handleConfirmAddRoom() {
  if (!addRoomFormRef.value) return
  
  try {
    await addRoomFormRef.value.validate()
    
    addingRoom.value = true
    
    const roomData = {
      projectId: props.project.id,
      roomType: addRoomForm.value.roomType,
      roomName: addRoomForm.value.roomName,
      area: addRoomForm.value.area,
      floor: addRoomForm.value.floor,
      orientation: addRoomForm.value.orientation,
      description: addRoomForm.value.description,
      fileIds: JSON.stringify([])
    }

    const res = await addProjectRooms(roomData)
    
    if (res.code === 200) {
      proxy.$modal.msgSuccess('房间添加成功')
      addRoomDialogVisible.value = false
      addRoomFormRef.value?.resetFields()
      await loadRooms()
      emit('success')
    } else {
      throw new Error(res.msg || '添加失败')
    }
  } catch (error) {
    if (error !== false) { // 表单验证失败会返回false
      proxy.$modal.msgError('添加房间失败：' + (error.msg || error.message || '未知错误'))
    }
  } finally {
    addingRoom.value = false
  }
}

// 添加并继续
async function handleAddAndContinue() {
  if (!addRoomFormRef.value) return
  
  try {
    await addRoomFormRef.value.validate()
    
    addingRoom.value = true
    
    const roomData = {
      projectId: props.project.id,
      roomType: addRoomForm.value.roomType,
      roomName: addRoomForm.value.roomName,
      area: addRoomForm.value.area,
      floor: addRoomForm.value.floor,
      orientation: addRoomForm.value.orientation,
      description: addRoomForm.value.description,
      fileIds: JSON.stringify([])
    }

    const res = await addProjectRooms(roomData)
    
    if (res.code === 200) {
      proxy.$modal.msgSuccess('房间添加成功')
      // 重置表单但保持房间类型，以便继续添加同类型房间
      const currentRoomType = addRoomForm.value.roomType
      addRoomForm.value = {
        roomType: currentRoomType,
        roomName: generateRoomName(currentRoomType),
        area: null,
        floor: '',
        orientation: '',
        description: ''
      }
      await loadRooms()
      emit('success')
    } else {
      throw new Error(res.msg || '添加失败')
    }
  } catch (error) {
    if (error !== false) {
      proxy.$modal.msgError('添加房间失败：' + (error.msg || error.message || '未知错误'))
    }
  } finally {
    addingRoom.value = false
  }
}

// 删除房间
async function handleDeleteRoom(roomId) {
  try {
    await proxy.$modal.confirm('确定要删除该房间吗？删除后该房间的所有设计稿也将被删除。')
    
    const res = await delProjectRooms(roomId)
    
    if (res.code === 200) {
      proxy.$modal.msgSuccess('删除成功')
      await loadRooms()
      emit('success')
    } else {
      throw new Error(res.msg || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      proxy.$modal.msgError('删除房间失败：' + (error.msg || error.message || '未知错误'))
    }
  }
}

// 加载房间列表
async function loadRooms() {
  if (!props.project?.id) return
  
  try {
    loading.value = true
    const res = await listProjectRooms({ projectId: props.project.id })
    
    if (res.code === 200) {
      rooms.value = res.rows || res.data || []
    } else {
      throw new Error(res.msg || '加载失败')
    }
  } catch (error) {
    console.error('加载房间列表失败:', error)
    proxy.$modal.msgError('加载房间列表失败：' + (error.msg || error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

// 取消
function handleCancel() {
  dialogVisible.value = false
}

// 监听对话框打开，加载数据
watch(() => props.modelValue, (val) => {
  if (val && props.project?.id) {
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
</style>

