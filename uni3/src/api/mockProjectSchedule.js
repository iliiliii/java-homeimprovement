/**
 * 项目进度模拟数据 - 用于开发测试
 */

// 模拟延迟
const delay = (ms = 500) => new Promise(resolve => setTimeout(resolve, ms))

// 模拟项目进度数据
const mockSchedules = [
  {
    id: 'schedule_001',
    projectId: 'project_001',
    stage: 'DEMOLITION',
    stageName: '拆除工程',
    stageOrder: 1,
    planStartDate: '2024-01-01T08:00:00',
    planEndDate: '2024-01-05T18:00:00',
    actualStartDate: '2024-01-01T08:30:00',
    actualEndDate: '2024-01-05T17:30:00',
    status: 'COMPLETED',
    statusText: '已完成',
    completionRate: 100.00,
    description: '拆除原有装修，清理现场',
    recordCount: 2,
    latestRecords: [
      {
        id: 'record_001',
        scheduleId: 'schedule_001',
        projectId: 'project_001',
        stage: 'DEMOLITION',
        stageName: '拆除工程',
        title: '拆除工程完成验收',
        description: '拆除工作已全部完成，现场清理干净，符合下一阶段施工要求',
        type: 'INSPECTION',
        typeText: '验收',
        createTime: '2024-01-05T17:30:00',
        createBy: 'staff_001',
        createByName: '张师傅',
        createByRole: 'STAFF',
        inspectionStatus: 'PASS',
        inspectionStatusText: '通过',
        images: [
          'https://images.unsplash.com/photo-1581094794329-c8112a89af12?ixlib=rb-4.0.3&auto=format&fit=crop&w=400&q=80',
          'https://images.unsplash.com/photo-1621905251189-08b45d6a269e?ixlib=rb-4.0.3&auto=format&fit=crop&w=400&q=80'
        ]
      },
      {
        id: 'record_002',
        scheduleId: 'schedule_001',
        projectId: 'project_001',
        stage: 'DEMOLITION',
        stageName: '拆除工程',
        title: '拆除垃圾清运',
        description: '拆除产生的建筑垃圾已全部清运完毕',
        type: 'PROGRESS',
        typeText: '进度',
        createTime: '2024-01-05T16:00:00',
        createBy: 'staff_001',
        createByName: '张师傅',
        createByRole: 'STAFF',
        inspectionStatus: null,
        inspectionStatusText: '',
        images: []
      }
    ]
  },
  {
    id: 'schedule_002',
    projectId: 'project_001',
    stage: 'HYDROELECTRIC',
    stageName: '水电改造',
    stageOrder: 2,
    planStartDate: '2024-01-06T08:00:00',
    planEndDate: '2024-01-15T18:00:00',
    actualStartDate: '2024-01-06T08:00:00',
    actualEndDate: null,
    status: 'IN_PROGRESS',
    statusText: '进行中',
    completionRate: 75.00,
    description: '水电线路改造，强弱电布线',
    recordCount: 4,
    latestRecords: [
      {
        id: 'record_006',
        scheduleId: 'schedule_002',
        projectId: 'project_001',
        stage: 'HYDROELECTRIC',
        stageName: '水电改造',
        title: '水管打压测试',
        description: '水管安装完成，进行8kg压力测试，保压30分钟无掉压',
        type: 'INSPECTION',
        typeText: '验收',
        createTime: '2024-01-12T14:00:00',
        createBy: 'staff_002',
        createByName: '李师傅',
        createByRole: 'STAFF',
        inspectionStatus: 'PASS',
        inspectionStatusText: '通过',
        images: [
          'https://images.unsplash.com/photo-1504307651254-35680f356dfd?ixlib=rb-4.0.3&auto=format&fit=crop&w=400&q=80'
        ]
      },
      {
        id: 'record_005',
        scheduleId: 'schedule_002',
        projectId: 'project_001',
        stage: 'HYDROELECTRIC',
        stageName: '水电改造',
        title: '弱电线路布置',
        description: '网络线、电视线已完成布置，预留充足长度',
        type: 'PROGRESS',
        typeText: '进度',
        createTime: '2024-01-10T15:00:00',
        createBy: 'staff_002',
        createByName: '李师傅',
        createByRole: 'STAFF',
        inspectionStatus: null,
        inspectionStatusText: '',
        images: []
      }
    ]
  },
  {
    id: 'schedule_003',
    projectId: 'project_001',
    stage: 'MASONRY',
    stageName: '泥瓦工程',
    stageOrder: 3,
    planStartDate: '2024-01-16T08:00:00',
    planEndDate: '2024-01-25T18:00:00',
    actualStartDate: null,
    actualEndDate: null,
    status: 'PENDING',
    statusText: '待开始',
    completionRate: 0.00,
    description: '地面找平，墙面处理，瓷砖铺贴',
    recordCount: 0,
    latestRecords: []
  },
  {
    id: 'schedule_004',
    projectId: 'project_001',
    stage: 'CARPENTRY',
    stageName: '木工工程',
    stageOrder: 4,
    planStartDate: '2024-01-26T08:00:00',
    planEndDate: '2024-02-05T18:00:00',
    actualStartDate: null,
    actualEndDate: null,
    status: 'PENDING',
    statusText: '待开始',
    completionRate: 0.00,
    description: '吊顶制作，柜体安装',
    recordCount: 0,
    latestRecords: []
  },
  {
    id: 'schedule_005',
    projectId: 'project_001',
    stage: 'PAINTING',
    stageName: '油漆工程',
    stageOrder: 5,
    planStartDate: '2024-02-06T08:00:00',
    planEndDate: '2024-02-15T18:00:00',
    actualStartDate: null,
    actualEndDate: null,
    status: 'PENDING',
    statusText: '待开始',
    completionRate: 0.00,
    description: '墙面刷漆，木作油漆',
    recordCount: 0,
    latestRecords: []
  }
]

// 模拟验收记录数据
const mockRecords = [
  {
    id: 'record_001',
    scheduleId: 'schedule_001',
    projectId: 'project_001',
    stage: 'DEMOLITION',
    stageName: '拆除工程',
    title: '拆除工程完成验收',
    description: '拆除工作已全部完成，现场清理干净，符合下一阶段施工要求。所有原有装修材料已清理完毕，墙体结构完好，为下一阶段水电改造做好准备。',
    type: 'INSPECTION',
    typeText: '验收',
    createTime: '2024-01-05T17:30:00',
    createBy: 'staff_001',
    createByName: '张师傅',
    createByRole: 'STAFF',
    inspectionStatus: 'PASS',
    inspectionStatusText: '通过',
    images: [
      'https://images.unsplash.com/photo-1581094794329-c8112a89af12?ixlib=rb-4.0.3&auto=format&fit=crop&w=400&q=80',
      'https://images.unsplash.com/photo-1621905251189-08b45d6a269e?ixlib=rb-4.0.3&auto=format&fit=crop&w=400&q=80',
      'https://images.unsplash.com/photo-1503387762-592deb58ef4e?ixlib=rb-4.0.3&auto=format&fit=crop&w=400&q=80'
    ],
    attachments: [
      {
        fileName: '拆除验收报告.pdf',
        fileUrl: '/uploads/files/demolition_report.pdf',
        fileSize: 524288,
        fileType: 'PDF'
      }
    ],
    remark: '业主现场确认验收通过'
  },
  {
    id: 'record_006',
    scheduleId: 'schedule_002',
    projectId: 'project_001',
    stage: 'HYDROELECTRIC',
    stageName: '水电改造',
    title: '水管打压测试',
    description: '水管安装完成，进行8kg压力测试，保压30分钟无掉压。所有接头牢固，管路走向合理，符合施工规范要求。',
    type: 'INSPECTION',
    typeText: '验收',
    createTime: '2024-01-12T14:00:00',
    createBy: 'staff_002',
    createByName: '李师傅',
    createByRole: 'STAFF',
    inspectionStatus: 'PASS',
    inspectionStatusText: '通过',
    images: [
      'https://images.unsplash.com/photo-1504307651254-35680f356dfd?ixlib=rb-4.0.3&auto=format&fit=crop&w=400&q=80',
      'https://images.unsplash.com/photo-1581094288338-2314dddb7ece?ixlib=rb-4.0.3&auto=format&fit=crop&w=400&q=80'
    ],
    attachments: [
      {
        fileName: '打压测试报告.pdf',
        fileUrl: '/uploads/files/pressure_test_report.pdf',
        fileSize: 256789,
        fileType: 'PDF'
      }
    ],
    remark: '测试结果符合标准'
  }
]

/**
 * 获取项目进度列表
 */
export const getMockProjectScheduleList = async () => {
  await delay()
  return mockSchedules
}

/**
 * 获取项目进度验收记录列表
 */
export const getMockProjectScheduleRecordList = async (params = {}) => {
  await delay()
  
  let filteredRecords = [...mockRecords]
  
  // 按进度ID筛选
  if (params.scheduleId) {
    filteredRecords = filteredRecords.filter(record => record.scheduleId === params.scheduleId)
  }
  
  // 分页处理
  const page = params.page || 1
  const pageSize = params.pageSize || 20
  const start = (page - 1) * pageSize
  const end = start + pageSize
  
  return {
    rows: filteredRecords.slice(start, end),
    total: filteredRecords.length
  }
}

/**
 * 获取进度验收记录详情
 */
export const getMockProjectScheduleRecordDetail = async (recordId) => {
  await delay()
  
  const record = mockRecords.find(r => r.id === recordId)
  if (!record) {
    throw new Error('记录不存在')
  }
  
  return record
}