import { Modal, Form, Input, Upload, Radio, DatePicker, message, Space } from 'antd';
import { PlusOutlined, InboxOutlined } from '@ant-design/icons';
import { useState } from 'react';
import type { UploadFile } from 'antd/es/upload/interface';
import dayjs from 'dayjs';

const { TextArea } = Input;

interface InspectionReportProps {
  open: boolean;
  onCancel: () => void;
  onSubmit: (data: any) => void;
  nodeName: string;
}

const InspectionReport = ({ open, onCancel, onSubmit, nodeName }: InspectionReportProps) => {
  const [form] = Form.useForm();
  const [fileList, setFileList] = useState<UploadFile[]>([]);

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();

      const data = {
        title: values.title,
        content: values.content,
        images: fileList.map(file => file.url || file.thumbUrl || ''),
        isPassed: values.isPassed,
        inspectionTime: values.inspectionTime.format('YYYY-MM-DD HH:mm'),
        inspector: values.inspector,
      };

      onSubmit(data);
      form.resetFields();
      setFileList([]);
      message.success('验收记录已提交');
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
    // 限制最多20张图片
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
          <span>{nodeName} - 验收上报</span>
        </Space>
      }
      open={open}
      onCancel={handleCancel}
      onOk={handleSubmit}
      width={800}
      okText="提交验收"
      cancelText="取消"
    >
      <Form
        form={form}
        layout="vertical"
        style={{ marginTop: 24 }}
        initialValues={{
          inspectionTime: dayjs(),
          isPassed: true,
        }}
      >
        <Form.Item
          name="title"
          label="验收标题"
          rules={[{ required: true, message: '请输入验收标题' }]}
        >
          <Input placeholder="例如：水电管路验收" size="large" />
        </Form.Item>

        <Form.Item
          name="content"
          label="验收内容"
          rules={[
            { required: true, message: '请输入验收内容' },
            { max: 100, message: '内容不能超过100字' },
          ]}
        >
          <TextArea
            rows={4}
            placeholder="请描述验收情况（不超过100字）"
            showCount
            maxLength={100}
          />
        </Form.Item>

        <Form.Item
          name="images"
          label={
            <Space>
              <span>现场照片</span>
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

        <Form.Item
          name="isPassed"
          label="验收结果"
          rules={[{ required: true, message: '请选择验收结果' }]}
        >
          <Radio.Group>
            <Radio value={true}>合格</Radio>
            <Radio value={false}>不合格</Radio>
          </Radio.Group>
        </Form.Item>

        <Space style={{ width: '100%' }} size="large">
          <Form.Item
            name="inspectionTime"
            label="验收时间"
            rules={[{ required: true, message: '请选择验收时间' }]}
            style={{ marginBottom: 0, flex: 1 }}
          >
            <DatePicker
              showTime
              format="YYYY-MM-DD HH:mm"
              placeholder="选择验收时间"
              style={{ width: '100%' }}
            />
          </Form.Item>

          <Form.Item
            name="inspector"
            label="验收人"
            rules={[{ required: true, message: '请输入验收人' }]}
            style={{ marginBottom: 0, flex: 1 }}
          >
            <Input placeholder="请输入验收人姓名" />
          </Form.Item>
        </Space>
      </Form>
    </Modal>
  );
};

export default InspectionReport;
