import { PROJECT_STATUS, TIMELINE_STATUS, QUALITY_STATUS, TEAM_ROLES } from '@/constants';
import type { ProjectStatus, TimelineStatus, QualityStatus, TeamRole } from '@/constants';

// 获取项目状态配置
export const getProjectStatusConfig = (status: ProjectStatus) => {
  return PROJECT_STATUS[status] || PROJECT_STATUS.planning;
};

// 获取时间轴状态配置
export const getTimelineStatusConfig = (status: TimelineStatus) => {
  return TIMELINE_STATUS[status] || TIMELINE_STATUS.pending;
};

// 获取质检状态配置
export const getQualityStatusConfig = (status: QualityStatus) => {
  return QUALITY_STATUS[status] || QUALITY_STATUS.pending;
};

// 获取团队角色配置
export const getTeamRoleConfig = (role: TeamRole) => {
  return TEAM_ROLES[role];
};

// 格式化金额（转换为万元）
export const formatBudget = (amount: number): string => {
  return (amount / 10000).toFixed(1);
};

// 计算百分比
export const calculatePercentage = (current: number, total: number): number => {
  if (total === 0) return 0;
  return Number(((current / total) * 100).toFixed(1));
};
