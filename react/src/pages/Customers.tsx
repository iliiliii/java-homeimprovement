import { useState } from 'react';
import { Card, Button, Badge, Space, Row, Col, Typography, Tag, Modal, message, Descriptions, Timeline } from 'antd';
import {
  PhoneOutlined,
  MailOutlined,
  EnvironmentOutlined,
  FolderOutlined,
  EyeOutlined,
  EditOutlined,
  CalendarOutlined,
  UserOutlined,
  ExclamationCircleOutlined,
  InfoCircleOutlined,
  HistoryOutlined,
} from '@ant-design/icons';
import Layout from '@/components/Layout';
import CustomerForm from '@/components/CustomerForm';
import { mockCustomers, mockProjects, Customer, Project } from '@/data/mockData';
import { getProjectStatusConfig, formatBudget } from '@/utils/helpers';

const { Title, Text } = Typography;
const { confirm } = Modal;

const Customers = () => {
  const [customers, setCustomers] = useState<Customer[]>(mockCustomers);
  const [projects] = useState<Project[]>(mockProjects);
  const [selectedCustomer, setSelectedCustomer] = useState<Customer | null>(null);
  const [showCustomerForm, setShowCustomerForm] = useState(false);
  const [editingCustomer, setEditingCustomer] = useState<Customer | null>(null);


  // 获取客户的项目列表
  const getCustomerProjects = (customerId: string) => {
    return projects.filter(p => p.customerId === customerId);
  };

  // 创建新客户
  const handleCreateCustomer = (data: any) => {
    const newCustomer: Customer = {
      id: Date.now().toString(),
      ...data,
      createdAt: new Date().toISOString().split('T')[0],
    };
    setCustomers(prev => [newCustomer, ...prev]);
    setShowCustomerForm(false);
    message.success('客户添加成功！');
  };

  // 编辑客户
  const handleEditCustomer = (data: any) => {
    if (!editingCustomer) return;

    const updatedCustomer: Customer = {
      ...editingCustomer,
      ...data,
    };

    setCustomers(prev => prev.map(c => c.id === editingCustomer.id ? updatedCustomer : c));
    setEditingCustomer(null);
    message.success('客户信息更新成功');

    // 如果当前正在查看该客户，也更新详情
    if (selectedCustomer?.id === editingCustomer.id) {
      setSelectedCustomer(updatedCustomer);
    }
  };

  // 删除客户
  const handleDeleteCustomer = (customer: Customer) => {
    const customerProjects = getCustomerProjects(customer.id);

    confirm({
      title: '确认删除客户',
      icon: <ExclamationCircleOutlined />,
      content: customerProjects.length > 0
        ? `客户 "${customer.name}" 有 ${customerProjects.length} 个关联项目，删除客户不会删除项目。确定要删除吗？`
        : `您确定要删除客户 "${customer.name}" 吗？此操作无法撤销。`,
      okText: '删除',
      okType: 'danger',
      cancelText: '取消',
      onOk() {
        setCustomers(prev => prev.filter(c => c.id !== customer.id));
        if (selectedCustomer?.id === customer.id) {
          setSelectedCustomer(null);
        }
        message.success('客户删除成功');
      },
    });
  };

  return (
    <Layout>
      <Space direction="vertical" size="large" style={{ width: '100%' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <div>
            <Title level={2}>客户管理</Title>
            <Text type="secondary">管理所有客户信息</Text>
          </div>
          <Button type="primary" onClick={() => setShowCustomerForm(true)}>
            添加客户
          </Button>
        </div>

        <Row gutter={[16, 16]}>
          {customers.map((customer) => {
            const customerProjects = getCustomerProjects(customer.id);
            const activeProjects = customerProjects.filter(p => p.status === 'inProgress');

            return (
              <Col xs={24} sm={12} lg={8} key={customer.id}>
                <Card
                  hoverable
                  actions={[
                    <Button
                      type="link"
                      icon={<EyeOutlined />}
                      onClick={() => setSelectedCustomer(customer)}
                    >
                      查看
                    </Button>,
                    <Button
                      type="link"
                      icon={<EditOutlined />}
                      onClick={() => setEditingCustomer(customer)}
                    >
                      编辑
                    </Button>,
                  ]}
                >
                  <Space direction="vertical" size="middle" style={{ width: '100%' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                      <div style={{ flex: 1 }}>
                        <Title level={5} style={{ margin: 0 }}>{customer.name}</Title>
                        <Text type="secondary" style={{ fontSize: 12 }}>
                          加入于 {customer.createdAt}
                        </Text>
                      </div>
                      {activeProjects.length > 0 && (
                        <Badge status="processing" text="活跃" />
                      )}
                    </div>

                    <Space direction="vertical" size="small" style={{ width: '100%' }}>
                      <Text type="secondary">
                        <PhoneOutlined /> {customer.phone}
                      </Text>
                      <Text type="secondary" ellipsis>
                        <MailOutlined /> {customer.email}
                      </Text>
                      <Text type="secondary" ellipsis>
                        <EnvironmentOutlined /> {customer.address}
                      </Text>
                    </Space>

                    <div>
                      <div
                        style={{
                          padding: '12px',
                          background: '#f0f5ff',
                          borderRadius: '4px',
                        }}
                      >
                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                          <Space size="small">
                            <FolderOutlined style={{ color: '#1677ff' }} />
                            <Text>关联项目</Text>
                          </Space>
                          <Text strong style={{ color: '#1677ff', fontSize: 18 }}>
                            {customerProjects.length}
                          </Text>
                        </div>
                        {activeProjects.length > 0 && (
                          <Text type="secondary" style={{ fontSize: 12, marginTop: 4, display: 'block' }}>
                            {activeProjects.length} 个项目进行中
                          </Text>
                        )}
                      </div>
                    </div>
                  </Space>
                </Card>
              </Col>
            );
          })}
        </Row>
      </Space>

      {/* 客户详情弹窗 */}
      <Modal
        title={
          <Space>
            <UserOutlined style={{ color: '#1677ff' }} />
            <span>{selectedCustomer?.name}</span>
          </Space>
        }
        open={!!selectedCustomer}
        onCancel={() => setSelectedCustomer(null)}
        width={1000}
        footer={null}
        style={{ top: 20 }}
      >
        {selectedCustomer && (
          <div style={{ maxHeight: 'calc(90vh - 150px)', overflowY: 'auto', padding: '0 4px' }}>
            <Space direction="vertical" size="middle" style={{ width: '100%' }}>
              {/* 客户操作 */}
              <Card size="small" style={{ background: '#f5f5f5' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <div>
                    <Text strong>客户操作</Text>
                    <br />
                    <Text type="secondary">编辑或删除客户信息</Text>
                  </div>
                  <Space>
                    <Button
                      icon={<EditOutlined />}
                      onClick={() => {
                        setEditingCustomer(selectedCustomer);
                        setSelectedCustomer(null);
                      }}
                    >
                      编辑客户
                    </Button>
                    <Button
                      danger
                      onClick={() => {
                        const customer = selectedCustomer;
                        setSelectedCustomer(null);
                        handleDeleteCustomer(customer);
                      }}
                    >
                      删除客户
                    </Button>
                  </Space>
                </div>
              </Card>

              {/* 客户基本信息 */}
              <Card
                size="small"
                title={
                  <Space>
                    <InfoCircleOutlined style={{ color: '#1677ff' }} />
                    <span>客户基本信息</span>
                  </Space>
                }
              >
                <Descriptions column={2} size="small">
                  <Descriptions.Item label="客户姓名" span={2}>
                    {selectedCustomer.name}
                  </Descriptions.Item>
                  <Descriptions.Item label="联系电话">
                    <Text copyable>{selectedCustomer.phone}</Text>
                  </Descriptions.Item>
                  <Descriptions.Item label="电子邮箱">
                    <Text copyable>{selectedCustomer.email}</Text>
                  </Descriptions.Item>
                  <Descriptions.Item label="联系地址" span={2}>
                    {selectedCustomer.address}
                  </Descriptions.Item>
                  <Descriptions.Item label="创建日期">
                    {selectedCustomer.createdAt}
                  </Descriptions.Item>
                </Descriptions>
              </Card>

              {/* 关联项目 */}
              <Card
                size="small"
                title={
                  <Space>
                    <HistoryOutlined style={{ color: '#52c41a' }} />
                    <span>关联项目</span>
                  </Space>
                }
              >
                {(() => {
                  const customerProjects = getCustomerProjects(selectedCustomer.id);

                  if (customerProjects.length > 0) {
                    return (
                      <>
                        {/* 项目统计 */}
                        <Row gutter={16} style={{ marginBottom: 16 }}>
                          <Col span={6}>
                            <Card size="small" style={{ textAlign: 'center', background: '#f0f5ff' }}>
                              <div style={{ fontSize: 24, fontWeight: 'bold', color: '#1677ff' }}>
                                {customerProjects.length}
                              </div>
                              <Text type="secondary" style={{ fontSize: 12 }}>总项目</Text>
                            </Card>
                          </Col>
                          <Col span={6}>
                            <Card size="small" style={{ textAlign: 'center', background: '#e6f7ff' }}>
                              <div style={{ fontSize: 24, fontWeight: 'bold', color: '#1677ff' }}>
                                {customerProjects.filter(p => p.status === 'inProgress').length}
                              </div>
                              <Text type="secondary" style={{ fontSize: 12 }}>进行中</Text>
                            </Card>
                          </Col>
                          <Col span={6}>
                            <Card size="small" style={{ textAlign: 'center', background: '#f6ffed' }}>
                              <div style={{ fontSize: 24, fontWeight: 'bold', color: '#52c41a' }}>
                                {customerProjects.filter(p => p.status === 'completed').length}
                              </div>
                              <Text type="secondary" style={{ fontSize: 12 }}>已完成</Text>
                            </Card>
                          </Col>
                          <Col span={6}>
                            <Card size="small" style={{ textAlign: 'center', background: '#f9f0ff' }}>
                              <div style={{ fontSize: 24, fontWeight: 'bold', color: '#722ed1' }}>
                                ¥{formatBudget(customerProjects.reduce((sum, p) => sum + p.totalBudget, 0))}万
                              </div>
                              <Text type="secondary" style={{ fontSize: 12 }}>总预算</Text>
                            </Card>
                          </Col>
                        </Row>

                        {/* 项目时间轴 */}
                        <Timeline
                          items={customerProjects.map((project) => {
                            const statusConfig = getProjectStatusConfig(project.status);
                            return {
                              color:
                                project.status === 'completed' ? 'green' :
                                project.status === 'inProgress' ? 'blue' : 'gray',
                              children: (
                                <div style={{ paddingBottom: 8 }}>
                                  <div style={{ marginBottom: 4 }}>
                                    <Text strong style={{ fontSize: 14 }}>{project.projectName}</Text>
                                    {' '}
                                    <Tag color={statusConfig.color} style={{ marginLeft: 8 }}>
                                      {statusConfig.label}
                                    </Tag>
                                  </div>
                                  <Text type="secondary" style={{ fontSize: 13 }}>{project.siteName}</Text>
                                  <br />
                                  <Text type="secondary" style={{ fontSize: 12 }}>
                                    <EnvironmentOutlined /> {project.siteAddress}
                                  </Text>
                                  <br />
                                  <Text type="secondary" style={{ fontSize: 12 }}>
                                    <CalendarOutlined /> {project.startDate} 至 {project.endDate}
                                  </Text>
                                  {project.totalBudget > 0 && (
                                    <>
                                      <br />
                                      <Text type="secondary" style={{ fontSize: 12 }}>
                                        预算：¥{project.totalBudget.toLocaleString()}
                                      </Text>
                                    </>
                                  )}
                                </div>
                              ),
                            };
                          })}
                        />
                      </>
                    );
                  } else {
                    return (
                      <div style={{ textAlign: 'center', padding: '40px 0' }}>
                        <FolderOutlined style={{ fontSize: 48, color: '#d9d9d9', marginBottom: 16 }} />
                        <br />
                        <Text type="secondary">该客户暂无关联项目</Text>
                      </div>
                    );
                  }
                })()}
              </Card>
            </Space>
          </div>
        )}
      </Modal>

      {/* 新建客户表单 */}
      <CustomerForm
        open={showCustomerForm}
        onCancel={() => setShowCustomerForm(false)}
        onSubmit={handleCreateCustomer}
      />

      {/* 编辑客户表单 */}
      {editingCustomer && (
        <CustomerForm
          open={!!editingCustomer}
          onCancel={() => setEditingCustomer(null)}
          onSubmit={handleEditCustomer}
          initialData={{
            name: editingCustomer.name,
            phone: editingCustomer.phone,
            email: editingCustomer.email,
            address: editingCustomer.address,
          }}
          title="编辑客户"
        />
      )}
    </Layout>
  );
};

export default Customers;
