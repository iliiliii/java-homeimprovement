import { Modal, Form, Input, Upload, DatePicker, message, Space } from 'antd';
import { PlusOutlined } from '@ant-design/icons';
import { useState } from 'react';
import type { UploadFile } from 'antd/es/upload/interface';
import dayjs from 'dayjs';

const { TextArea } = Input;

interface QualityIssueReportProps {
  open: boolean;
  onCancel: () => void;
  onSubmit: (data: any) => void;
  projectName: string;
}

const QualityIssueReport = ({ open, onCancel, onSubmit, projectName }: QualityIssueReportProps) => {
  const [form] = Form.useForm();
  const [fileList, setFileList] = useState<UploadFile[]>([]);

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();

      const data = {
        title: values.title,
        description: values.description,
        images: fileList.map(file => file.url || file.thumbUrl || ''),
        reportTime: values.reportTime.format('YYYY-MM-DD HH:mm'),
        reporter: values.reporter,
        status: 'pending',
      };

      onSubmit(data);
      form.resetFields();
      setFileList([]);
      message.success('质量问题已上报');
    } catch (error) {
      // 验证失败
    }
  };

  const handleCancel = () => {
    form.resetFields();
    setFileList([]);
    onCancel();
  };

  const handleUploadChange = ({ fileList: newFileList }: any) => {
    if (newFileList.length > 20) {
      message.warning('最多只能上传20张图片');
      // 只保留前20张
      setFileList(newFileList.slice(0, 20));
      return;
    }
    setFileList(newFileList);
  };

  return (
    <Modal
      title={
        <Space>
          <span>{projectName} - 质量问题上报</span>
        </Space>
      }
      open={open}
      onCancel={handleCancel}
      onOk={handleSubmit}
      width={800}
      okText="提交问题"
      cancelText="取消"
    >
      <Form
        form={form}
        layout="vertical"
        style={{ marginTop: 24 }}
        initialValues={{
          reportTime: dayjs(),
        }}
      >
        <Form.Item
          name="title"
          label="问题标题"
          rules={[{ required: true, message: '请输入问题标题' }]}
        >
          <Input placeholder="例如：墙面平整度问题" size="large" />
        </Form.Item>

        <Form.Item
          name="description"
          label="问题描述"
          rules={[
            { required: true, message: '请输入问题描述' },
            { max: 100, message: '描述不能超过100字' },
          ]}
        >
          <TextArea
            rows={4}
            placeholder="请详细描述发现的质量问题（不超过100字）"
            showCount
            maxLength={100}
          />
        </Form.Item>

        <Form.Item
          name="images"
          label={
            <Space>
              <span>问题照片</span>
              <span style={{ fontSize: 12, color: '#999' }}>（最多20张）</span>
            </Space>
          }
        >
          <Upload
            listType="picture-card"
            fileList={fileList}
            onChange={handleUploadChange}
            beforeUpload={() => false}
            multiple
            maxCount={20}
          >
            {fileList.length >= 20 ? null : (
              <div>
                <PlusOutlined />
                <div style={{ marginTop: 8 }}>上传照片</div>
              </div>
            )}
          </Upload>
        </Form.Item>

        <Space style={{ width: '100%' }} size="large">
          <Form.Item
            name="reportTime"
            label="上报时间"
            rules={[{ required: true, message: '请选择上报时间' }]}
            style={{ marginBottom: 0, flex: 1 }}
          >
            <DatePicker
              showTime
              format="YYYY-MM-DD HH:mm"
              placeholder="选择上报时间"
              style={{ width: '100%' }}
            />
          </Form.Item>

          <Form.Item
            name="reporter"
            label="上报人"
            rules={[{ required: true, message: '请输入上报人' }]}
            style={{ marginBottom: 0, flex: 1 }}
          >
            <Input placeholder="请输入上报人姓名" />
          </Form.Item>
        </Space>
      </Form>
    </Modal>
  );
};

export default QualityIssueReport;
