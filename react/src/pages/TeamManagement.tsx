import { useState } from 'react';
import { Card, Button, Table, Space, Tag, Avatar, Modal, Form, Input, Select, Upload, message, Row, Col, Typography } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined, UserOutlined, PhoneOutlined, ProjectOutlined, UploadOutlined } from '@ant-design/icons';
import Layout from '@/components/Layout';
import type { UploadFile } from 'antd/es/upload/interface';
import { TEAM_ROLES } from '@/constants';
import type { TeamRole } from '@/constants';

const { Title, Text } = Typography;

export interface TeamMember {
  id: string;
  name: string;
  role: TeamRole;
  phone: string;
  avatar?: string;
  projectIds: string[];
}

const TeamManagement = () => {
  const [members, setMembers] = useState<TeamMember[]>([
    {
      id: '1',
      name: '张设计',
      role: '设计师',
      phone: '13800138001',
      avatar: '',
      projectIds: ['1', '2'],
    },
    {
      id: '2',
      name: '李经理',
      role: '项目经理',
      phone: '13800138002',
      avatar: '',
      projectIds: ['1'],
    },
    {
      id: '3',
      name: '王工长',
      role: '工长',
      phone: '13800138003',
      avatar: '',
      projectIds: ['1', '3'],
    },
    {
      id: '4',
      name: '赵监理',
      role: '监理',
      phone: '13800138004',
      avatar: '',
      projectIds: ['2'],
    },
  ]);

  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingMember, setEditingMember] = useState<TeamMember | null>(null);
  const [form] = Form.useForm();
  const [fileList, setFileList] = useState<UploadFile[]>([]);


  // 打开添加/编辑弹窗
  const handleOpenModal = (member?: TeamMember) => {
    if (member) {
      setEditingMember(member);
      form.setFieldsValue({
        name: member.name,
        role: member.role,
        phone: member.phone,
      });
      if (member.avatar) {
        setFileList([{
          uid: '-1',
          name: 'avatar.png',
          status: 'done',
          url: member.avatar,
        }]);
      }
    } else {
      setEditingMember(null);
      form.resetFields();
      setFileList([]);
    }
    setIsModalOpen(true);
  };

  // 关闭弹窗
  const handleCloseModal = () => {
    setIsModalOpen(false);
    setEditingMember(null);
    form.resetFields();
    setFileList([]);
  };

  // 保存成员
  const handleSave = async () => {
    try {
      const values = await form.validateFields();
      const avatar = fileList[0]?.url || fileList[0]?.thumbUrl || '';

      if (editingMember) {
        // 编辑
        const updatedMember: TeamMember = {
          ...editingMember,
          name: values.name,
          role: values.role,
          phone: values.phone,
          avatar,
        };
        setMembers(members.map(m => m.id === editingMember.id ? updatedMember : m));
        message.success('成员信息已更新');
      } else {
        // 新增
        const newMember: TeamMember = {
          id: Date.now().toString(),
          name: values.name,
          role: values.role,
          phone: values.phone,
          avatar,
          projectIds: [],
        };
        setMembers([...members, newMember]);
        message.success('成员已添加');
      }

      handleCloseModal();
    } catch (error) {
      // 验证失败
    }
  };

  // 删除成员
  const handleDelete = (member: TeamMember) => {
    Modal.confirm({
      title: '确认删除',
      content: `确定要删除成员"${member.name}"吗？`,
      okText: '确定',
      cancelText: '取消',
      okType: 'danger',
      onOk: () => {
        setMembers(members.filter(m => m.id !== member.id));
        message.success('成员已删除');
      },
    });
  };

  // 处理上传
  const handleUploadChange = ({ fileList: newFileList }: any) => {
    setFileList(newFileList.slice(-1)); // 只保留最新的一个文件
  };

  // 按角色统计
  const getRoleCount = (role: TeamMember['role']) => {
    return members.filter(m => m.role === role).length;
  };

  const columns = [
    {
      title: '头像',
      dataIndex: 'avatar',
      key: 'avatar',
      width: 80,
      render: (avatar: string, record: TeamMember) => (
        <Avatar
          size={48}
          src={avatar}
          icon={<UserOutlined />}
          style={{ backgroundColor: TEAM_ROLES[record.role].color }}
        >
          {!avatar && record.name.slice(0, 1)}
        </Avatar>
      ),
    },
    {
      title: '姓名',
      dataIndex: 'name',
      key: 'name',
      width: 120,
      render: (name: string) => <Text strong>{name}</Text>,
    },
    {
      title: '角色',
      dataIndex: 'role',
      key: 'role',
      width: 120,
      render: (role: TeamMember['role']) => (
        <Tag color={TEAM_ROLES[role].color}>
          {role}
        </Tag>
      ),
    },
    {
      title: '联系方式',
      dataIndex: 'phone',
      key: 'phone',
      width: 150,
      render: (phone: string) => (
        <Space>
          <PhoneOutlined style={{ color: '#1677ff' }} />
          <Text>{phone}</Text>
        </Space>
      ),
    },
    {
      title: '参与项目',
      dataIndex: 'projectIds',
      key: 'projectIds',
      render: (projectIds: string[]) => (
        <Space>
          <ProjectOutlined style={{ color: '#52c41a' }} />
          <Text>{projectIds.length} 个项目</Text>
        </Space>
      ),
    },
    {
      title: '操作',
      key: 'action',
      width: 150,
      render: (_: any, record: TeamMember) => (
        <Space>
          <Button
            type="link"
            icon={<EditOutlined />}
            onClick={() => handleOpenModal(record)}
          >
            编辑
          </Button>
          <Button
            type="link"
            danger
            icon={<DeleteOutlined />}
            onClick={() => handleDelete(record)}
          >
            删除
          </Button>
        </Space>
      ),
    },
  ];

  return (
    <Layout>
      <Space direction="vertical" size="large" style={{ width: '100%' }}>
        {/* 页面标题 */}
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <div>
            <Title level={2}>人员管理</Title>
            <Text type="secondary">管理团队成员信息</Text>
          </div>
          <Button
            type="primary"
            icon={<PlusOutlined />}
            onClick={() => handleOpenModal()}
          >
            添加成员
          </Button>
        </div>

        {/* 统计卡片 */}
        <Row gutter={16}>
          <Col span={6}>
            <Card>
              <Space direction="vertical" size="small">
                <Text type="secondary">设计师</Text>
                <div style={{ fontSize: 24, fontWeight: 'bold', color: '#1677ff' }}>
                  {getRoleCount('设计师')}
                </div>
              </Space>
            </Card>
          </Col>
          <Col span={6}>
            <Card>
              <Space direction="vertical" size="small">
                <Text type="secondary">项目经理</Text>
                <div style={{ fontSize: 24, fontWeight: 'bold', color: '#52c41a' }}>
                  {getRoleCount('项目经理')}
                </div>
              </Space>
            </Card>
          </Col>
          <Col span={6}>
            <Card>
              <Space direction="vertical" size="small">
                <Text type="secondary">工长</Text>
                <div style={{ fontSize: 24, fontWeight: 'bold', color: '#faad14' }}>
                  {getRoleCount('工长')}
                </div>
              </Space>
            </Card>
          </Col>
          <Col span={6}>
            <Card>
              <Space direction="vertical" size="small">
                <Text type="secondary">监理</Text>
                <div style={{ fontSize: 24, fontWeight: 'bold', color: '#722ed1' }}>
                  {getRoleCount('监理')}
                </div>
              </Space>
            </Card>
          </Col>
        </Row>

        {/* 成员列表 */}
        <Card>
          <Table
            columns={columns}
            dataSource={members}
            rowKey="id"
            pagination={{ pageSize: 10 }}
          />
        </Card>
      </Space>

      {/* 添加/编辑成员弹窗 */}
      <Modal
        title={editingMember ? '编辑成员' : '添加成员'}
        open={isModalOpen}
        onOk={handleSave}
        onCancel={handleCloseModal}
        width={600}
        okText="保存"
        cancelText="取消"
      >
        <Form
          form={form}
          layout="vertical"
          style={{ marginTop: 24 }}
        >
          <Form.Item
            name="avatar"
            label="头像（可选）"
          >
            <Upload
              listType="picture-card"
              fileList={fileList}
              onChange={handleUploadChange}
              beforeUpload={() => false} // 阻止自动上传
              maxCount={1}
            >
              {fileList.length === 0 && (
                <div>
                  <UploadOutlined />
                  <div style={{ marginTop: 8 }}>上传头像</div>
                </div>
              )}
            </Upload>
          </Form.Item>

          <Form.Item
            name="name"
            label="姓名"
            rules={[{ required: true, message: '请输入姓名' }]}
          >
            <Input placeholder="请输入姓名" size="large" />
          </Form.Item>

          <Form.Item
            name="role"
            label="角色"
            rules={[{ required: true, message: '请选择角色' }]}
          >
            <Select placeholder="请选择角色" size="large">
              <Select.Option value="设计师">设计师</Select.Option>
              <Select.Option value="项目经理">项目经理</Select.Option>
              <Select.Option value="工长">工长</Select.Option>
              <Select.Option value="监理">监理</Select.Option>
            </Select>
          </Form.Item>

          <Form.Item
            name="phone"
            label="联系方式"
            rules={[
              { required: true, message: '请输入联系方式' },
              { pattern: /^1[3-9]\d{9}$/, message: '请输入有效的手机号码' },
            ]}
          >
            <Input placeholder="请输入手机号码" size="large" />
          </Form.Item>
        </Form>
      </Modal>
    </Layout>
  );
};

export default TeamManagement;
