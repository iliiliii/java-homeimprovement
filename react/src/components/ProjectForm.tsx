import { Modal, Form, Input, Select, DatePicker, message } from 'antd';
import { mockCustomers } from '@/data/mockData';
import dayjs from 'dayjs';

const { TextArea } = Input;
const { RangePicker } = DatePicker;

interface ProjectFormData {
  projectName: string;
  siteName: string;
  siteAddress: string;
  customerId: string;
  dateRange: [dayjs.Dayjs, dayjs.Dayjs];
  description?: string;
}

interface ProjectFormProps {
  open: boolean;
  onCancel: () => void;
  onSubmit: (data: any) => void;
  initialData?: any;
  title?: string;
}

const ProjectForm = ({
  open,
  onCancel,
  onSubmit,
  initialData,
  title = '新建项目'
}: ProjectFormProps) => {
  const [form] = Form.useForm();

  const handleOk = async () => {
    try {
      const values = await form.validateFields();
      const [startDate, endDate] = values.dateRange;

      onSubmit({
        projectName: values.projectName,
        siteName: values.siteName,
        siteAddress: values.siteAddress,
        customerId: values.customerId,
        startDate: startDate.format('YYYY-MM-DD'),
        endDate: endDate.format('YYYY-MM-DD'),
        description: values.description,
      });

      form.resetFields();
    } catch (error) {
      message.error('请检查表单填写是否完整');
    }
  };

  const handleCancel = () => {
    form.resetFields();
    onCancel();
  };

  // 设置初始值
  const getInitialValues = () => {
    if (!initialData) return undefined;

    return {
      projectName: initialData.projectName,
      siteName: initialData.siteName,
      siteAddress: initialData.siteAddress,
      customerId: initialData.customerId,
      dateRange: initialData.startDate && initialData.endDate
        ? [dayjs(initialData.startDate), dayjs(initialData.endDate)]
        : undefined,
      description: initialData.description,
    };
  };

  return (
    <Modal
      title={title}
      open={open}
      onOk={handleOk}
      onCancel={handleCancel}
      width={600}
      okText={title === '新建项目' ? '创建项目' : '保存修改'}
      cancelText="取消"
    >
      <Form
        form={form}
        layout="vertical"
        initialValues={getInitialValues()}
      >
        <Form.Item
          label="项目名称"
          name="projectName"
          rules={[{ required: true, message: '请输入项目名称' }]}
        >
          <Input placeholder="例如：张先生家装修工程" />
        </Form.Item>

        <Form.Item
          label="工地名称"
          name="siteName"
          rules={[{ required: true, message: '请输入工地名称' }]}
        >
          <Input placeholder="例如：金地花园" />
        </Form.Item>

        <Form.Item
          label="工地地址"
          name="siteAddress"
          rules={[{ required: true, message: '请输入工地地址' }]}
        >
          <Input placeholder="请输入详细地址" />
        </Form.Item>

        <Form.Item
          label="关联客户"
          name="customerId"
          rules={[{ required: true, message: '请选择客户' }]}
        >
          <Select placeholder="请选择客户">
            {mockCustomers.map((customer) => (
              <Select.Option key={customer.id} value={customer.id}>
                {customer.name} - {customer.phone}
              </Select.Option>
            ))}
          </Select>
        </Form.Item>

        <Form.Item
          label="施工周期"
          name="dateRange"
          rules={[{ required: true, message: '请选择项目开始和结束日期' }]}
        >
          <RangePicker
            style={{ width: '100%' }}
            placeholder={['开工日期', '预计完工日期']}
          />
        </Form.Item>

        <Form.Item
          label="项目描述"
          name="description"
        >
          <TextArea
            rows={4}
            placeholder="请输入项目描述、特殊要求等（可选）"
          />
        </Form.Item>
      </Form>
    </Modal>
  );
};

export default ProjectForm;
