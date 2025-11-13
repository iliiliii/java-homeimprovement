import { Card, Button, Space, Table, Tag, Typography, message, Modal, Form, Input, Select, DatePicker, Upload, Image } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined, FileTextOutlined, CalendarOutlined, SendOutlined, EyeOutlined, UploadOutlined, PictureOutlined } from '@ant-design/icons';
import Layout from '@/components/Layout';
import { useState } from 'react';
import dayjs from 'dayjs';

const { Title, Text } = Typography;
const { TextArea } = Input;

interface NewsItem {
  id: string;
  title: string;
  content: string;
  publishDate: string;
  status: 'draft' | 'published';
  position: 'banner' | 'news' | 'other';
  coverImage?: string;
}

const NewsSettings = () => {
  const [news, setNews] = useState<NewsItem[]>([
    {
      id: '1',
      title: '装修季大促销活动开始啦',
      content: '全场装修服务8折优惠，优质材料供应商合作，为您提供性价比最高的装修方案。活动时间有限，欢迎咨询！',
      publishDate: '2024-03-15',
      status: 'published',
      position: 'banner',
    },
    {
      id: '2',
      title: '新增智能家居安装服务',
      content: '我们现在提供全套智能家居解决方案，包括智能照明、智能门锁、智能窗帘等',
      publishDate: '2024-03-10',
      status: 'published',
      position: 'news',
    },
    {
      id: '3',
      title: '春季装修注意事项',
      content: '春季是装修的黄金季节，但也要注意防潮、通风等问题',
      publishDate: '2024-03-05',
      status: 'draft',
      position: 'news',
    },
  ]);

  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingNews, setEditingNews] = useState<NewsItem | null>(null);
  const [form] = Form.useForm();

  // 打开新建/编辑 Modal
  const handleOpenModal = (record?: NewsItem) => {
    if (record) {
      setEditingNews(record);
      form.setFieldsValue({
        ...record,
        publishDate: record.publishDate ? dayjs(record.publishDate) : null,
      });
    } else {
      setEditingNews(null);
      form.resetFields();
    }
    setIsModalOpen(true);
  };

  // 保存资讯
  const handleSave = async () => {
    try {
      const values = await form.validateFields();
      const newsData: NewsItem = {
        id: editingNews?.id || Date.now().toString(),
        title: values.title,
        content: values.content,
        publishDate: values.publishDate ? values.publishDate.format('YYYY-MM-DD') : '',
        status: values.status || 'draft',
        position: values.position,
        coverImage: values.coverImage,
      };

      if (editingNews) {
        setNews(news.map(n => n.id === editingNews.id ? newsData : n));
        message.success('资讯已更新');
      } else {
        setNews([newsData, ...news]);
        message.success('资讯已创建');
      }

      setIsModalOpen(false);
      form.resetFields();
    } catch (error) {
      // 验证失败
    }
  };

  // 发布资讯
  const handlePublish = (record: NewsItem) => {
    Modal.confirm({
      title: '确认发布',
      content: `确定要发布资讯"${record.title}"吗？发布后将在${getPositionLabel(record.position)}展示。`,
      okText: '确定发布',
      cancelText: '取消',
      onOk: () => {
        setNews(news.map(n =>
          n.id === record.id
            ? { ...n, status: 'published', publishDate: dayjs().format('YYYY-MM-DD') }
            : n
        ));
        message.success('资讯已发布');
      },
    });
  };

  // 取消发布
  const handleUnpublish = (record: NewsItem) => {
    Modal.confirm({
      title: '确认取消发布',
      content: `确定要取消发布资讯"${record.title}"吗？取消后将不再展示。`,
      okText: '确定',
      cancelText: '取消',
      onOk: () => {
        setNews(news.map(n =>
          n.id === record.id
            ? { ...n, status: 'draft' }
            : n
        ));
        message.success('已取消发布');
      },
    });
  };

  // 删除资讯
  const handleDelete = (id: string) => {
    Modal.confirm({
      title: '确认删除',
      content: '确定要删除这条资讯吗？删除后无法恢复。',
      okText: '确定',
      cancelText: '取消',
      onOk: () => {
        setNews(news.filter(n => n.id !== id));
        message.success('资讯已删除');
      },
    });
  };

  // 获取发布位置标签
  const getPositionLabel = (position: string) => {
    const map: { [key: string]: string } = {
      banner: 'Banner区域',
      news: '资讯区域',
      other: '其他区域',
    };
    return map[position] || position;
  };

  const columns = [
    {
      title: '标题',
      dataIndex: 'title',
      key: 'title',
      width: '25%',
      render: (text: string, record: NewsItem) => (
        <Space>
          {record.coverImage && <PictureOutlined style={{ color: '#1677ff' }} />}
          <FileTextOutlined />
          <Text strong>{text}</Text>
        </Space>
      ),
    },
    {
      title: '内容',
      dataIndex: 'content',
      key: 'content',
      width: '30%',
      ellipsis: true,
    },
    {
      title: '发布位置',
      dataIndex: 'position',
      key: 'position',
      width: '12%',
      render: (position: string) => {
        const colorMap: { [key: string]: string } = {
          banner: 'red',
          news: 'blue',
          other: 'default',
        };
        return (
          <Tag color={colorMap[position]}>
            {getPositionLabel(position)}
          </Tag>
        );
      },
    },
    {
      title: '发布日期',
      dataIndex: 'publishDate',
      key: 'publishDate',
      width: '12%',
      render: (date: string) => (
        date ? (
          <Space>
            <CalendarOutlined />
            {date}
          </Space>
        ) : (
          <Text type="secondary">未发布</Text>
        )
      ),
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: '10%',
      render: (status: string) => (
        <Tag color={status === 'published' ? 'success' : 'default'}>
          {status === 'published' ? '已发布' : '草稿'}
        </Tag>
      ),
    },
    {
      title: '操作',
      key: 'action',
      width: '15%',
      render: (_: any, record: NewsItem) => (
        <Space size="small">
          {record.status === 'draft' ? (
            <Button
              type="link"
              icon={<SendOutlined />}
              size="small"
              onClick={() => handlePublish(record)}
            >
              发布
            </Button>
          ) : (
            <Button
              type="link"
              icon={<EyeOutlined />}
              size="small"
              onClick={() => handleUnpublish(record)}
            >
              取消发布
            </Button>
          )}
          <Button
            type="link"
            icon={<EditOutlined />}
            size="small"
            onClick={() => handleOpenModal(record)}
          >
            编辑
          </Button>
          <Button
            type="link"
            danger
            icon={<DeleteOutlined />}
            size="small"
            onClick={() => handleDelete(record.id)}
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
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <div>
            <Title level={2}>资讯设置</Title>
            <Text type="secondary">管理系统资讯和公告信息，支持多位置发布</Text>
          </div>
          <Button
            type="primary"
            icon={<PlusOutlined />}
            onClick={() => handleOpenModal()}
          >
            新建资讯
          </Button>
        </div>

        <Card>
          <Table
            columns={columns}
            dataSource={news}
            rowKey="id"
            pagination={{ pageSize: 10 }}
          />
        </Card>
      </Space>

      {/* 新建/编辑资讯 Modal */}
      <Modal
        title={editingNews ? '编辑资讯' : '新建资讯'}
        open={isModalOpen}
        onOk={handleSave}
        onCancel={() => {
          setIsModalOpen(false);
          form.resetFields();
        }}
        okText="保存"
        cancelText="取消"
        width={700}
      >
        <Form
          form={form}
          layout="vertical"
          style={{ marginTop: 16 }}
          initialValues={{
            status: 'draft',
            position: 'news',
          }}
        >
          <Form.Item
            name="title"
            label="资讯标题"
            rules={[{ required: true, message: '请输入资讯标题' }]}
          >
            <Input placeholder="请输入资讯标题" />
          </Form.Item>

          <Form.Item
            name="content"
            label="资讯内容"
            rules={[{ required: true, message: '请输入资讯内容' }]}
          >
            <TextArea
              rows={6}
              placeholder="请输入资讯内容"
              showCount
              maxLength={500}
            />
          </Form.Item>

          <Form.Item
            name="position"
            label="发布位置"
            rules={[{ required: true, message: '请选择发布位置' }]}
          >
            <Select placeholder="请选择发布位置">
              <Select.Option value="banner">
                <Space>
                  <Tag color="red">Banner</Tag>
                  顶部轮播图区域（首页最显眼位置）
                </Space>
              </Select.Option>
              <Select.Option value="news">
                <Space>
                  <Tag color="blue">资讯</Tag>
                  资讯列表区域（常规资讯展示）
                </Space>
              </Select.Option>
              <Select.Option value="other">
                <Space>
                  <Tag color="default">其他</Tag>
                  其他区域（浮动通知、弹窗等）
                </Space>
              </Select.Option>
            </Select>
          </Form.Item>

          <Form.Item
            name="coverImage"
            label="封面图片"
            tooltip="建议尺寸：750x400px，Banner位置建议上传封面图"
          >
            <Input
              placeholder="请输入图片URL（暂时支持URL，后续可添加上传功能）"
              prefix={<PictureOutlined />}
            />
          </Form.Item>

          <Form.Item
            name="publishDate"
            label="发布日期"
            tooltip="不填写则使用实际发布时的日期"
          >
            <DatePicker style={{ width: '100%' }} placeholder="选择发布日期" />
          </Form.Item>

          <Form.Item
            name="status"
            label="状态"
            rules={[{ required: true, message: '请选择状态' }]}
          >
            <Select placeholder="请选择状态">
              <Select.Option value="draft">
                <Tag color="default">草稿</Tag>
                保存为草稿，不会立即发布
              </Select.Option>
              <Select.Option value="published">
                <Tag color="success">已发布</Tag>
                立即发布到选定位置
              </Select.Option>
            </Select>
          </Form.Item>
        </Form>
      </Modal>
    </Layout>
  );
};

export default NewsSettings;
