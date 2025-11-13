// 模拟数据

export interface Customer {
  id: string;
  name: string;
  phone: string;
  email: string;
  address: string;
  createdAt: string;
}

export interface Project {
  id: string;
  siteName: string;
  projectName: string;
  siteAddress: string;
  customerId: string;
  customerName: string;
  status: 'planning' | 'inProgress' | 'completed' | 'suspended';
  startDate: string;
  endDate: string;
  totalBudget: number;
  expenses: {
    category: string;
    amount: number;
  }[];
  timeline: {
    date: string;
    title: string;
    description: string;
    status: 'completed' | 'inProgress' | 'pending';
    inspections?: InspectionRecord[];
  }[];
  team?: {
    designers: string[];
    projectManagers: string[];
    foremen: string[];
    supervisors: string[];
  };
}

// 验收记录接口
export interface InspectionRecord {
  id: string;
  title: string;
  content: string;
  images: string[];
  isPassed: boolean;
  inspectionTime: string;
  inspector: string;
}

// 质量问题接口
export interface QualityIssue {
  id: string;
  title: string;
  description: string;
  images: string[];
  reportTime: string;
  reporter: string;
  status: 'pending' | 'resolved';
  resolution?: {
    description: string;
    images: string[];
    resolveTime: string;
    resolver: string;
  };
}

export interface QualityCheck {
  id: string;
  projectId: string;
  projectName: string;
  checkDate: string;
  inspector: string;
  category: string;
  status: 'passed' | 'failed' | 'pending';
  issues: QualityIssue[];
  inspections: InspectionRecord[];
}

// 设计稿图片接口
export interface DesignImage {
  id: string;
  url: string;
  name: string;
  uploadTime: string;
  uploader: string;
  version: number;
}

// 房间设计稿接口
export interface RoomDesign {
  id: string;
  roomName: string;
  images: DesignImage[];
  versionHistory?: DesignImage[];  // 历史版本
}

// 项目设计稿接口
export interface ProjectDesign {
  projectId: string;
  rooms: RoomDesign[];  // 按房间分类
}

export const mockCustomers: Customer[] = [
  {
    id: '1',
    name: '张先生',
    phone: '138-0000-0001',
    email: 'zhang@example.com',
    address: '北京市朝阳区建国路88号',
    createdAt: '2024-01-15',
  },
  {
    id: '2',
    name: '李女士',
    phone: '139-0000-0002',
    email: 'li@example.com',
    address: '上海市浦东新区世纪大道200号',
    createdAt: '2024-02-20',
  },
  {
    id: '3',
    name: '王总',
    phone: '136-0000-0003',
    email: 'wang@example.com',
    address: '深圳市南山区科技园北区',
    createdAt: '2024-03-10',
  },
];

export const mockProjects: Project[] = [
  {
    id: '1',
    siteName: '阳光花园工地',
    projectName: '现代简约三居室装修',
    siteAddress: '北京市朝阳区建国路88号2单元1201',
    customerId: '1',
    customerName: '张先生',
    status: 'inProgress',
    startDate: '2024-03-01',
    endDate: '2024-06-30',
    totalBudget: 280000,
    expenses: [
      { category: '水电改造', amount: 35000 },
      { category: '泥瓦工程', amount: 42000 },
      { category: '木工工程', amount: 55000 },
      { category: '油漆工程', amount: 28000 },
      { category: '主材采购', amount: 85000 },
      { category: '软装配饰', amount: 35000 },
    ],
    timeline: [
      {
        date: '2024-03-01',
        title: '开工准备',
        description: '现场测量、方案确认、材料准备',
        status: 'completed',
      },
      {
        date: '2024-03-10',
        title: '拆除工程',
        description: '拆除旧墙体、拆除旧地板',
        status: 'completed',
      },
      {
        date: '2024-03-20',
        title: '水电改造',
        description: '水电管道铺设、开关插座定位',
        status: 'completed',
      },
      {
        date: '2024-04-15',
        title: '泥瓦工程',
        description: '墙面找平、地面铺贴',
        status: 'inProgress',
      },
      {
        date: '2024-05-10',
        title: '木工工程',
        description: '吊顶、柜体制作',
        status: 'pending',
      },
      {
        date: '2024-06-01',
        title: '油漆工程',
        description: '墙面乳胶漆、木器漆',
        status: 'pending',
      },
      {
        date: '2024-06-20',
        title: '安装收尾',
        description: '灯具安装、五金安装、保洁',
        status: 'pending',
      },
    ],
  },
  {
    id: '2',
    siteName: '海景公寓工地',
    projectName: '北欧风格复式装修',
    siteAddress: '上海市浦东新区世纪大道200号5栋601',
    customerId: '2',
    customerName: '李女士',
    status: 'planning',
    startDate: '2024-05-01',
    endDate: '2024-09-30',
    totalBudget: 450000,
    expenses: [
      { category: '设计费用', amount: 25000 },
      { category: '水电改造', amount: 65000 },
      { category: '泥瓦工程', amount: 78000 },
      { category: '木工工程', amount: 95000 },
      { category: '油漆工程', amount: 42000 },
      { category: '主材采购', amount: 120000 },
      { category: '软装配饰', amount: 25000 },
    ],
    timeline: [
      {
        date: '2024-05-01',
        title: '方案设计',
        description: '平面布局、效果图设计',
        status: 'pending',
      },
    ],
  },
  {
    id: '3',
    siteName: '科技园办公室工地',
    projectName: '现代办公空间装修',
    siteAddress: '深圳市南山区科技园北区A座8楼',
    customerId: '3',
    customerName: '王总',
    status: 'completed',
    startDate: '2023-12-01',
    endDate: '2024-03-15',
    totalBudget: 580000,
    expenses: [
      { category: '水电改造', amount: 85000 },
      { category: '泥瓦工程', amount: 95000 },
      { category: '木工工程', amount: 120000 },
      { category: '油漆工程', amount: 55000 },
      { category: '主材采购', amount: 180000 },
      { category: '办公家具', amount: 45000 },
    ],
    timeline: [
      {
        date: '2023-12-01',
        title: '项目启动',
        description: '完成所有前期准备工作',
        status: 'completed',
      },
      {
        date: '2024-01-15',
        title: '主体施工',
        description: '完成所有主体施工',
        status: 'completed',
      },
      {
        date: '2024-03-15',
        title: '项目验收',
        description: '客户验收通过',
        status: 'completed',
      },
    ],
  },
];

export const mockQualityChecks: QualityCheck[] = [
  {
    id: '1',
    projectId: '1',
    projectName: '现代简约三居室装修',
    checkDate: '2024-04-10',
    inspector: '质检员-刘工',
    category: '水电工程',
    status: 'passed',
    issues: [],
    inspections: [
      {
        id: 'ins-1',
        title: '水电管路验收',
        content: '所有水电管路布置符合规范，接头牢固',
        images: [],
        isPassed: true,
        inspectionTime: '2024-04-10 14:30',
        inspector: '质检员-刘工',
      },
    ],
  },
  {
    id: '2',
    projectId: '1',
    projectName: '现代简约三居室装修',
    checkDate: '2024-04-20',
    inspector: '质检员-陈工',
    category: '泥瓦工程',
    status: 'failed',
    issues: [
      {
        id: 'issue-1',
        title: '墙面平整度问题',
        description: '客厅北墙面平整度偏差超过3mm，不符合验收标准',
        images: [],
        reportTime: '2024-04-20 10:00',
        reporter: '质检员-陈工',
        status: 'pending',
      },
      {
        id: 'issue-2',
        title: '地砖空鼓',
        description: '卧室地砖存在多处空鼓现象',
        images: [],
        reportTime: '2024-04-20 11:30',
        reporter: '质检员-陈工',
        status: 'pending',
      },
    ],
    inspections: [],
  },
];

export const mockCategories = [
  '水电改造',
  '泥瓦工程',
  '木工工程',
  '油漆工程',
  '主材采购',
  '软装配饰',
  '设计费用',
];

export const mockNews = [
  {
    id: '1',
    title: '春季装修优惠活动开始啦！',
    content: '全场主材8折起，预约设计免费出3D效果图',
    publishDate: '2024-03-01',
    status: 'published',
  },
  {
    id: '2',
    title: '环保材料新品上市',
    content: '全新环保乳胶漆系列，零甲醛更健康',
    publishDate: '2024-03-15',
    status: 'published',
  },
];

// 模拟设计稿数据
export const mockProjectDesigns: ProjectDesign[] = [
  {
    projectId: '1',
    rooms: [
      {
        id: 'room-1',
        roomName: '客厅',
        images: [],
        versionHistory: [],
      },
      {
        id: 'room-2',
        roomName: '主卧',
        images: [],
        versionHistory: [],
      },
      {
        id: 'room-3',
        roomName: '次卧',
        images: [],
        versionHistory: [],
      },
      {
        id: 'room-4',
        roomName: '厨房',
        images: [],
        versionHistory: [],
      },
      {
        id: 'room-5',
        roomName: '卫生间',
        images: [],
        versionHistory: [],
      },
    ],
  },
];
