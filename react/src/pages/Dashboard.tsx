import { Card, Row, Col, Statistic, Progress, Tag, Typography, Space, Divider, List, Avatar, Button, Badge, Alert, Timeline } from 'antd';
import {
  FolderOutlined,
  TeamOutlined,
  DollarOutlined,
  CheckCircleOutlined,
  UserOutlined,
  PhoneOutlined,
  EnvironmentOutlined,
  CalendarOutlined,
  ArrowRightOutlined,
  TrophyOutlined,
  ClockCircleOutlined,
  WarningOutlined,
  HomeOutlined,
} from '@ant-design/icons';
import Layout from '@/components/Layout';
import { mockProjects, mockCustomers, mockQualityChecks } from '@/data/mockData';
import { useNavigate } from 'react-router-dom';

const { Title, Text } = Typography;

const Dashboard = () => {
  const navigate = useNavigate();

  // 统计数据
  const totalProjects = mockProjects.length;
  const inProgressProjects = mockProjects.filter(p => p.status === 'inProgress').length;
  const completedProjects = mockProjects.filter(p => p.status === 'completed').length;
  const planningProjects = mockProjects.filter(p => p.status === 'planning').length;

  const totalCustomers = mockCustomers.length;
  const activeCustomers = mockCustomers.filter(c => c.status === 'active').length;

  const totalBudget = mockProjects.reduce((sum, p) => sum + p.totalBudget, 0);
  const totalExpense = mockProjects.reduce((sum, p) => sum + p.expenses.reduce((s, e) => s + e.amount, 0), 0);

  const passedQualityChecks = mockQualityChecks.filter(q => q.status === 'passed').length;
  const failedQualityChecks = mockQualityChecks.filter(q => q.status === 'failed').length;

  // 客户-项目关联统计
  const getCustomerProjects = (customerName: string) => {
    return mockProjects.filter(p => p.customerName === customerName);
  };

  // 获取最活跃的客户（项目最多的客户）
  const topCustomers = mockCustomers
    .map(customer => ({
      ...customer,
      projectCount: getCustomerProjects(customer.name).length,
      totalBudget: getCustomerProjects(customer.name).reduce((sum, p) => sum + p.totalBudget, 0),
    }))
    .sort((a, b) => b.projectCount - a.projectCount)
    .slice(0, 5);

  return (
    <Layout>
      <Space direction="vertical" size="large" style={{ width: '100%' }}>
        {/* 页面标题 */}
        <div>
          <Title level={2}>总览看板</Title>
          <Text type="secondary">客户管理与项目管理全局视图</Text>
        </div>

        {/* 核心指标卡片 */}
        <Row gutter={[16, 16]}>
          <Col xs={24} sm={12} lg={6}>
            <Card hoverable onClick={() => navigate('/customers')} style={{ cursor: 'pointer' }}>
              <Statistic
                title={
                  <Space>
                    <TeamOutlined style={{ color: '#52c41a' }} />
                    <span>客户总数</span>
                  </Space>
                }
                value={totalCustomers}
                suffix="位"
                valueStyle={{ color: '#52c41a' }}
              />
              <Divider style={{ margin: '12px 0' }} />
              <Text type="secondary" style={{ fontSize: 12 }}>
                活跃客户 {activeCustomers} 位
              </Text>
            </Card>
          </Col>

          <Col xs={24} sm={12} lg={6}>
            <Card hoverable onClick={() => navigate('/projects')} style={{ cursor: 'pointer' }}>
              <Statistic
                title={
                  <Space>
                    <FolderOutlined style={{ color: '#1677ff' }} />
                    <span>项目总数</span>
                  </Space>
                }
                value={totalProjects}
                suffix="个"
                valueStyle={{ color: '#1677ff' }}
              />
              <Divider style={{ margin: '12px 0' }} />
              <Space split="·" size="small" style={{ fontSize: 12 }}>
                <Text type="secondary">进行中 {inProgressProjects}</Text>
                <Text type="secondary">已完成 {completedProjects}</Text>
              </Space>
            </Card>
          </Col>

          <Col xs={24} sm={12} lg={6}>
            <Card>
              <Statistic
                title={
                  <Space>
                    <DollarOutlined style={{ color: '#faad14' }} />
                    <span>合同总额</span>
                  </Space>
                }
                value={(totalBudget / 10000).toFixed(1)}
                suffix="万"
                prefix="¥"
                valueStyle={{ color: '#faad14' }}
              />
              <Divider style={{ margin: '12px 0' }} />
              <Text type="secondary" style={{ fontSize: 12 }}>
                已支出 ¥{(totalExpense / 10000).toFixed(1)}万
              </Text>
            </Card>
          </Col>

          <Col xs={24} sm={12} lg={6}>
            <Card hoverable onClick={() => navigate('/quality')} style={{ cursor: 'pointer' }}>
              <Statistic
                title={
                  <Space>
                    <CheckCircleOutlined style={{ color: '#722ed1' }} />
                    <span>质检通过率</span>
                  </Space>
                }
                value={mockQualityChecks.length > 0 ? ((passedQualityChecks / mockQualityChecks.length) * 100).toFixed(0) : 0}
                suffix="%"
                valueStyle={{ color: '#722ed1' }}
              />
              <Divider style={{ margin: '12px 0' }} />
              <Text type="secondary" style={{ fontSize: 12 }}>
                不通过 {failedQualityChecks} 次
              </Text>
            </Card>
          </Col>
        </Row>

        {/* 客户管理 - 重点突出 */}
        <Card
          title={
            <Space>
              <TrophyOutlined style={{ color: '#faad14' }} />
              <span>重点客户管理</span>
            </Space>
          }
          extra={
            <Button type="link" onClick={() => navigate('/customers')}>
              查看全部 <ArrowRightOutlined />
            </Button>
          }
        >
          <List
            dataSource={topCustomers}
            renderItem={(customer) => {
              const projects = getCustomerProjects(customer.name);
              const activeProjects = projects.filter(p => p.status === 'inProgress').length;

              return (
                <List.Item
                  style={{
                    padding: '16px 0',
                    cursor: 'pointer',
                    transition: 'background 0.3s',
                  }}
                  onClick={() => navigate('/customers')}
                >
                  <List.Item.Meta
                    avatar={
                      <Avatar
                        size={56}
                        style={{ backgroundColor: '#1677ff' }}
                        icon={<UserOutlined />}
                      />
                    }
                    title={
                      <Space>
                        <Text strong style={{ fontSize: 16 }}>{customer.name}</Text>
                        {customer.status === 'active' && (
                          <Badge status="success" text="活跃" />
                        )}
                      </Space>
                    }
                    description={
                      <Space direction="vertical" size="small">
                        <Space size="large">
                          <Space size="small">
                            <PhoneOutlined style={{ color: '#999' }} />
                            <Text type="secondary">{customer.phone}</Text>
                          </Space>
                          <Space size="small">
                            <EnvironmentOutlined style={{ color: '#999' }} />
                            <Text type="secondary">{customer.address}</Text>
                          </Space>
                        </Space>
                        <Space split="·" size="small">
                          <Text type="secondary" style={{ fontSize: 12 }}>
                            项目数量：<Text strong style={{ color: '#1677ff' }}>{customer.projectCount}</Text>
                          </Text>
                          <Text type="secondary" style={{ fontSize: 12 }}>
                            合同总额：<Text strong style={{ color: '#faad14' }}>¥{(customer.totalBudget / 10000).toFixed(1)}万</Text>
                          </Text>
                          {activeProjects > 0 && (
                            <Text style={{ fontSize: 12, color: '#52c41a' }}>
                              进行中 {activeProjects} 个
                            </Text>
                          )}
                        </Space>
                      </Space>
                    }
                  />
                </List.Item>
              );
            }}
          />
        </Card>

        {/* 客户-项目关联视图 */}
        <Row gutter={[16, 16]}>
          <Col xs={24} lg={14}>
            <Card
              title={
                <Space>
                  <FolderOutlined style={{ color: '#1677ff' }} />
                  <span>进行中的项目</span>
                  <Badge count={inProgressProjects} style={{ backgroundColor: '#52c41a' }} />
                </Space>
              }
              extra={
                <Button type="link" onClick={() => navigate('/progress')}>
                  进度跟踪 <ArrowRightOutlined />
                </Button>
              }
            >
              <Space direction="vertical" size="middle" style={{ width: '100%' }}>
                {mockProjects
                  .filter(p => p.status === 'inProgress')
                  .map((project) => {
                    const completedSteps = project.timeline.filter(t => t.status === 'completed').length;
                    const totalSteps = project.timeline.length;
                    const progressPercent = (completedSteps / totalSteps) * 100;

                    return (
                      <Card
                        key={project.id}
                        size="small"
                        hoverable
                        onClick={() => navigate('/projects')}
                        style={{ cursor: 'pointer' }}
                      >
                        <Row gutter={16} align="middle">
                          <Col flex="auto">
                            <Space direction="vertical" size="small" style={{ width: '100%' }}>
                              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                                <Text strong style={{ fontSize: 15 }}>{project.projectName}</Text>
                                <Tag color="processing">进行中</Tag>
                              </div>

                              <Space size="large">
                                <Space size="small">
                                  <UserOutlined style={{ color: '#999' }} />
                                  <Text type="secondary">{project.customerName}</Text>
                                </Space>
                                <Space size="small">
                                  <HomeOutlined style={{ color: '#999' }} />
                                  <Text type="secondary">{project.siteName}</Text>
                                </Space>
                              </Space>

                              <Row gutter={16} style={{ marginTop: 8 }}>
                                <Col span={12}>
                                  <Text type="secondary" style={{ fontSize: 12 }}>施工进度</Text>
                                  <Progress
                                    percent={Number(progressPercent.toFixed(0))}
                                    size="small"
                                    strokeColor="#52c41a"
                                  />
                                </Col>
                                <Col span={12}>
                                  <Text type="secondary" style={{ fontSize: 12 }}>预算执行</Text>
                                  <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: 4 }}>
                                    <Text strong style={{ fontSize: 13, color: '#faad14' }}>
                                      ¥{(project.totalBudget / 10000).toFixed(1)}万
                                    </Text>
                                    <Text type="secondary" style={{ fontSize: 12 }}>
                                      {project.startDate}
                                    </Text>
                                  </div>
                                </Col>
                              </Row>
                            </Space>
                          </Col>
                        </Row>
                      </Card>
                    );
                  })}
              </Space>
            </Card>
          </Col>

          <Col xs={24} lg={10}>
            <Card
              title={
                <Space>
                  <ClockCircleOutlined style={{ color: '#722ed1' }} />
                  <span>待办事项</span>
                </Space>
              }
            >
              <Space direction="vertical" size="middle" style={{ width: '100%' }}>
                {/* 规划中的项目 */}
                {planningProjects > 0 && (
                  <Alert
                    message={`${planningProjects} 个项目待启动`}
                    description="请及时安排项目开工计划"
                    type="warning"
                    showIcon
                    icon={<CalendarOutlined />}
                    action={
                      <Button size="small" onClick={() => navigate('/projects')}>
                        查看
                      </Button>
                    }
                  />
                )}

                {/* 质检待处理 */}
                {failedQualityChecks > 0 && (
                  <Alert
                    message={`${failedQualityChecks} 个质检问题`}
                    description="需要整改处理"
                    type="error"
                    showIcon
                    icon={<WarningOutlined />}
                    action={
                      <Button size="small" danger onClick={() => navigate('/quality')}>
                        处理
                      </Button>
                    }
                  />
                )}

                {/* 近期项目时间线 */}
                <div>
                  <Text strong style={{ fontSize: 14, marginBottom: 12, display: 'block' }}>
                    近期重要节点
                  </Text>
                  <Timeline
                    items={mockProjects
                      .filter(p => p.status === 'inProgress')
                      .flatMap(p =>
                        p.timeline
                          .filter(t => t.status === 'inProgress')
                          .map(t => ({
                            color: 'blue',
                            children: (
                              <div>
                                <Text strong style={{ fontSize: 13 }}>{t.title}</Text>
                                <br />
                                <Text type="secondary" style={{ fontSize: 12 }}>
                                  {p.projectName} - {t.date}
                                </Text>
                              </div>
                            ),
                          }))
                      )
                      .slice(0, 5)}
                  />
                </div>
              </Space>
            </Card>
          </Col>
        </Row>
      </Space>
    </Layout>
  );
};

export default Dashboard;
