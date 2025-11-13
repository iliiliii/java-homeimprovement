import { Modal, Form, Input, DatePicker, Select, Button, Timeline, Space, message, Popconfirm, Tag } from 'antd';
import { PlusOutlined, DeleteOutlined, CalendarOutlined, CheckCircleOutlined, ClockCircleOutlined } from '@ant-design/icons';
import { useState, useEffect } from 'react';
import dayjs from 'dayjs';
import { CONSTRUCTION_PHASES, TIMELINE_STATUS } from '@/constants';
import { getTimelineStatusConfig, calculatePercentage } from '@/utils/helpers';

const { TextArea } = Input;

interface TimelineItem {
  id: string;
  title: string;
  description: string;
  date: string;
  status: 'pending' | 'inProgress' | 'completed';
}

interface TimelineManagementProps {
  open: boolean;
  onCancel: () => void;
  onSave: (timeline: TimelineItem[]) => void;
  initialData?: TimelineItem[];
  projectName: string;
}

const TimelineManagement = ({
  open,
  onCancel,
  onSave,
  initialData = [],
  projectName,
}: TimelineManagementProps) => {
  const [timelineItems, setTimelineItems] = useState<TimelineItem[]>(initialData);
  const [form] = Form.useForm();
  const [isAddingNew, setIsAddingNew] = useState(false);

  const handleAddTimeline = async () => {
    try {
      const values = await form.validateFields();
      const newItem: TimelineItem = {
        id: Date.now().toString(),
        title: values.title,
        description: values.description,
        date: values.date.format('YYYY-MM-DD'),
        status: values.status || 'pending',
      };

      setTimelineItems([...timelineItems, newItem]);
      form.resetFields();
      setIsAddingNew(false);
      message.success('施工阶段已添加');
    } catch (error) {
      // 验证失败
    }
  };

  const handleDelete = (id: string) => {
    setTimelineItems(timelineItems.filter(t => t.id !== id));
    message.success('施工阶段已删除');
  };

  const handleUpdateStatus = (id: string, status: 'pending' | 'inProgress' | 'completed') => {
    setTimelineItems(timelineItems.map(item =>
      item.id === id ? { ...item, status } : item
    ));
    message.success('状态已更新');
  };

  const handleSave = () => {
    if (timelineItems.length === 0) {
      message.warning('请至少添加一个施工阶段');
      return;
    }
    onSave(timelineItems);
    onCancel();
  };


  const completedCount = timelineItems.filter(t => t.status === 'completed').length;
  const inProgressCount = timelineItems.filter(t => t.status === 'inProgress').length;

  return (
    <Modal
      title={
        <Space>
          <CalendarOutlined style={{ color: '#1677ff' }} />
          <span>{projectName} - 施工进度管理</span>
        </Space>
      }
      open={open}
      onCancel={onCancel}
      onOk={handleSave}
      width={900}
      okText="保存进度"
      cancelText="取消"
    >
      <Space direction="vertical" size="large" style={{ width: '100%' }}>
        {/* 进度统计 */}
        <div
          style={{
            background: '#f0f5ff',
            border: '1px solid #adc6ff',
            borderRadius: '8px',
            padding: '16px',
          }}
        >
          <Space size="large" style={{ width: '100%', justifyContent: 'space-around' }}>
            <div style={{ textAlign: 'center' }}>
              <div style={{ fontSize: 14, color: '#999' }}>总阶段</div>
              <div style={{ fontSize: 28, color: '#1677ff', fontWeight: 'bold', marginTop: 4 }}>
                {timelineItems.length}
              </div>
            </div>
            <div style={{ textAlign: 'center' }}>
              <div style={{ fontSize: 14, color: '#999' }}>已完成</div>
              <div style={{ fontSize: 28, color: '#52c41a', fontWeight: 'bold', marginTop: 4 }}>
                {completedCount}
              </div>
            </div>
            <div style={{ textAlign: 'center' }}>
              <div style={{ fontSize: 14, color: '#999' }}>进行中</div>
              <div style={{ fontSize: 28, color: '#1677ff', fontWeight: 'bold', marginTop: 4 }}>
                {inProgressCount}
              </div>
            </div>
            <div style={{ textAlign: 'center' }}>
              <div style={{ fontSize: 14, color: '#999' }}>完成度</div>
              <div style={{ fontSize: 28, color: '#722ed1', fontWeight: 'bold', marginTop: 4 }}>
                {calculatePercentage(completedCount, timelineItems.length)}%
              </div>
            </div>
          </Space>
        </div>

        {/* 施工时间轴 */}
        {timelineItems.length > 0 && (
          <div style={{ maxHeight: '400px', overflowY: 'auto', padding: '0 16px' }}>
            <Timeline
              items={timelineItems.map((item) => ({
                color: item.status === 'completed' ? 'green' : item.status === 'inProgress' ? 'blue' : 'gray',
                dot:
                  item.status === 'completed' ? <CheckCircleOutlined style={{ fontSize: 16 }} /> :
                  item.status === 'inProgress' ? <ClockCircleOutlined style={{ fontSize: 16 }} /> :
                  undefined,
                children: (
                  <div
                    style={{
                      background: '#fafafa',
                      padding: '12px',
                      borderRadius: '8px',
                      marginBottom: '12px',
                    }}
                  >
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                      <div style={{ flex: 1 }}>
                        <div style={{ marginBottom: 8 }}>
                          <span style={{ fontSize: 15, fontWeight: 'bold', marginRight: 8 }}>
                            {item.title}
                          </span>
                          <Tag color={getTimelineStatusConfig(item.status).color}>
                            {getTimelineStatusConfig(item.status).label}
                          </Tag>
                        </div>
                        <div style={{ color: '#666', marginBottom: 8 }}>{item.description}</div>
                        <div style={{ fontSize: 13, color: '#999' }}>
                          <CalendarOutlined style={{ marginRight: 4 }} />
                          {item.date}
                        </div>
                      </div>
                      <Space>
                        <Select
                          value={item.status}
                          size="small"
                          style={{ width: 100 }}
                          onChange={(status) => handleUpdateStatus(item.id, status)}
                        >
                          <Select.Option value="pending">待开始</Select.Option>
                          <Select.Option value="inProgress">进行中</Select.Option>
                          <Select.Option value="completed">已完成</Select.Option>
                        </Select>
                        <Popconfirm
                          title="确认删除"
                          description="确定要删除这个施工阶段吗？"
                          onConfirm={() => handleDelete(item.id)}
                          okText="确定"
                          cancelText="取消"
                        >
                          <Button
                            type="link"
                            danger
                            size="small"
                            icon={<DeleteOutlined />}
                          />
                        </Popconfirm>
                      </Space>
                    </div>
                  </div>
                ),
              }))}
            />
          </div>
        )}

        {/* 添加施工阶段表单 */}
        {isAddingNew ? (
          <div
            style={{
              background: '#f5f5f5',
              padding: '16px',
              borderRadius: '8px',
            }}
          >
            <Form form={form} layout="vertical">
              <Form.Item
                name="title"
                label="施工阶段"
                rules={[{ required: true, message: '请选择施工阶段' }]}
              >
                <Select placeholder="选择施工阶段" size="large">
                  {CONSTRUCTION_PHASES.map(cat => (
                    <Select.Option key={cat.id} value={cat.name}>
                      {cat.name}
                    </Select.Option>
                  ))}
                </Select>
              </Form.Item>

              <Form.Item
                name="description"
                label="阶段说明"
                rules={[{ required: true, message: '请输入阶段说明' }]}
              >
                <TextArea
                  rows={3}
                  placeholder="描述该阶段的工作内容"
                />
              </Form.Item>

              <Space style={{ width: '100%' }} size="large">
                <Form.Item
                  name="date"
                  label="计划日期"
                  rules={[{ required: true, message: '请选择日期' }]}
                  style={{ marginBottom: 0, flex: 1 }}
                >
                  <DatePicker placeholder="选择日期" style={{ width: '100%' }} />
                </Form.Item>

                <Form.Item
                  name="status"
                  label="当前状态"
                  initialValue="pending"
                  style={{ marginBottom: 0, width: 120 }}
                >
                  <Select>
                    <Select.Option value="pending">待开始</Select.Option>
                    <Select.Option value="inProgress">进行中</Select.Option>
                    <Select.Option value="completed">已完成</Select.Option>
                  </Select>
                </Form.Item>
              </Space>

              <div style={{ marginTop: 16 }}>
                <Space>
                  <Button type="primary" onClick={handleAddTimeline}>
                    确认添加
                  </Button>
                  <Button onClick={() => { form.resetFields(); setIsAddingNew(false); }}>
                    取消
                  </Button>
                </Space>
              </div>
            </Form>
          </div>
        ) : (
          <Button
            type="dashed"
            block
            icon={<PlusOutlined />}
            onClick={() => setIsAddingNew(true)}
          >
            添加施工阶段
          </Button>
        )}
      </Space>
    </Modal>
  );
};

export default TimelineManagement;
