import { Modal, Form, Select, Space, message, Card, Avatar, Tag, Typography, Empty } from 'antd';
import { UserOutlined } from '@ant-design/icons';
import { useState, useEffect } from 'react';
import type { TeamMember } from '@/pages/TeamManagement';
import { TEAM_ROLES } from '@/constants';

const { Text } = Typography;

interface TeamAssignmentProps {
  open: boolean;
  onCancel: () => void;
  onSave: (assignment: ProjectTeam) => void;
  initialData?: ProjectTeam;
  projectName: string;
  allMembers: TeamMember[];
}

export interface ProjectTeam {
  designers: string[]; // 设计师IDs
  projectManagers: string[]; // 项目经理IDs
  foremen: string[]; // 工长IDs
  supervisors: string[]; // 监理IDs
}

const TeamAssignment = ({
  open,
  onCancel,
  onSave,
  initialData,
  projectName,
  allMembers,
}: TeamAssignmentProps) => {
  const [form] = Form.useForm();

  useEffect(() => {
    if (open && initialData) {
      form.setFieldsValue({
        designers: initialData.designers,
        projectManagers: initialData.projectManagers,
        foremen: initialData.foremen,
        supervisors: initialData.supervisors,
      });
    } else if (open) {
      form.resetFields();
    }
  }, [open, initialData, form]);

  const handleSave = async () => {
    try {
      const values = await form.validateFields();

      const assignment: ProjectTeam = {
        designers: values.designers || [],
        projectManagers: values.projectManagers || [],
        foremen: values.foremen || [],
        supervisors: values.supervisors || [],
      };

      onSave(assignment);
      message.success('团队成员已分配');
    } catch (error) {
      // 验证失败
    }
  };

  const handleModalCancel = () => {
    form.resetFields();
    onCancel();
  };

  // 按角色筛选成员
  const getMembersByRole = (role: TeamMember['role']) => {
    return allMembers.filter(m => m.role === role);
  };

  // 渲染成员选项
  const renderMemberOption = (member: TeamMember) => (
    <Select.Option key={member.id} value={member.id}>
      <Space>
        <Avatar size={24} src={member.avatar} icon={<UserOutlined />}>
          {!member.avatar && member.name.slice(0, 1)}
        </Avatar>
        <span>{member.name}</span>
        <Text type="secondary" style={{ fontSize: 12 }}>
          {member.phone}
        </Text>
      </Space>
    </Select.Option>
  );


  return (
    <Modal
      title={
        <Space>
          <UserOutlined style={{ color: '#1677ff' }} />
          <span>{projectName} - 团队成员分配</span>
        </Space>
      }
      open={open}
      onCancel={handleModalCancel}
      onOk={handleSave}
      width={800}
      okText="保存分配"
      cancelText="取消"
    >
      <div style={{ marginTop: 16 }}>
        {allMembers.length === 0 ? (
          <Empty
            description="暂无团队成员，请先在人员管理中添加成员"
            style={{ padding: '40px 0' }}
          />
        ) : (
          <Form form={form} layout="vertical">
            <Space direction="vertical" size="large" style={{ width: '100%' }}>
              {/* 设计师 */}
              <Card size="small" style={{ background: '#f0f5ff' }}>
                <Form.Item
                  name="designers"
                  label={
                    <Space>
                      <Tag color={TEAM_ROLES['设计师'].color}>设计师</Tag>
                      <Text type="secondary" style={{ fontSize: 12 }}>
                        （可多选）
                      </Text>
                    </Space>
                  }
                  style={{ marginBottom: 0 }}
                >
                  <Select
                    mode="multiple"
                    placeholder="请选择设计师"
                    size="large"
                    maxTagCount="responsive"
                    allowClear
                  >
                    {getMembersByRole('设计师').map(renderMemberOption)}
                  </Select>
                </Form.Item>
              </Card>

              {/* 项目经理 */}
              <Card size="small" style={{ background: '#f6ffed' }}>
                <Form.Item
                  name="projectManagers"
                  label={
                    <Space>
                      <Tag color={TEAM_ROLES['项目经理'].color}>项目经理</Tag>
                      <Text type="secondary" style={{ fontSize: 12 }}>
                        （可多选）
                      </Text>
                    </Space>
                  }
                  style={{ marginBottom: 0 }}
                >
                  <Select
                    mode="multiple"
                    placeholder="请选择项目经理"
                    size="large"
                    maxTagCount="responsive"
                    allowClear
                  >
                    {getMembersByRole('项目经理').map(renderMemberOption)}
                  </Select>
                </Form.Item>
              </Card>

              {/* 工长 */}
              <Card size="small" style={{ background: '#fff7e6' }}>
                <Form.Item
                  name="foremen"
                  label={
                    <Space>
                      <Tag color={TEAM_ROLES['工长'].color}>工长</Tag>
                      <Text type="secondary" style={{ fontSize: 12 }}>
                        （可多选）
                      </Text>
                    </Space>
                  }
                  style={{ marginBottom: 0 }}
                >
                  <Select
                    mode="multiple"
                    placeholder="请选择工长"
                    size="large"
                    maxTagCount="responsive"
                    allowClear
                  >
                    {getMembersByRole('工长').map(renderMemberOption)}
                  </Select>
                </Form.Item>
              </Card>

              {/* 监理 */}
              <Card size="small" style={{ background: '#f9f0ff' }}>
                <Form.Item
                  name="supervisors"
                  label={
                    <Space>
                      <Tag color={TEAM_ROLES['监理'].color}>监理</Tag>
                      <Text type="secondary" style={{ fontSize: 12 }}>
                        （可多选）
                      </Text>
                    </Space>
                  }
                  style={{ marginBottom: 0 }}
                >
                  <Select
                    mode="multiple"
                    placeholder="请选择监理"
                    size="large"
                    maxTagCount="responsive"
                    allowClear
                  >
                    {getMembersByRole('监理').map(renderMemberOption)}
                  </Select>
                </Form.Item>
              </Card>
            </Space>
          </Form>
        )}
      </div>
    </Modal>
  );
};

export default TeamAssignment;
