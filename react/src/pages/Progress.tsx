import { Card, Tag, Progress as AntProgress, Typography, Space, Row, Col, Timeline, Alert, List, Badge, Empty, Button, Collapse, Image } from 'antd';
import { CalendarOutlined, WarningOutlined, FolderOutlined, EnvironmentOutlined, ClockCircleOutlined, CheckCircleOutlined, CloseCircleOutlined, PlusOutlined, FileImageOutlined } from '@ant-design/icons';
import Layout from '@/components/Layout';
import { mockProjects, type Project, type InspectionRecord } from '@/data/mockData';
import InspectionReport from '@/components/InspectionReport';
import { useState } from 'react';

const { Title, Text } = Typography;

const Progress = () => {
  const [projects, setProjects] = useState<Project[]>(mockProjects);
  const [inspectionModalOpen, setInspectionModalOpen] = useState(false);
  const [selectedTimelineNode, setSelectedTimelineNode] = useState<{index: number, title: string} | null>(null);

  // 只筛选进行中的项目（基于state）
  const inProgressProjects = projects.filter(p => p.status === 'inProgress');
  const [selectedProject, setSelectedProject] = useState<Project | null>(inProgressProjects[0] || null);

  const getStatusTag = (status: string) => {
    const statusMap = {
      inProgress: { label: '进行中', color: 'processing' },
      completed: { label: '已完成', color: 'success' },
      planning: { label: '规划中', color: 'default' },
      suspended: { label: '暂停', color: 'error' },
    };
    return statusMap[status as keyof typeof statusMap] || statusMap.planning;
  };

  const calculateProgress = (project: any) => {
    const completedSteps = project.timeline.filter((t: any) => t.status === 'completed').length;
    const inProgressSteps = project.timeline.filter((t: any) => t.status === 'inProgress').length;
    const totalSteps = project.timeline.length;
    const progressPercentage = (completedSteps / totalSteps) * 100;

    return {
      completedSteps,
      inProgressSteps,
      totalSteps,
      progressPercentage,
    };
  };

  const handleOpenInspectionModal = (nodeIndex: number, nodeTitle: string) => {
    setSelectedTimelineNode({ index: nodeIndex, title: nodeTitle });
    setInspectionModalOpen(true);
  };

  const handleInspectionSubmit = (data: any) => {
    if (!selectedProject || selectedTimelineNode === null) return;

    const newInspection: InspectionRecord = {
      id: `ins-${Date.now()}`,
      ...data,
    };

    // Update the project's timeline node with new inspection
    setProjects(prevProjects => {
      const updatedProjects = prevProjects.map(p => {
        if (p.id === selectedProject.id) {
          const updatedTimeline = [...p.timeline];
          const nodeIndex = selectedTimelineNode.index;

          if (!updatedTimeline[nodeIndex].inspections) {
            updatedTimeline[nodeIndex].inspections = [];
          }

          updatedTimeline[nodeIndex].inspections = [
            newInspection,
            ...updatedTimeline[nodeIndex].inspections!
          ];

          const updatedProject = { ...p, timeline: updatedTimeline };

          // Update selected project immediately with the same reference
          setSelectedProject(updatedProject);

          return updatedProject;
        }
        return p;
      });

      return updatedProjects;
    });

    setInspectionModalOpen(false);
    setSelectedTimelineNode(null);
  };

  return (
    <Layout>
      <Space direction="vertical" size="large" style={{ width: '100%' }}>
        <div>
          <Title level={2}>进度跟踪</Title>
          <Text type="secondary">跟踪进行中项目的施工进度</Text>
        </div>

        <Row gutter={16}>
          {/* 左侧：进行中的项目列表 */}
          <Col xs={24} lg={8}>
            <Card
              title={
                <Space>
                  <FolderOutlined />
                  <span>进行中的项目</span>
                  <Badge count={inProgressProjects.length} style={{ backgroundColor: '#52c41a' }} />
                </Space>
              }
            >
              {inProgressProjects.length > 0 ? (
                <List
                  dataSource={inProgressProjects}
                  renderItem={(project) => {
                    const progress = calculateProgress(project);
                    const isSelected = selectedProject?.id === project.id;

                    return (
                      <List.Item
                        style={{
                          cursor: 'pointer',
                          backgroundColor: isSelected ? '#e6f4ff' : 'transparent',
                          padding: '16px',
                          borderRadius: '8px',
                          marginBottom: '8px',
                          transition: 'all 0.3s',
                        }}
                        onClick={() => setSelectedProject(project)}
                      >
                        <Space direction="vertical" style={{ width: '100%' }} size="small">
                          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                            <Text strong style={{ fontSize: 16 }}>{project.projectName}</Text>
                            <Tag color="processing">进行中</Tag>
                          </div>

                          <Space size="small">
                            <EnvironmentOutlined style={{ color: '#999' }} />
                            <Text type="secondary" style={{ fontSize: 13 }}>
                              {project.siteName}
                            </Text>
                          </Space>

                          <div style={{ marginTop: 8 }}>
                            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 4 }}>
                              <Text type="secondary" style={{ fontSize: 12 }}>进度</Text>
                              <Text strong style={{ fontSize: 12, color: '#1677ff' }}>
                                {progress.progressPercentage.toFixed(0)}%
                              </Text>
                            </div>
                            <AntProgress
                              percent={Number(progress.progressPercentage.toFixed(1))}
                              strokeColor="#1677ff"
                              size="small"
                              showInfo={false}
                            />
                          </div>

                          <Space split="·" size="small" style={{ fontSize: 12 }}>
                            <Text type="secondary">
                              已完成 {progress.completedSteps}/{progress.totalSteps}
                            </Text>
                            <Text type="secondary">进行中 {progress.inProgressSteps}</Text>
                          </Space>
                        </Space>
                      </List.Item>
                    );
                  }}
                />
              ) : (
                <Empty description="暂无进行中的项目" />
              )}
            </Card>
          </Col>

          {/* 右侧：选中项目的详细进度 */}
          <Col xs={24} lg={16}>
            <Card
              title={
                selectedProject ? (
                  <Space>
                    <FolderOutlined />
                    <span>{selectedProject.projectName}</span>
                    <Tag color={getStatusTag(selectedProject.status).color}>
                      {getStatusTag(selectedProject.status).label}
                    </Tag>
                  </Space>
                ) : (
                  <span>项目进度详情</span>
                )
              }
            >
              {selectedProject ? (
                <Space direction="vertical" size="large" style={{ width: '100%' }}>
                  {/* 项目基本信息 */}
                  <div>
                    <Space direction="vertical" size="small">
                      <Space>
                        <EnvironmentOutlined style={{ color: '#1677ff' }} />
                        <Text strong>工地地址：</Text>
                        <Text>{selectedProject.siteName}</Text>
                      </Space>
                      <Space>
                        <ClockCircleOutlined style={{ color: '#1677ff' }} />
                        <Text strong>开工日期：</Text>
                        <Text>{selectedProject.startDate}</Text>
                      </Space>
                      <Space>
                        <CalendarOutlined style={{ color: '#1677ff' }} />
                        <Text strong>预计完工：</Text>
                        <Text>{selectedProject.endDate}</Text>
                      </Space>
                    </Space>
                  </div>

                  {/* 进度统计 */}
                  <Row gutter={16}>
                    {(() => {
                      const progress = calculateProgress(selectedProject);
                      return (
                        <>
                          <Col span={8}>
                            <Card size="small" style={{ background: '#f0f5ff', textAlign: 'center', borderColor: '#1677ff' }}>
                              <Text type="secondary" style={{ display: 'block', marginBottom: 8 }}>总进度</Text>
                              <Text strong style={{ fontSize: 32, color: '#1677ff' }}>
                                {progress.progressPercentage.toFixed(0)}%
                              </Text>
                            </Card>
                          </Col>
                          <Col span={8}>
                            <Card size="small" style={{ background: '#f6ffed', textAlign: 'center', borderColor: '#52c41a' }}>
                              <Text type="secondary" style={{ display: 'block', marginBottom: 8 }}>已完成</Text>
                              <Text strong style={{ fontSize: 32, color: '#52c41a' }}>
                                {progress.completedSteps}/{progress.totalSteps}
                              </Text>
                            </Card>
                          </Col>
                          <Col span={8}>
                            <Card size="small" style={{ background: '#fff7e6', textAlign: 'center', borderColor: '#faad14' }}>
                              <Text type="secondary" style={{ display: 'block', marginBottom: 8 }}>进行中</Text>
                              <Text strong style={{ fontSize: 32, color: '#faad14' }}>
                                {progress.inProgressSteps}
                              </Text>
                            </Card>
                          </Col>
                        </>
                      );
                    })()}
                  </Row>

                  {/* 整体进度条 */}
                  <div>
                    <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 12 }}>
                      <Text strong style={{ fontSize: 16 }}>整体进度</Text>
                      <Text type="secondary" style={{ fontSize: 16 }}>
                        {calculateProgress(selectedProject).progressPercentage.toFixed(1)}%
                      </Text>
                    </div>
                    <AntProgress
                      percent={Number(calculateProgress(selectedProject).progressPercentage.toFixed(1))}
                      strokeColor={{
                        '0%': '#108ee9',
                        '100%': '#87d068',
                      }}
                      strokeWidth={12}
                    />
                  </div>

                  {/* 进度时间轴 */}
                  <div>
                    <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 16 }}>
                      <CalendarOutlined style={{ fontSize: 18, color: '#1677ff' }} />
                      <Text strong style={{ fontSize: 16 }}>施工进度时间轴</Text>
                    </div>
                    <Timeline
                      items={selectedProject.timeline.map((item: any, index: number) => ({
                        color:
                          item.status === 'completed' ? 'green' :
                          item.status === 'inProgress' ? 'blue' : 'gray',
                        children: (
                          <Card size="small" style={{ marginBottom: 8 }}>
                            <Space direction="vertical" style={{ width: '100%' }} size="middle">
                              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                                <div>
                                  <Text strong style={{ fontSize: 15 }}>{item.title}</Text>
                                  {' '}
                                  <Tag
                                    color={
                                      item.status === 'completed' ? 'success' :
                                      item.status === 'inProgress' ? 'processing' : 'default'
                                    }
                                  >
                                    {item.status === 'completed' ? '已完成' :
                                     item.status === 'inProgress' ? '进行中' : '待开始'}
                                  </Tag>
                                </div>
                                <Button
                                  type="primary"
                                  icon={<PlusOutlined />}
                                  size="small"
                                  onClick={() => handleOpenInspectionModal(index, item.title)}
                                >
                                  验收上报
                                </Button>
                              </div>

                              <Text type="secondary" style={{ display: 'block' }}>
                                {item.description}
                              </Text>

                              <Text type="secondary" style={{ fontSize: 13 }}>
                                <CalendarOutlined style={{ marginRight: 4 }} />
                                {item.date}
                              </Text>

                              {/* Display inspection records */}
                              {item.inspections && item.inspections.length > 0 && (
                                <div>
                                  <Text strong style={{ fontSize: 14, display: 'block', marginBottom: 8 }}>
                                    验收记录 ({item.inspections.length})
                                  </Text>
                                  <Collapse
                                    size="small"
                                    items={item.inspections.map((inspection: InspectionRecord) => ({
                                      key: inspection.id,
                                      label: (
                                        <Space>
                                          {inspection.isPassed ? (
                                            <CheckCircleOutlined style={{ color: '#52c41a' }} />
                                          ) : (
                                            <CloseCircleOutlined style={{ color: '#f5222d' }} />
                                          )}
                                          <Text strong>{inspection.title}</Text>
                                          <Tag color={inspection.isPassed ? 'success' : 'error'}>
                                            {inspection.isPassed ? '合格' : '不合格'}
                                          </Tag>
                                        </Space>
                                      ),
                                      children: (
                                        <Space direction="vertical" style={{ width: '100%' }} size="small">
                                          <div>
                                            <Text type="secondary">验收内容：</Text>
                                            <Text>{inspection.content}</Text>
                                          </div>
                                          <div>
                                            <Text type="secondary">验收时间：</Text>
                                            <Text>{inspection.inspectionTime}</Text>
                                          </div>
                                          <div>
                                            <Text type="secondary">验收人：</Text>
                                            <Text>{inspection.inspector}</Text>
                                          </div>
                                          {inspection.images && inspection.images.length > 0 && (
                                            <div>
                                              <Text type="secondary" style={{ display: 'block', marginBottom: 8 }}>
                                                <FileImageOutlined /> 现场照片 ({inspection.images.length})
                                              </Text>
                                              <Image.PreviewGroup>
                                                <Space wrap>
                                                  {inspection.images.map((img, idx) => (
                                                    <Image
                                                      key={idx}
                                                      width={80}
                                                      height={80}
                                                      src={img}
                                                      style={{ objectFit: 'cover', borderRadius: 4 }}
                                                    />
                                                  ))}
                                                </Space>
                                              </Image.PreviewGroup>
                                            </div>
                                          )}
                                        </Space>
                                      ),
                                    }))}
                                  />
                                </div>
                              )}
                            </Space>
                          </Card>
                        ),
                      }))}
                    />
                  </div>

                  {/* 提醒信息 */}
                  {(() => {
                    const progress = calculateProgress(selectedProject);
                    if (selectedProject.status === 'inProgress' && progress.inProgressSteps === 0) {
                      return (
                        <Alert
                          message="进度提醒"
                          description="当前无进行中的任务，请及时安排下一阶段施工"
                          type="warning"
                          icon={<WarningOutlined />}
                          showIcon
                        />
                      );
                    }
                    return null;
                  })()}
                </Space>
              ) : (
                <Empty
                  description="请在左侧选择一个项目查看进度详情"
                  style={{ marginTop: 100 }}
                />
              )}
            </Card>
          </Col>
        </Row>
      </Space>

      {/* Inspection Report Modal */}
      <InspectionReport
        open={inspectionModalOpen}
        onCancel={() => {
          setInspectionModalOpen(false);
          setSelectedTimelineNode(null);
        }}
        onSubmit={handleInspectionSubmit}
        nodeName={selectedTimelineNode?.title || ''}
      />
    </Layout>
  );
};

export default Progress;
