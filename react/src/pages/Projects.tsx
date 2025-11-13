import { useState } from 'react';
import { Card, Button, Tag, Space, Row, Col, Progress, Modal, Typography, message, Descriptions, Timeline, Avatar } from 'antd';
import {
  EnvironmentOutlined,
  UserOutlined,
  CalendarOutlined,
  DollarOutlined,
  EyeOutlined,
  EditOutlined,
  PlayCircleOutlined,
  PauseCircleOutlined,
  CheckCircleOutlined,
  ExclamationCircleOutlined,
  SettingOutlined,
  ClockCircleOutlined,
  InfoCircleOutlined,
  HistoryOutlined,
  TeamOutlined,
  FileImageOutlined,
} from '@ant-design/icons';
import Layout from '@/components/Layout';
import ProjectForm from '@/components/ProjectForm';
import BudgetManagement from '@/components/BudgetManagement';
import TimelineManagement from '@/components/TimelineManagement';
import TeamAssignment, { type ProjectTeam } from '@/components/TeamAssignment';
import DesignManagement from '@/components/DesignManagement';
import type { TeamMember } from '@/pages/TeamManagement';
import { mockProjects, Project, mockCustomers, mockProjectDesigns, type ProjectDesign } from '@/data/mockData';
import { getProjectStatusConfig, formatBudget, calculatePercentage } from '@/utils/helpers';
import { TEAM_ROLES } from '@/constants';

// 模拟团队成员数据
const mockTeamMembers: TeamMember[] = [
  {
    id: '1',
    name: '张设计',
    role: '设计师',
    phone: '13800138001',
    avatar: '',
    projectIds: [],
  },
  {
    id: '2',
    name: '李经理',
    role: '项目经理',
    phone: '13800138002',
    avatar: '',
    projectIds: [],
  },
  {
    id: '3',
    name: '王工长',
    role: '工长',
    phone: '13800138003',
    avatar: '',
    projectIds: [],
  },
  {
    id: '4',
    name: '赵监理',
    role: '监理',
    phone: '13800138004',
    avatar: '',
    projectIds: [],
  },
];

const { Title, Text } = Typography;
const { confirm } = Modal;

const Projects = () => {
  const [projects, setProjects] = useState<Project[]>(mockProjects);
  const [selectedProject, setSelectedProject] = useState<Project | null>(null);
  const [showProjectForm, setShowProjectForm] = useState(false);
  const [editingProject, setEditingProject] = useState<Project | null>(null);
  const [showBudgetManagement, setShowBudgetManagement] = useState(false);
  const [showTimelineManagement, setShowTimelineManagement] = useState(false);
  const [showTeamAssignment, setShowTeamAssignment] = useState(false);
  const [showDesignManagement, setShowDesignManagement] = useState(false);
  const [managingProject, setManagingProject] = useState<Project | null>(null);
  const [projectDesigns, setProjectDesigns] = useState<ProjectDesign[]>(mockProjectDesigns);


  // 创建新项目
  const handleCreateProject = (data: any) => {
    const newProject: Project = {
      id: Date.now().toString(),
      ...data,
      customerName: mockCustomers.find(c => c.id === data.customerId)?.name || '',
      status: 'planning',
      startDate: data.startDate.toISOString().split('T')[0],
      endDate: data.endDate.toISOString().split('T')[0],
      totalBudget: 0,
      expenses: [],
      timeline: [
        {
          date: data.startDate.toISOString().split('T')[0],
          title: '项目启动',
          description: '项目创建并开始规划',
          status: 'pending',
        },
      ],
    };
    setProjects(prev => [newProject, ...prev]);
    setShowProjectForm(false);
    message.success('项目创建成功！请继续设置项目预算和施工进度', 5);

    // 打开项目详情以便用户设置预算和进度
    setTimeout(() => {
      setSelectedProject(newProject);
    }, 500);
  };

  // 编辑项目
  const handleEditProject = (data: any) => {
    if (!editingProject) return;

    const updatedProject: Project = {
      ...editingProject,
      ...data,
      customerName: mockCustomers.find(c => c.id === data.customerId)?.name || editingProject.customerName,
      startDate: data.startDate.toISOString().split('T')[0],
      endDate: data.endDate.toISOString().split('T')[0],
    };

    setProjects(prev => prev.map(p => p.id === editingProject.id ? updatedProject : p));
    setEditingProject(null);
    message.success('项目信息更新成功');
  };

  // 删除项目
  const handleDeleteProject = (project: Project) => {
    confirm({
      title: '确认删除项目',
      icon: <ExclamationCircleOutlined />,
      content: `您确定要删除项目 "${project.projectName}" 吗？此操作无法撤销。`,
      okText: '删除',
      okType: 'danger',
      cancelText: '取消',
      onOk() {
        setProjects(prev => prev.filter(p => p.id !== project.id));
        message.success('项目删除成功');
      },
    });
  };

  // 更新项目状态
  const handleUpdateProjectStatus = (projectId: string, newStatus: Project['status']) => {
    setProjects(prev => prev.map(p =>
      p.id === projectId ? { ...p, status: newStatus } : p
    ));
    setSelectedProject(prev => prev ? { ...prev, status: newStatus } : null);
    message.success(`项目状态已更新为${getProjectStatusConfig(newStatus).label}`);
  };

  // 保存预算数据
  const handleSaveBudget = (budgets: any[]) => {
    if (!managingProject) return;

    const totalBudget = budgets.reduce((sum, b) => sum + b.amount, 0);
    const updatedProject = {
      ...managingProject,
      totalBudget,
      expenses: budgets,
    };

    setProjects(prev => prev.map(p => p.id === managingProject.id ? updatedProject : p));
    if (selectedProject?.id === managingProject.id) {
      setSelectedProject(updatedProject);
    }
    setShowBudgetManagement(false);
    setManagingProject(null);
    message.success('预算已保存');
  };

  // 保存进度时间轴数据
  const handleSaveTimeline = (timeline: any[]) => {
    if (!managingProject) return;

    const updatedProject = {
      ...managingProject,
      timeline,
    };

    setProjects(prev => prev.map(p => p.id === managingProject.id ? updatedProject : p));
    if (selectedProject?.id === managingProject.id) {
      setSelectedProject(updatedProject);
    }
    setShowTimelineManagement(false);
    setManagingProject(null);
    message.success('施工进度已保存');
  };

  // 打开预算管理
  const handleOpenBudgetManagement = (project: Project) => {
    setManagingProject(project);
    setShowBudgetManagement(true);
  };

  // 打开进度管理
  const handleOpenTimelineManagement = (project: Project) => {
    setManagingProject(project);
    setShowTimelineManagement(true);
  };

  // 打开团队分配
  const handleOpenTeamAssignment = (project: Project) => {
    setManagingProject(project);
    setShowTeamAssignment(true);
  };

  // 保存团队分配
  const handleSaveTeam = (team: ProjectTeam) => {
    if (!managingProject) return;

    const updatedProject = {
      ...managingProject,
      team,
    };

    setProjects(prev => prev.map(p => p.id === managingProject.id ? updatedProject : p));
    if (selectedProject?.id === managingProject.id) {
      setSelectedProject(updatedProject);
    }
    setShowTeamAssignment(false);
    setManagingProject(null);
    message.success('团队成员已分配');
  };

  // 获取团队成员名称
  const getTeamMemberName = (memberId: string) => {
    return mockTeamMembers.find(m => m.id === memberId)?.name || '';
  };

  // 打开设计稿管理
  const handleOpenDesignManagement = (project: Project) => {
    setManagingProject(project);
    setShowDesignManagement(true);
  };

  // 获取项目设计稿数据
  const getProjectDesign = (projectId: string): ProjectDesign | null => {
    return projectDesigns.find(d => d.projectId === projectId) || null;
  };

  // 更新设计稿数据
  const handleUpdateDesign = (updatedDesign: ProjectDesign) => {
    setProjectDesigns(prev => {
      const index = prev.findIndex(d => d.projectId === updatedDesign.projectId);
      if (index >= 0) {
        const updated = [...prev];
        updated[index] = updatedDesign;
        return updated;
      } else {
        return [...prev, updatedDesign];
      }
    });
  };

  return (
    <Layout>
      <Space direction="vertical" size="large" style={{ width: '100%' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <div>
            <Title level={2}>项目管理</Title>
            <Text type="secondary">管理所有装修项目</Text>
          </div>
          <Button type="primary" onClick={() => setShowProjectForm(true)}>
            新建项目
          </Button>
        </div>

        <Row gutter={[16, 16]}>
          {projects.map((project) => {
            const totalExpenses = project.expenses.reduce((sum, e) => sum + e.amount, 0);
            const status = getProjectStatusConfig(project.status);
            const progressPercent = calculatePercentage(totalExpenses, project.totalBudget);

            return (
              <Col xs={24} sm={12} lg={8} key={project.id}>
                <Card
                  hoverable
                  actions={[
                    <Button
                      type="link"
                      icon={<EyeOutlined />}
                      onClick={() => setSelectedProject(project)}
                    >
                      查看
                    </Button>,
                    <Button
                      type="link"
                      icon={<EditOutlined />}
                      onClick={() => setEditingProject(project)}
                    >
                      编辑
                    </Button>,
                    <Button
                      type="link"
                      icon={<DollarOutlined />}
                      onClick={() => handleOpenBudgetManagement(project)}
                    >
                      预算
                    </Button>,
                    <Button
                      type="link"
                      icon={<ClockCircleOutlined />}
                      onClick={() => handleOpenTimelineManagement(project)}
                    >
                      进度
                    </Button>,
                  ]}
                >
                  <Space direction="vertical" size="middle" style={{ width: '100%' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                      <div style={{ flex: 1 }}>
                        <Title level={5} style={{ margin: 0 }}>{project.projectName}</Title>
                        <Text type="secondary">{project.siteName}</Text>
                      </div>
                      <Tag color={status.color}>{status.label}</Tag>
                    </div>

                    <Space direction="vertical" size="small" style={{ width: '100%' }}>
                      <Text type="secondary">
                        <EnvironmentOutlined /> {project.siteAddress}
                      </Text>
                      <Text type="secondary">
                        <UserOutlined /> 客户：{project.customerName}
                      </Text>
                      <Text type="secondary">
                        <CalendarOutlined /> {project.startDate} 至 {project.endDate}
                      </Text>
                    </Space>

                    <div>
                      {project.totalBudget > 0 ? (
                        <>
                          <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 8 }}>
                            <Text strong>总预算</Text>
                            <Text strong style={{ color: '#1677ff', fontSize: 18 }}>
                              ¥{formatBudget(project.totalBudget)}万
                            </Text>
                          </div>
                          <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 8 }}>
                            <Text type="secondary">已支出</Text>
                            <Text type="secondary">¥{formatBudget(totalExpenses)}万</Text>
                          </div>
                          <Progress percent={progressPercent} size="small" />
                        </>
                      ) : (
                        <div
                          style={{
                            padding: '12px',
                            background: '#fff7e6',
                            border: '1px dashed #ffd591',
                            borderRadius: '4px',
                            textAlign: 'center',
                          }}
                        >
                          <Text type="secondary" style={{ fontSize: 12 }}>
                            <DollarOutlined /> 尚未设置预算
                          </Text>
                        </div>
                      )}
                    </div>
                  </Space>
                </Card>
              </Col>
            );
          })}
        </Row>
      </Space>

      {/* 项目详情弹窗 */}
      <Modal
        title={
          <Space>
            <span>{selectedProject?.projectName}</span>
            {selectedProject && (
              <Tag color={getProjectStatusConfig(selectedProject.status).color}>
                {getProjectStatusConfig(selectedProject.status).label}
              </Tag>
            )}
          </Space>
        }
        open={!!selectedProject}
        onCancel={() => setSelectedProject(null)}
        width={1200}
        footer={null}
        style={{ top: 20 }}
      >
        {selectedProject && (
          <div style={{ maxHeight: 'calc(90vh - 150px)', overflowY: 'auto', padding: '0 4px' }}>
            <Space direction="vertical" size="middle" style={{ width: '100%' }}>
              {/* 项目状态管理 */}
              <Card size="small" style={{ background: '#f5f5f5' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <div>
                    <Text strong>项目状态管理</Text>
                    <br />
                    <Text type="secondary">当前状态：{getProjectStatusConfig(selectedProject.status).label}</Text>
                  </div>
                  <Space>
                    {selectedProject.status === 'planning' && (
                      <Button
                        type="primary"
                        icon={<PlayCircleOutlined />}
                        onClick={() => handleUpdateProjectStatus(selectedProject.id, 'inProgress')}
                      >
                        开始项目
                      </Button>
                    )}
                    {selectedProject.status === 'inProgress' && (
                      <>
                        <Button
                          icon={<PauseCircleOutlined />}
                          onClick={() => handleUpdateProjectStatus(selectedProject.id, 'suspended')}
                        >
                          暂停项目
                        </Button>
                        <Button
                          type="primary"
                          icon={<CheckCircleOutlined />}
                          onClick={() => handleUpdateProjectStatus(selectedProject.id, 'completed')}
                        >
                          完成项目
                        </Button>
                      </>
                    )}
                    {selectedProject.status === 'suspended' && (
                      <Button
                        type="primary"
                        icon={<PlayCircleOutlined />}
                        onClick={() => handleUpdateProjectStatus(selectedProject.id, 'inProgress')}
                      >
                        恢复项目
                      </Button>
                    )}
                  </Space>
                </div>
              </Card>

              {/* 项目设置操作 */}
              <Card size="small" style={{ background: '#fff7e6', border: '1px solid #ffd591' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <div>
                    <Text strong><SettingOutlined /> 项目设置</Text>
                    <br />
                    <Text type="secondary">管理项目预算、施工进度、团队成员和设计稿</Text>
                  </div>
                  <Space wrap>
                    <Button
                      icon={<DollarOutlined />}
                      onClick={() => handleOpenBudgetManagement(selectedProject)}
                    >
                      管理预算
                    </Button>
                    <Button
                      icon={<ClockCircleOutlined />}
                      onClick={() => handleOpenTimelineManagement(selectedProject)}
                    >
                      管理进度
                    </Button>
                    <Button
                      icon={<TeamOutlined />}
                      onClick={() => handleOpenTeamAssignment(selectedProject)}
                    >
                      分配团队
                    </Button>
                    <Button
                      type="primary"
                      icon={<FileImageOutlined />}
                      onClick={() => handleOpenDesignManagement(selectedProject)}
                    >
                      设计稿管理
                    </Button>
                  </Space>
                </div>
              </Card>

              {/* 项目基本信息 */}
              <Card
                size="small"
                title={
                  <Space>
                    <InfoCircleOutlined style={{ color: '#1677ff' }} />
                    <span>项目基本信息</span>
                  </Space>
                }
              >
                <Descriptions column={2} size="small">
                  <Descriptions.Item label="项目名称" span={2}>
                    {selectedProject.projectName}
                  </Descriptions.Item>
                  <Descriptions.Item label="工地名称" span={2}>
                    {selectedProject.siteName}
                  </Descriptions.Item>
                  <Descriptions.Item label="工地地址" span={2}>
                    {selectedProject.siteAddress}
                  </Descriptions.Item>
                  <Descriptions.Item label="关联客户">
                    {selectedProject.customerName}
                  </Descriptions.Item>
                  <Descriptions.Item label="项目状态">
                    <Tag color={getProjectStatusConfig(selectedProject.status).color}>
                      {getProjectStatusConfig(selectedProject.status).label}
                    </Tag>
                  </Descriptions.Item>
                  <Descriptions.Item label="开始日期">
                    {selectedProject.startDate}
                  </Descriptions.Item>
                  <Descriptions.Item label="预计完工">
                    {selectedProject.endDate}
                  </Descriptions.Item>
                </Descriptions>
              </Card>

              {/* 项目预算 */}
              <Card
                size="small"
                title={
                  <Space>
                    <DollarOutlined style={{ color: '#faad14' }} />
                    <span>项目预算</span>
                  </Space>
                }
              >
                {selectedProject.expenses.length > 0 ? (
                  <Space direction="vertical" size="small" style={{ width: '100%' }}>
                    <Row gutter={[16, 8]}>
                      {selectedProject.expenses.map((expense, index) => (
                        <Col span={12} key={index}>
                          <Card size="small" style={{ background: '#fafafa' }}>
                            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                              <Text>{expense.category}</Text>
                              <Text strong style={{ color: '#faad14' }}>
                                ¥{expense.amount.toLocaleString()}
                              </Text>
                            </div>
                          </Card>
                        </Col>
                      ))}
                    </Row>
                    <Card size="small" style={{ background: '#fff7e6', borderColor: '#faad14' }}>
                      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                        <Text strong style={{ fontSize: 16 }}>预算总额</Text>
                        <Text strong style={{ color: '#faad14', fontSize: 20 }}>
                          ¥{selectedProject.totalBudget.toLocaleString()}
                        </Text>
                      </div>
                    </Card>
                  </Space>
                ) : (
                  <div style={{ textAlign: 'center', padding: '20px 0' }}>
                    <Text type="secondary">暂无预算信息</Text>
                  </div>
                )}
              </Card>

              {/* 项目团队 */}
              <Card
                size="small"
                title={
                  <Space>
                    <TeamOutlined style={{ color: '#722ed1' }} />
                    <span>项目团队</span>
                  </Space>
                }
              >
                {selectedProject.team ? (
                  <Space direction="vertical" size="middle" style={{ width: '100%' }}>
                    {/* 设计师 */}
                    {selectedProject.team.designers && selectedProject.team.designers.length > 0 && (
                      <div>
                        <Text type="secondary" style={{ marginBottom: 8, display: 'block' }}>
                          <Tag color={TEAM_ROLES['设计师'].color}>设计师</Tag>
                        </Text>
                        <Space wrap>
                          {selectedProject.team.designers.map((id) => (
                            <Card key={id} size="small" style={{ background: '#f0f5ff' }}>
                              <Space>
                                <Avatar size={32} icon={<UserOutlined />} style={{ background: '#1677ff' }} />
                                <Text>{getTeamMemberName(id)}</Text>
                              </Space>
                            </Card>
                          ))}
                        </Space>
                      </div>
                    )}

                    {/* 项目经理 */}
                    {selectedProject.team.projectManagers && selectedProject.team.projectManagers.length > 0 && (
                      <div>
                        <Text type="secondary" style={{ marginBottom: 8, display: 'block' }}>
                          <Tag color={TEAM_ROLES['项目经理'].color}>项目经理</Tag>
                        </Text>
                        <Space wrap>
                          {selectedProject.team.projectManagers.map((id) => (
                            <Card key={id} size="small" style={{ background: '#f6ffed' }}>
                              <Space>
                                <Avatar size={32} icon={<UserOutlined />} style={{ background: '#52c41a' }} />
                                <Text>{getTeamMemberName(id)}</Text>
                              </Space>
                            </Card>
                          ))}
                        </Space>
                      </div>
                    )}

                    {/* 工长 */}
                    {selectedProject.team.foremen && selectedProject.team.foremen.length > 0 && (
                      <div>
                        <Text type="secondary" style={{ marginBottom: 8, display: 'block' }}>
                          <Tag color={TEAM_ROLES['工长'].color}>工长</Tag>
                        </Text>
                        <Space wrap>
                          {selectedProject.team.foremen.map((id) => (
                            <Card key={id} size="small" style={{ background: '#fff7e6' }}>
                              <Space>
                                <Avatar size={32} icon={<UserOutlined />} style={{ background: '#fa8c16' }} />
                                <Text>{getTeamMemberName(id)}</Text>
                              </Space>
                            </Card>
                          ))}
                        </Space>
                      </div>
                    )}

                    {/* 监理 */}
                    {selectedProject.team.supervisors && selectedProject.team.supervisors.length > 0 && (
                      <div>
                        <Text type="secondary" style={{ marginBottom: 8, display: 'block' }}>
                          <Tag color={TEAM_ROLES['监理'].color}>监理</Tag>
                        </Text>
                        <Space wrap>
                          {selectedProject.team.supervisors.map((id) => (
                            <Card key={id} size="small" style={{ background: '#f9f0ff' }}>
                              <Space>
                                <Avatar size={32} icon={<UserOutlined />} style={{ background: '#722ed1' }} />
                                <Text>{getTeamMemberName(id)}</Text>
                              </Space>
                            </Card>
                          ))}
                        </Space>
                      </div>
                    )}

                    {/* 如果所有角色都为空 */}
                    {(!selectedProject.team.designers || selectedProject.team.designers.length === 0) &&
                     (!selectedProject.team.projectManagers || selectedProject.team.projectManagers.length === 0) &&
                     (!selectedProject.team.foremen || selectedProject.team.foremen.length === 0) &&
                     (!selectedProject.team.supervisors || selectedProject.team.supervisors.length === 0) && (
                      <div style={{ textAlign: 'center', padding: '20px 0' }}>
                        <Text type="secondary">暂无团队成员</Text>
                      </div>
                    )}
                  </Space>
                ) : (
                  <div style={{ textAlign: 'center', padding: '20px 0' }}>
                    <Text type="secondary">暂无团队成员</Text>
                  </div>
                )}
              </Card>

              {/* 项目进度 */}
              <Card
                size="small"
                title={
                  <Space>
                    <HistoryOutlined style={{ color: '#52c41a' }} />
                    <span>项目进度</span>
                  </Space>
                }
              >
                {selectedProject.timeline.length > 0 ? (
                  <>
                    {/* 进度统计 */}
                    <Row gutter={16} style={{ marginBottom: 16 }}>
                      <Col span={6}>
                        <Card size="small" style={{ textAlign: 'center', background: '#f0f5ff' }}>
                          <div style={{ fontSize: 24, fontWeight: 'bold', color: '#1677ff' }}>
                            {selectedProject.timeline.length}
                          </div>
                          <Text type="secondary" style={{ fontSize: 12 }}>总阶段</Text>
                        </Card>
                      </Col>
                      <Col span={6}>
                        <Card size="small" style={{ textAlign: 'center', background: '#f6ffed' }}>
                          <div style={{ fontSize: 24, fontWeight: 'bold', color: '#52c41a' }}>
                            {selectedProject.timeline.filter(t => t.status === 'completed').length}
                          </div>
                          <Text type="secondary" style={{ fontSize: 12 }}>已完成</Text>
                        </Card>
                      </Col>
                      <Col span={6}>
                        <Card size="small" style={{ textAlign: 'center', background: '#e6f7ff' }}>
                          <div style={{ fontSize: 24, fontWeight: 'bold', color: '#1677ff' }}>
                            {selectedProject.timeline.filter(t => t.status === 'inProgress').length}
                          </div>
                          <Text type="secondary" style={{ fontSize: 12 }}>进行中</Text>
                        </Card>
                      </Col>
                      <Col span={6}>
                        <Card size="small" style={{ textAlign: 'center', background: '#f9f0ff' }}>
                          <div style={{ fontSize: 24, fontWeight: 'bold', color: '#722ed1' }}>
                            {((selectedProject.timeline.filter(t => t.status === 'completed').length / selectedProject.timeline.length) * 100).toFixed(0)}%
                          </div>
                          <Text type="secondary" style={{ fontSize: 12 }}>完成度</Text>
                        </Card>
                      </Col>
                    </Row>

                    {/* 进度时间轴 */}
                    <Timeline
                      items={selectedProject.timeline.map((item) => ({
                        color:
                          item.status === 'completed' ? 'green' :
                          item.status === 'inProgress' ? 'blue' : 'gray',
                        children: (
                          <div style={{ paddingBottom: 8 }}>
                            <div style={{ marginBottom: 4 }}>
                              <Text strong style={{ fontSize: 14 }}>{item.title}</Text>
                              {' '}
                              <Tag
                                color={
                                  item.status === 'completed' ? 'success' :
                                  item.status === 'inProgress' ? 'processing' : 'default'
                                }
                                style={{ marginLeft: 8 }}
                              >
                                {item.status === 'completed' ? '已完成' :
                                 item.status === 'inProgress' ? '进行中' : '待开始'}
                              </Tag>
                            </div>
                            <Text type="secondary" style={{ fontSize: 13 }}>{item.description}</Text>
                            <br />
                            <Text type="secondary" style={{ fontSize: 12 }}>
                              <CalendarOutlined /> {item.date}
                            </Text>
                          </div>
                        ),
                      }))}
                    />
                  </>
                ) : (
                  <div style={{ textAlign: 'center', padding: '20px 0' }}>
                    <Text type="secondary">暂无进度信息</Text>
                  </div>
                )}
              </Card>
            </Space>
          </div>
        )}
      </Modal>

      {/* 新建项目表单 */}
      <ProjectForm
        open={showProjectForm}
        onCancel={() => setShowProjectForm(false)}
        onSubmit={handleCreateProject}
      />

      {/* 编辑项目表单 */}
      {editingProject && (
        <ProjectForm
          open={!!editingProject}
          onCancel={() => setEditingProject(null)}
          onSubmit={handleEditProject}
          initialData={{
            projectName: editingProject.projectName,
            siteName: editingProject.siteName,
            siteAddress: editingProject.siteAddress,
            customerId: editingProject.customerId,
            startDate: new Date(editingProject.startDate),
            endDate: new Date(editingProject.endDate),
          }}
          title="编辑项目"
        />
      )}

      {/* 预算管理 */}
      {managingProject && (
        <BudgetManagement
          open={showBudgetManagement}
          onCancel={() => {
            setShowBudgetManagement(false);
            setManagingProject(null);
          }}
          onSave={handleSaveBudget}
          initialData={managingProject.expenses}
          projectName={managingProject.projectName}
        />
      )}

      {/* 进度管理 */}
      {managingProject && (
        <TimelineManagement
          open={showTimelineManagement}
          onCancel={() => {
            setShowTimelineManagement(false);
            setManagingProject(null);
          }}
          onSave={handleSaveTimeline}
          initialData={managingProject.timeline}
          projectName={managingProject.projectName}
        />
      )}

      {/* 团队分配 */}
      {managingProject && (
        <TeamAssignment
          open={showTeamAssignment}
          onCancel={() => {
            setShowTeamAssignment(false);
            setManagingProject(null);
          }}
          onSave={handleSaveTeam}
          initialData={managingProject.team}
          projectName={managingProject.projectName}
          allMembers={mockTeamMembers}
        />
      )}

      {/* 设计稿管理 */}
      {managingProject && (
        <DesignManagement
          open={showDesignManagement}
          onCancel={() => {
            setShowDesignManagement(false);
            setManagingProject(null);
          }}
          projectId={managingProject.id}
          projectName={managingProject.projectName}
          designData={getProjectDesign(managingProject.id)}
          onUpdate={handleUpdateDesign}
        />
      )}
    </Layout>
  );
};

export default Projects;
