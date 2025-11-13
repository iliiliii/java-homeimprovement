import { Modal, Form, Input, Upload, DatePicker, message, Space, Alert } from 'antd';
import { PlusOutlined } from '@ant-design/icons';
import { useState } from 'react';
import type { UploadFile } from 'antd/es/upload/interface';
import dayjs from 'dayjs';
import type { QualityIssue } from '@/data/mockData';

const { TextArea } = Input;

interface IssueResolutionProps {
  open: boolean;
  onCancel: () => void;
  onSubmit: (data: any) => void;
  issue: QualityIssue | null;
}

const IssueResolution = ({ open, onCancel, onSubmit, issue }: IssueResolutionProps) => {
  const [form] = Form.useForm();
  const [fileList, setFileList] = useState<UploadFile[]>([]);

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();

      const data = {
        description: values.description,
        images: fileList.map(file => file.url || file.thumbUrl || ''),
        resolveTime: values.resolveTime.format('YYYY-MM-DD HH:mm'),
        resolver: values.resolver,
      };

      onSubmit(data);
      form.resetFields();
      setFileList([]);
      message.success('整改处理已提交');
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
          <span>整改处理</span>
        </Space>
      }
      open={open}
      onCancel={handleCancel}
      onOk={handleSubmit}
      width={800}
      okText="提交整改"
      cancelText="取消"
    >
      {issue && (
        <>
          <Alert
            message={issue.title}
            description={issue.description}
            type="warning"
            showIcon
            style={{ marginBottom: 24 }}
          />

          <Form
            form={form}
            layout="vertical"
            initialValues={{
              resolveTime: dayjs(),
            }}
          >
            <Form.Item
              name="description"
              label="整改说明"
              rules={[
                { required: true, message: '请输入整改说明' },
                { max: 100, message: '说明不能超过100字' },
              ]}
            >
              <TextArea
                rows={4}
                placeholder="请描述整改措施和结果（不超过100字）"
                showCount
                maxLength={100}
              />
            </Form.Item>

            <Form.Item
              name="images"
              label={
                <Space>
                  <span>整改照片</span>
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
                name="resolveTime"
                label="整改时间"
                rules={[{ required: true, message: '请选择整改时间' }]}
                style={{ marginBottom: 0, flex: 1 }}
              >
                <DatePicker
                  showTime
                  format="YYYY-MM-DD HH:mm"
                  placeholder="选择整改时间"
                  style={{ width: '100%' }}
                />
              </Form.Item>

              <Form.Item
                name="resolver"
                label="整改人"
                rules={[{ required: true, message: '请输入整改人' }]}
                style={{ marginBottom: 0, flex: 1 }}
              >
                <Input placeholder="请输入整改人姓名" />
              </Form.Item>
            </Space>
          </Form>
        </>
      )}
    </Modal>
  );
};

export default IssueResolution;
