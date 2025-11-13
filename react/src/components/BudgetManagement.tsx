import { Modal, Form, Input, InputNumber, Select, Button, Table, Space, message, Popconfirm } from 'antd';
import { PlusOutlined, DeleteOutlined, DollarOutlined, EditOutlined } from '@ant-design/icons';
import { useState, useEffect } from 'react';

interface BudgetItem {
  id: string;
  category: string;
  amount: number;
  description?: string;
}

interface BudgetManagementProps {
  open: boolean;
  onCancel: () => void;
  onSave: (budgets: BudgetItem[]) => void;
  initialData?: BudgetItem[];
  projectName: string;
}

const BudgetManagement = ({
  open,
  onCancel,
  onSave,
  initialData = [],
  projectName,
}: BudgetManagementProps) => {
  const [budgets, setBudgets] = useState<BudgetItem[]>(initialData);
  const [form] = Form.useForm();
  const [isAddingNew, setIsAddingNew] = useState(false);
  const [editingBudget, setEditingBudget] = useState<BudgetItem | null>(null);

  // 当 dialog 打开时，重置状态并同步 initialData（只在打开时同步一次）
  useEffect(() => {
    if (open) {
      setBudgets([...initialData]); // 创建副本，避免引用问题
      setIsAddingNew(false);
      setEditingBudget(null);
      form.resetFields();
    }
  }, [open]); // 移除 initialData 依赖，只在 open 变化时执行

  const handleAddBudget = async () => {
    try {
      const values = await form.validateFields();
      const newBudget: BudgetItem = {
        id: Date.now().toString(),
        category: values.category,
        amount: values.amount,
        description: values.description,
      };

      setBudgets([...budgets, newBudget]);
      form.resetFields();
      setIsAddingNew(false);
      message.success('预算项已添加');
    } catch (error) {
      // 验证失败
    }
  };

  const handleDelete = (id: string) => {
    const newBudgets = budgets.filter(b => b.id !== id);
    setBudgets(newBudgets);

    // 如果删除的是正在编辑的项，清除编辑状态
    if (editingBudget?.id === id) {
      setEditingBudget(null);
      form.resetFields();
    }

    message.success('预算项已删除');
  };

  const handleEdit = (budget: BudgetItem) => {
    setIsAddingNew(false);
    setEditingBudget(budget);
    form.setFieldsValue({
      category: budget.category,
      amount: budget.amount,
      description: budget.description,
    });
  };

  const handleUpdate = async () => {
    if (!editingBudget) return;

    try {
      const values = await form.validateFields();
      const updatedBudget: BudgetItem = {
        ...editingBudget,
        category: values.category,
        amount: values.amount,
        description: values.description,
      };

      setBudgets(budgets.map(b => b.id === editingBudget.id ? updatedBudget : b));
      form.resetFields();
      setEditingBudget(null);
      message.success('预算项已更新');
    } catch (error) {
      // 验证失败
    }
  };

  const handleCancelEdit = () => {
    form.resetFields();
    setEditingBudget(null);
    setIsAddingNew(false);
  };

  const handleSave = () => {
    if (budgets.length === 0) {
      message.warning('请至少添加一项预算');
      return;
    }
    onSave(budgets);
    message.success('预算已保存');
  };

  const handleModalCancel = () => {
    // 清理编辑状态
    setIsAddingNew(false);
    setEditingBudget(null);
    form.resetFields();
    onCancel();
  };

  const totalBudget = budgets.reduce((sum, b) => sum + b.amount, 0);

  const columns = [
    {
      title: '预算类别',
      dataIndex: 'category',
      key: 'category',
      width: '25%',
    },
    {
      title: '预算金额',
      dataIndex: 'amount',
      key: 'amount',
      width: '25%',
      render: (amount: number) => (
        <span style={{ color: '#faad14', fontWeight: 'bold' }}>
          ¥{amount.toLocaleString()}
        </span>
      ),
    },
    {
      title: '说明',
      dataIndex: 'description',
      key: 'description',
      width: '35%',
      render: (text: string) => text || <span style={{ color: '#999' }}>-</span>,
    },
    {
      title: '操作',
      key: 'action',
      width: '20%',
      render: (_: any, record: BudgetItem) => (
        <Space size="small">
          <Button
            type="link"
            size="small"
            icon={<EditOutlined />}
            onClick={() => handleEdit(record)}
            disabled={editingBudget?.id === record.id}
          >
            编辑
          </Button>
          <Popconfirm
            title="确认删除"
            description="确定要删除这项预算吗？"
            onConfirm={() => handleDelete(record.id)}
            okText="确定"
            cancelText="取消"
          >
            <Button
              type="link"
              danger
              size="small"
              icon={<DeleteOutlined />}
            >
              删除
            </Button>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <Modal
      title={
        <Space>
          <DollarOutlined style={{ color: '#faad14' }} />
          <span>{projectName} - 预算管理</span>
        </Space>
      }
      open={open}
      onCancel={handleModalCancel}
      onOk={handleSave}
      width={900}
      okText="保存预算"
      cancelText="取消"
    >
      <Space direction="vertical" size="large" style={{ width: '100%' }}>
        {/* 预算总额显示 */}
        <div
          style={{
            background: '#fff7e6',
            border: '1px solid #ffd591',
            borderRadius: '8px',
            padding: '16px',
            textAlign: 'center',
          }}
        >
          <div style={{ fontSize: 14, color: '#999', marginBottom: 8 }}>预算总额</div>
          <div style={{ fontSize: 32, color: '#faad14', fontWeight: 'bold' }}>
            ¥{totalBudget.toLocaleString()}
          </div>
          <div style={{ fontSize: 12, color: '#999', marginTop: 8 }}>
            {budgets.length} 个预算项
          </div>
        </div>

        {/* 预算列表 */}
        <Table
          columns={columns}
          dataSource={budgets}
          rowKey="id"
          pagination={false}
          locale={{ emptyText: '暂无预算项，请点击下方按钮添加' }}
          size="small"
        />

        {/* 添加/编辑预算表单 */}
        {(isAddingNew || editingBudget) ? (
          <div
            style={{
              background: editingBudget ? '#e6f7ff' : '#f5f5f5',
              padding: '20px',
              borderRadius: '8px',
              border: editingBudget ? '2px solid #1677ff' : '1px solid #d9d9d9',
            }}
          >
            <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <span style={{ fontWeight: 'bold', fontSize: 15, color: editingBudget ? '#1677ff' : '#000' }}>
                {editingBudget ? '📝 编辑预算项' : '➕ 添加预算项'}
              </span>
              {editingBudget && (
                <span style={{ fontSize: 12, color: '#666' }}>
                  正在编辑：{editingBudget.category}
                </span>
              )}
            </div>
            <Form form={form} layout="vertical">
              <Space direction="vertical" size="middle" style={{ width: '100%' }}>
                <Space size="middle" style={{ width: '100%' }}>
                  <Form.Item
                    name="category"
                    label="预算类别"
                    rules={[{ required: true, message: '请选择类别' }]}
                    style={{ flex: 1, marginBottom: 0 }}
                  >
                    <Select placeholder="选择预算类别" size="large">
                      <Select.Option value="拆除工程">拆除工程</Select.Option>
                      <Select.Option value="水电安装">水电安装</Select.Option>
                      <Select.Option value="泥瓦工程">泥瓦工程</Select.Option>
                      <Select.Option value="木工工程">木工工程</Select.Option>
                      <Select.Option value="油漆工程">油漆工程</Select.Option>
                      <Select.Option value="材料费">材料费</Select.Option>
                      <Select.Option value="人工费">人工费</Select.Option>
                      <Select.Option value="管理费">管理费</Select.Option>
                      <Select.Option value="其他">其他</Select.Option>
                    </Select>
                  </Form.Item>

                  <Form.Item
                    name="amount"
                    label="预算金额"
                    rules={[
                      { required: true, message: '请输入金额' },
                      { type: 'number', min: 1, message: '金额必须大于0' },
                    ]}
                    style={{ flex: 1, marginBottom: 0 }}
                  >
                    <InputNumber
                      placeholder="输入预算金额"
                      style={{ width: '100%' }}
                      size="large"
                      min={0}
                      formatter={value => `¥ ${value}`.replace(/\B(?=(\d{3})+(?!\d))/g, ',')}
                      parser={value => value!.replace(/¥\s?|(,*)/g, '') as any}
                    />
                  </Form.Item>
                </Space>

                <Form.Item
                  name="description"
                  label="说明（可选）"
                  style={{ marginBottom: 0 }}
                >
                  <Input placeholder="输入预算说明" size="large" />
                </Form.Item>

                <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 8, marginTop: 8 }}>
                  <Button onClick={handleCancelEdit} size="large">
                    取消
                  </Button>
                  <Button
                    type="primary"
                    onClick={editingBudget ? handleUpdate : handleAddBudget}
                    size="large"
                    style={{ minWidth: 100 }}
                  >
                    {editingBudget ? '确认修改' : '确认添加'}
                  </Button>
                </div>
              </Space>
            </Form>
          </div>
        ) : (
          <Button
            type="dashed"
            block
            icon={<PlusOutlined />}
            onClick={() => {
              form.resetFields();
              setEditingBudget(null);
              setIsAddingNew(true);
            }}
          >
            添加预算项
          </Button>
        )}
      </Space>
    </Modal>
  );
};

export default BudgetManagement;
