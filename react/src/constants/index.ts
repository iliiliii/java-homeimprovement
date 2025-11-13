// 项目状态配置
export const PROJECT_STATUS = {
  planning: { label: '规划中', color: 'default' as const },
  inProgress: { label: '进行中', color: 'processing' as const },
  completed: { label: '已完成', color: 'success' as const },
  suspended: { label: '暂停', color: 'error' as const },
} as const;

export type ProjectStatus = keyof typeof PROJECT_STATUS;

// 时间轴状态配置
export const TIMELINE_STATUS = {
  completed: { label: '已完成', color: 'success' as const },
  inProgress: { label: '进行中', color: 'processing' as const },
  pending: { label: '待开始', color: 'default' as const },
} as const;

export type TimelineStatus = keyof typeof TIMELINE_STATUS;

// 质检状态配置
export const QUALITY_STATUS = {
  passed: { label: '通过', color: 'success' as const },
  failed: { label: '不通过', color: 'error' as const },
  pending: { label: '待检查', color: 'default' as const },
} as const;

export type QualityStatus = keyof typeof QUALITY_STATUS;

// 团队角色配置
export const TEAM_ROLES = {
  设计师: { color: 'blue' as const },
  项目经理: { color: 'green' as const },
  工长: { color: 'orange' as const },
  监理: { color: 'purple' as const },
} as const;

export type TeamRole = keyof typeof TEAM_ROLES;

// 标准施工阶段
export const CONSTRUCTION_PHASES = [
  { id: '1', name: '基础工程' },
  { id: '2', name: '安装工程' },
  { id: '3', name: '泥瓦工程' },
  { id: '4', name: '木工工程' },
  { id: '5', name: '油漆工程' },
  { id: '6', name: '收尾工程' },
] as const;
