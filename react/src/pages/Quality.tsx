import { Card, Button, Tag, Space, Row, Col, Typography, Alert, Statistic, List, Badge, Empty, Timeline, Divider, Collapse, Image, message } from 'antd';
import {
  CheckCircleOutlined,
  CalendarOutlined,
  UserOutlined,
  WarningOutlined,
  EyeOutlined,
  FolderOutlined,
  EnvironmentOutlined,
  CloseCircleOutlined,
  ClockCircleOutlined,
  PlusOutlined,
  FileProtectOutlined,
  FileImageOutlined,
  ToolOutlined,
} from '@ant-design/icons';
import Layout from '@/components/Layout';
import { mockProjects, mockQualityChecks, type QualityCheck, type QualityIssue } from '@/data/mockData';
import QualityIssueReport from '@/components/QualityIssueReport';
import IssueResolution from '@/components/IssueResolution';
import { useState } from 'react';

const { Title, Text } = Typography;

const Quality = () => {
  // 只筛选进行中的项目
  const inProgressProjects = mockProjects.filter(p => p.status === 'inProgress');
  const [selectedProject, setSelectedProject] = useState(inProgressProjects[0] || null);
  const [qualityChecks, setQualityChecks] = useState<QualityCheck[]>(mockQualityChecks);
  const [issueReportModalOpen, setIssueReportModalOpen] = useState(false);
  const [resolutionModalOpen, setResolutionModalOpen] = useState(false);
  const [selectedIssue, setSelectedIssue] = useState<QualityIssue | null>(null);

  // 获取项目的质检记录
  const getProjectQualityChecks = (projectName: string) => {
    return qualityChecks.filter(q => q.projectName === projectName);
  };

  // Handle issue report submission
  const handleIssueReportSubmit = (data: any) => {
    if (!selectedProject) return;

    const newIssue: QualityIssue = {
      id: `issue-${Date.now()}`,
      ...data,
    };

    // Find or create a quality check record for today
    const today = new Date().toISOString().split('T')[0];
    const existingCheckIndex = qualityChecks.findIndex(
      q => q.projectName === selectedProject.projectName && q.checkDate === today
    );

    if (existingCheckIndex >= 0) {
      // Add to existing check
      setQualityChecks(prev => {
        const updated = [...prev];
        updated[existingCheckIndex] = {
          ...updated[existingCheckIndex],
          issues: [newIssue, ...updated[existingCheckIndex].issues],
          status: 'failed',
        };
        return updated;
      });
    } else {
      // Create new check record
      const newCheck: QualityCheck = {
        id: `qc-${Date.now()}`,
        projectId: selectedProject.id,
        projectName: selectedProject.projectName,
        checkDate: today,
        inspector: data.reporter,
        category: '现场质检',
        status: 'failed',
        issues: [newIssue],
        inspections: [],
      };
      setQualityChecks(prev => [newCheck, ...prev]);
    }

    setIssueReportModalOpen(false);
    message.success('质量问题上报成功');
  };

  // Handle issue resolution submission
  const handleResolutionSubmit = (data: any) => {
    if (!selectedIssue) return;

    setQualityChecks(prev => {
      return prev.map(check => {
        const issueIndex = check.issues.findIndex(i => i.id === selectedIssue.id);
        if (issueIndex >= 0) {
          const updatedIssues = [...check.issues];
          updatedIssues[issueIndex] = {
            ...updatedIssues[issueIndex],
            status: 'resolved',
            resolution: data,
          };

          // If all issues are resolved, mark check as passed
          const allResolved = updatedIssues.every(i => i.status === 'resolved');

          return {
            ...check,
            issues: updatedIssues,
            status: allResolved ? 'passed' : 'failed',
          };
        }
        return check;
      });
    });

    setResolutionModalOpen(false);
    setSelectedIssue(null);
    message.success('整改处理已提交');
  };

  // Open resolution modal for an issue
  const handleOpenResolution = (issue: QualityIssue) => {
    setSelectedIssue(issue);
    setResolutionModalOpen(true);
  };

  // 计算项目质检统计
  const calculateQualityStats = (projectName: string) => {
    const checks = getProjectQualityChecks(projectName);
    const passedCount = checks.filter(c => c.status === 'passed').length;
    const failedCount = checks.filter(c => c.status === 'failed').length;
    const pendingCount = checks.filter(c => c.status === 'pending').length;

    // 统计待处理的问题数（status为'pending'的问题）
    const pendingIssues = checks.reduce((sum, c) => {
      const pendingInCheck = c.issues.filter(issue => issue.status === 'pending').length;
      return sum + pendingInCheck;
    }, 0);

    const passRate = checks.length > 0 ? ((passedCount / checks.length) * 100).toFixed(0) : '0';

    return {
      total: checks.length,
      passed: passedCount,
      failed: failedCount,
      pending: pendingCount,
      pendingIssues,
      passRate,
    };
  };

  return (
    <Layout>
      <Space direction="vertical" size="large" style={{ width: '100%' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <div>
            <Title level={2}>质量检查</Title>
            <Text type="secondary">跟踪进行中项目的质量验收</Text>
          </div>
          <Button
            type="primary"
            icon={<PlusOutlined />}
            onClick={() => setIssueReportModalOpen(true)}
            disabled={!selectedProject}
          >
            问题上报
          </Button>
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
                    const stats = calculateQualityStats(project.projectName);
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

                          <Divider style={{ margin: '8px 0' }} />

                          <Row gutter={8}>
                            <Col span={8}>
                              <div style={{ textAlign: 'center' }}>
                                <Text type="secondary" style={{ fontSize: 12, display: 'block' }}>总检查</Text>
                                <Text strong style={{ fontSize: 18, color: '#1677ff' }}>{stats.total}</Text>
                              </div>
                            </Col>
                            <Col span={8}>
                              <div style={{ textAlign: 'center' }}>
                                <Text type="secondary" style={{ fontSize: 12, display: 'block' }}>通过</Text>
                                <Text strong style={{ fontSize: 18, color: '#52c41a' }}>{stats.passed}</Text>
                              </div>
                            </Col>
                            <Col span={8}>
                              <div style={{ textAlign: 'center' }}>
                                <Text type="secondary" style={{ fontSize: 12, display: 'block' }}>问题</Text>
                                <Text strong style={{ fontSize: 18, color: '#f5222d' }}>{stats.failed}</Text>
                              </div>
                            </Col>
                          </Row>

                          {stats.pendingIssues > 0 && (
                            <Alert
                              message={`${stats.pendingIssues} 个问题待处理`}
                              type="error"
                              showIcon
                              style={{ padding: '4px 8px', fontSize: 12 }}
                            />
                          )}
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

          {/* 右侧：选中项目的质检日志 */}
          <Col xs={24} lg={16}>
            <Card
              title={
                selectedProject ? (
                  <Space>
                    <FileProtectOutlined />
                    <span>{selectedProject.projectName} - 质检记录</span>
                  </Space>
                ) : (
                  <span>质量检查记录</span>
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
                        <CalendarOutlined style={{ color: '#1677ff' }} />
                        <Text strong>施工周期：</Text>
                        <Text>{selectedProject.startDate} 至 {selectedProject.endDate}</Text>
                      </Space>
                    </Space>
                  </div>

                  {/* 质检统计 */}
                  {(() => {
                    const stats = calculateQualityStats(selectedProject.projectName);
                    return (
                      <Row gutter={16}>
                        <Col span={6}>
                          <Card size="small" style={{ background: '#f0f5ff', textAlign: 'center', borderColor: '#1677ff' }}>
                            <Statistic
                              title="总检查"
                              value={stats.total}
                              suffix="次"
                              valueStyle={{ fontSize: 28, color: '#1677ff' }}
                            />
                          </Card>
                        </Col>
                        <Col span={6}>
                          <Card size="small" style={{ background: '#f6ffed', textAlign: 'center', borderColor: '#52c41a' }}>
                            <Statistic
                              title="通过率"
                              value={stats.passRate}
                              suffix="%"
                              valueStyle={{ fontSize: 28, color: '#52c41a' }}
                            />
                          </Card>
                        </Col>
                        <Col span={6}>
                          <Card size="small" style={{ background: '#fff1f0', textAlign: 'center', borderColor: '#f5222d' }}>
                            <Statistic
                              title="不通过"
                              value={stats.failed}
                              suffix="次"
                              valueStyle={{ fontSize: 28, color: '#f5222d' }}
                            />
                          </Card>
                        </Col>
                        <Col span={6}>
                          <Card size="small" style={{ background: '#fffbe6', textAlign: 'center', borderColor: '#faad14' }}>
                            <Statistic
                              title="待检查"
                              value={stats.pending}
                              suffix="项"
                              valueStyle={{ fontSize: 28, color: '#faad14' }}
                            />
                          </Card>
                        </Col>
                      </Row>
                    );
                  })()}

                  {/* 质检记录时间轴 */}
                  <div>
                    <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 16 }}>
                      <FileProtectOutlined style={{ fontSize: 18, color: '#1677ff' }} />
                      <Text strong style={{ fontSize: 16 }}>质检记录时间轴</Text>
                    </div>

                    {(() => {
                      const checks = getProjectQualityChecks(selectedProject.projectName);
                      if (checks.length === 0) {
                        return <Empty description="暂无质检记录" />;
                      }

                      return (
                        <Timeline
                          items={checks.map((check) => ({
                            color:
                              check.status === 'passed' ? 'green' :
                              check.status === 'failed' ? 'red' : 'blue',
                            dot:
                              check.status === 'passed' ? <CheckCircleOutlined style={{ fontSize: 16 }} /> :
                              check.status === 'failed' ? <CloseCircleOutlined style={{ fontSize: 16 }} /> :
                              <ClockCircleOutlined style={{ fontSize: 16 }} />,
                            children: (
                              <Card size="small" hoverable style={{ marginBottom: 8 }}>
                                <Space direction="vertical" size="middle" style={{ width: '100%' }}>
                                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                                    <div>
                                      <Text strong style={{ fontSize: 15 }}>{check.category}</Text>
                                      <div style={{ marginTop: 4 }}>
                                        <Space size="small">
                                          <CalendarOutlined style={{ color: '#999' }} />
                                          <Text type="secondary" style={{ fontSize: 13 }}>
                                            {check.checkDate}
                                          </Text>
                                        </Space>
                                        <Text type="secondary" style={{ margin: '0 8px' }}>|</Text>
                                        <Space size="small">
                                          <UserOutlined style={{ color: '#999' }} />
                                          <Text type="secondary" style={{ fontSize: 13 }}>
                                            {check.inspector}
                                          </Text>
                                        </Space>
                                      </div>
                                    </div>
                                    <Tag
                                      color={
                                        check.status === 'passed' ? 'success' :
                                        check.status === 'failed' ? 'error' : 'warning'
                                      }
                                      style={{ fontSize: 13 }}
                                    >
                                      {check.status === 'passed' ? '✓ 通过' :
                                       check.status === 'failed' ? '✗ 不通过' : '⏱ 待检查'}
                                    </Tag>
                                  </div>

                                  {check.status === 'passed' && (
                                    <Alert
                                      message="质量检查通过"
                                      description="施工质量符合标准，可进入下一阶段"
                                      type="success"
                                      showIcon
                                      icon={<CheckCircleOutlined />}
                                    />
                                  )}

                                  {check.status === 'failed' && check.issues.length > 0 && (
                                    <div>
                                      <Alert
                                        message={`发现 ${check.issues.length} 个质量问题`}
                                        type="error"
                                        showIcon
                                        icon={<WarningOutlined />}
                                        style={{ marginBottom: 12 }}
                                      />
                                      <Collapse
                                        size="small"
                                        items={check.issues.map((issue: QualityIssue) => ({
                                          key: issue.id,
                                          label: (
                                            <Space>
                                              <WarningOutlined style={{ color: issue.status === 'resolved' ? '#52c41a' : '#f5222d' }} />
                                              <Text strong>{issue.title}</Text>
                                              <Tag color={issue.status === 'resolved' ? 'success' : 'error'}>
                                                {issue.status === 'resolved' ? '已整改' : '待整改'}
                                              </Tag>
                                            </Space>
                                          ),
                                          children: (
                                            <Space direction="vertical" style={{ width: '100%' }} size="middle">
                                              {/* Original Issue */}
                                              <div>
                                                <Text strong style={{ display: 'block', marginBottom: 8 }}>问题描述</Text>
                                                <Space direction="vertical" style={{ width: '100%' }} size="small">
                                                  <Text>{issue.description}</Text>
                                                  <div>
                                                    <Text type="secondary">上报时间：</Text>
                                                    <Text>{issue.reportTime}</Text>
                                                  </div>
                                                  <div>
                                                    <Text type="secondary">上报人：</Text>
                                                    <Text>{issue.reporter}</Text>
                                                  </div>
                                                  {issue.images && issue.images.length > 0 && (
                                                    <div>
                                                      <Text type="secondary" style={{ display: 'block', marginBottom: 8 }}>
                                                        <FileImageOutlined /> 问题照片 ({issue.images.length})
                                                      </Text>
                                                      <Image.PreviewGroup>
                                                        <Space wrap>
                                                          {issue.images.map((img, idx) => (
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
                                              </div>

                                              {/* Resolution Info */}
                                              {issue.resolution && (
                                                <>
                                                  <Divider style={{ margin: '8px 0' }} />
                                                  <div>
                                                    <Text strong style={{ display: 'block', marginBottom: 8, color: '#52c41a' }}>
                                                      <ToolOutlined /> 整改处理
                                                    </Text>
                                                    <Space direction="vertical" style={{ width: '100%' }} size="small">
                                                      <div>
                                                        <Text type="secondary">整改说明：</Text>
                                                        <Text>{issue.resolution.description}</Text>
                                                      </div>
                                                      <div>
                                                        <Text type="secondary">整改时间：</Text>
                                                        <Text>{issue.resolution.resolveTime}</Text>
                                                      </div>
                                                      <div>
                                                        <Text type="secondary">整改人：</Text>
                                                        <Text>{issue.resolution.resolver}</Text>
                                                      </div>
                                                      {issue.resolution.images && issue.resolution.images.length > 0 && (
                                                        <div>
                                                          <Text type="secondary" style={{ display: 'block', marginBottom: 8 }}>
                                                            <FileImageOutlined /> 整改照片 ({issue.resolution.images.length})
                                                          </Text>
                                                          <Image.PreviewGroup>
                                                            <Space wrap>
                                                              {issue.resolution.images.map((img, idx) => (
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
                                                  </div>
                                                </>
                                              )}

                                              {/* Action Button */}
                                              {issue.status === 'pending' && (
                                                <Button
                                                  type="primary"
                                                  icon={<ToolOutlined />}
                                                  size="small"
                                                  onClick={() => handleOpenResolution(issue)}
                                                >
                                                  提交整改
                                                </Button>
                                              )}
                                            </Space>
                                          ),
                                        }))}
                                      />
                                    </div>
                                  )}

                                  {check.status === 'pending' && (
                                    <Alert
                                      message="等待质检"
                                      description="等待质检员进行现场检查"
                                      type="warning"
                                      showIcon
                                      icon={<ClockCircleOutlined />}
                                    />
                                  )}

                                </Space>
                              </Card>
                            ),
                          }))}
                        />
                      );
                    })()}
                  </div>
                </Space>
              ) : (
                <Empty
                  description="请在左侧选择一个项目查看质检记录"
                  style={{ marginTop: 100 }}
                />
              )}
            </Card>
          </Col>
        </Row>
      </Space>

      {/* Quality Issue Report Modal */}
      <QualityIssueReport
        open={issueReportModalOpen}
        onCancel={() => setIssueReportModalOpen(false)}
        onSubmit={handleIssueReportSubmit}
        projectName={selectedProject?.projectName || ''}
      />

      {/* Issue Resolution Modal */}
      <IssueResolution
        open={resolutionModalOpen}
        onCancel={() => {
          setResolutionModalOpen(false);
          setSelectedIssue(null);
        }}
        onSubmit={handleResolutionSubmit}
        issue={selectedIssue}
      />
    </Layout>
  );
};

export default Quality;
