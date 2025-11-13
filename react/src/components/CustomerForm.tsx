import { Modal, Form, Input, message } from 'antd';
import { UserOutlined, PhoneOutlined, MailOutlined, EnvironmentOutlined } from '@ant-design/icons';
import { useEffect } from 'react';

interface CustomerFormProps {
  open: boolean;
  onCancel: () => void;
  onSubmit: (values: any) => void;
  initialData?: {
    name: string;
    phone: string;
    email: string;
    address: string;
  };
  title?: string;
}

const CustomerForm = ({ open, onCancel, onSubmit, initialData, title = '新建客户' }: CustomerFormProps) => {
  const [form] = Form.useForm();

  useEffect(() => {
    if (open) {
      if (initialData) {
        form.setFieldsValue(initialData);
      } else {
        form.resetFields();
      }
    }
  }, [open, initialData, form]);

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      onSubmit(values);
      form.resetFields();
    } catch (error) {
      // 验证失败
    }
  };

  const handleCancel = () => {
    form.resetFields();
    onCancel();
  };

  return (
    <Modal
      title={title}
      open={open}
      onCancel={handleCancel}
      onOk={handleSubmit}
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
          name="name"
          label="客户姓名"
          rules={[{ required: true, message: '请输入客户姓名' }]}
        >
          <Input
            prefix={<UserOutlined />}
            placeholder="请输入客户姓名"
            size="large"
          />
        </Form.Item>

        <Form.Item
          name="phone"
          label="联系电话"
          rules={[
            { required: true, message: '请输入联系电话' },
            { pattern: /^1[3-9]\d{9}$/, message: '请输入有效的手机号码' },
          ]}
        >
          <Input
            prefix={<PhoneOutlined />}
            placeholder="请输入联系电话"
            size="large"
          />
        </Form.Item>

        <Form.Item
          name="email"
          label="电子邮箱"
          rules={[
            { required: true, message: '请输入电子邮箱' },
            { type: 'email', message: '请输入有效的邮箱地址' },
          ]}
        >
          <Input
            prefix={<MailOutlined />}
            placeholder="请输入电子邮箱"
            size="large"
          />
        </Form.Item>

        <Form.Item
          name="address"
          label="联系地址"
          rules={[{ required: true, message: '请输入联系地址' }]}
        >
          <Input.TextArea
            prefix={<EnvironmentOutlined />}
            placeholder="请输入联系地址"
            size="large"
            rows={3}
          />
        </Form.Item>
      </Form>
    </Modal>
  );
};

export default CustomerForm;
