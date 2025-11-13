import { Card, Button, Space, List, Typography, Input, message, Row, Col, Modal, Form, Collapse, Select } from 'antd';
import { PlusOutlined, DeleteOutlined, TagsOutlined, AppstoreOutlined, FolderOutlined, EditOutlined } from '@ant-design/icons';
import Layout from '@/components/Layout';
import { useState } from 'react';

const { Title, Text } = Typography;
const { Panel } = Collapse;

// 标准施工阶段（大分类）
const standardMainCategories = [
  { id: '1', name: '基础工程' },
  { id: '2', name: '安装工程' },
  { id: '3', name: '泥瓦工程' },
  { id: '4', name: '木工工程' },
  { id: '5', name: '油漆工程' },
  { id: '6', name: '收尾工程' },
];

interface SubCategory {
  id: string;
  name: string;
}

interface Project {
  id: string;
  name: string;
  subCategories: SubCategory[];
}

interface MainCategory {
  id: string;
  name: string;
  projects: Project[];
}

const CategorySettings = () => {
  const [categories, setCategories] = useState<MainCategory[]>([
    {
      id: '1',
      name: '基础工程',
      projects: [
        {
          id: '1-1',
          name: '拆除工程',
          subCategories: [
            { id: '1-1-1', name: '墙体拆除' },
            { id: '1-1-2', name: '地面拆除' },
            { id: '1-1-3', name: '吊顶拆除' },
          ],
        },
        {
          id: '1-2',
          name: '防水工程',
          subCategories: [
            { id: '1-2-1', name: '卫生间防水' },
            { id: '1-2-2', name: '厨房防水' },
            { id: '1-2-3', name: '阳台防水' },
          ],
        },
      ],
    },
    {
      id: '2',
      name: '安装工程',
      projects: [
        {
          id: '2-1',
          name: '水电安装',
          subCategories: [
            { id: '2-1-1', name: '强电改造' },
            { id: '2-1-2', name: '弱电改造' },
            { id: '2-1-3', name: '给水改造' },
            { id: '2-1-4', name: '排水改造' },
          ],
        },
      ],
    },
    {
      id: '3',
      name: '泥瓦工程',
      projects: [],
    },
    {
      id: '4',
      name: '木工工程',
      projects: [],
    },
    {
      id: '5',
      name: '油漆工程',
      projects: [],
    },
    {
      id: '6',
      name: '收尾工程',
      projects: [],
    },
  ]);

  const [selectedCategory, setSelectedCategory] = useState<MainCategory | null>(categories[0]);
  const [selectedProject, setSelectedProject] = useState<Project | null>(null);

  const [isMainCategoryModalOpen, setIsMainCategoryModalOpen] = useState(false);
  const [isProjectModalOpen, setIsProjectModalOpen] = useState(false);
  const [isSubCategoryModalOpen, setIsSubCategoryModalOpen] = useState(false);

  const [mainCategoryForm] = Form.useForm();
  const [projectForm] = Form.useForm();
  const [subCategoryForm] = Form.useForm();

  // 添加大分类
  const handleAddMainCategory = async () => {
    try {
      const values = await mainCategoryForm.validateFields();

      // 检查是否已存在
      const exists = categories.some(cat => cat.name === values.name);
      if (exists) {
        message.warning(`大分类"${values.name}"已存在`);
        return;
      }

      const newCategory: MainCategory = {
        id: Date.now().toString(),
        name: values.name,
        projects: [],
      };
      setCategories([...categories, newCategory]);
      message.success(`大分类"${values.name}"已添加`);
      setIsMainCategoryModalOpen(false);
      mainCategoryForm.resetFields();
    } catch (error) {
      // 验证失败
    }
  };

  // 获取可添加的大分类（排除已存在的）
  const getAvailableMainCategories = () => {
    const existingNames = categories.map(cat => cat.name);
    return standardMainCategories.filter(cat => !existingNames.includes(cat.name));
  };

  // 删除大分类
  const handleDeleteMainCategory = (category: MainCategory) => {
    Modal.confirm({
      title: '确认删除',
      content: `确定要删除大分类"${category.name}"及其下所有项目和小分类吗？`,
      okText: '确定',
      cancelText: '取消',
      onOk: () => {
        setCategories(categories.filter(c => c.id !== category.id));
        if (selectedCategory?.id === category.id) {
          setSelectedCategory(categories[0] || null);
          setSelectedProject(null);
        }
        message.success(`大分类"${category.name}"已删除`);
      },
    });
  };

  // 添加项目
  const handleAddProject = async () => {
    if (!selectedCategory) {
      message.warning('请先选择一个大分类');
      return;
    }
    try {
      const values = await projectForm.validateFields();
      const newProject: Project = {
        id: Date.now().toString(),
        name: values.name,
        subCategories: [],
      };
      const updatedCategories = categories.map(cat => {
        if (cat.id === selectedCategory.id) {
          return {
            ...cat,
            projects: [...cat.projects, newProject],
          };
        }
        return cat;
      });
      setCategories(updatedCategories);
      setSelectedCategory(updatedCategories.find(c => c.id === selectedCategory.id) || null);
      message.success(`项目"${values.name}"已添加`);
      setIsProjectModalOpen(false);
      projectForm.resetFields();
    } catch (error) {
      // 验证失败
    }
  };

  // 删除项目
  const handleDeleteProject = (projectId: string) => {
    if (!selectedCategory) return;
    const project = selectedCategory.projects.find(p => p.id === projectId);
    Modal.confirm({
      title: '确认删除',
      content: `确定要删除项目"${project?.name}"及其下所有小分类吗？`,
      okText: '确定',
      cancelText: '取消',
      onOk: () => {
        const updatedCategories = categories.map(cat => {
          if (cat.id === selectedCategory.id) {
            return {
              ...cat,
              projects: cat.projects.filter(p => p.id !== projectId),
            };
          }
          return cat;
        });
        setCategories(updatedCategories);
        setSelectedCategory(updatedCategories.find(c => c.id === selectedCategory.id) || null);
        if (selectedProject?.id === projectId) {
          setSelectedProject(null);
        }
        message.success('项目已删除');
      },
    });
  };

  // 添加小分类
  const handleAddSubCategory = async () => {
    if (!selectedCategory || !selectedProject) {
      message.warning('请先选择一个项目');
      return;
    }
    try {
      const values = await subCategoryForm.validateFields();
      const newSubCategory: SubCategory = {
        id: Date.now().toString(),
        name: values.name,
      };
      const updatedCategories = categories.map(cat => {
        if (cat.id === selectedCategory.id) {
          return {
            ...cat,
            projects: cat.projects.map(proj => {
              if (proj.id === selectedProject.id) {
                return {
                  ...proj,
                  subCategories: [...proj.subCategories, newSubCategory],
                };
              }
              return proj;
            }),
          };
        }
        return cat;
      });
      setCategories(updatedCategories);
      const updatedCat = updatedCategories.find(c => c.id === selectedCategory.id);
      setSelectedCategory(updatedCat || null);
      setSelectedProject(updatedCat?.projects.find(p => p.id === selectedProject.id) || null);
      message.success(`小分类"${values.name}"已添加`);
      setIsSubCategoryModalOpen(false);
      subCategoryForm.resetFields();
    } catch (error) {
      // 验证失败
    }
  };

  // 删除小分类
  const handleDeleteSubCategory = (projectId: string, subCategoryId: string) => {
    if (!selectedCategory) return;
    const updatedCategories = categories.map(cat => {
      if (cat.id === selectedCategory.id) {
        return {
          ...cat,
          projects: cat.projects.map(proj => {
            if (proj.id === projectId) {
              return {
                ...proj,
                subCategories: proj.subCategories.filter(sub => sub.id !== subCategoryId),
              };
            }
            return proj;
          }),
        };
      }
      return cat;
    });
    setCategories(updatedCategories);
    const updatedCat = updatedCategories.find(c => c.id === selectedCategory.id);
    setSelectedCategory(updatedCat || null);
    setSelectedProject(updatedCat?.projects.find(p => p.id === projectId) || null);
    message.success('小分类已删除');
  };

  return (
    <Layout>
      <Space direction="vertical" size="large" style={{ width: '100%' }}>
        <div>
          <Title level={2}>分类设置</Title>
          <Text type="secondary">管理项目费用分类：大分类 → 项目 → 小分类</Text>
        </div>

        <Row gutter={16}>
          {/* 左侧：大分类 */}
          <Col xs={24} lg={8}>
            <Card
              title={<Space><AppstoreOutlined /> 大分类</Space>}
              extra={
                <Button
                  type="primary"
                  size="small"
                  icon={<PlusOutlined />}
                  onClick={() => setIsMainCategoryModalOpen(true)}
                >
                  添加
                </Button>
              }
            >
              <List
                dataSource={categories}
                locale={{ emptyText: '暂无大分类，请添加' }}
                renderItem={(item) => (
                  <List.Item
                    style={{
                      cursor: 'pointer',
                      backgroundColor: selectedCategory?.id === item.id ? '#e6f4ff' : 'transparent',
                      padding: '12px 16px',
                    }}
                    onClick={() => {
                      setSelectedCategory(item);
                      setSelectedProject(null);
                    }}
                    actions={[
                      <Button
                        type="link"
                        danger
                        icon={<DeleteOutlined />}
                        size="small"
                        onClick={(e) => {
                          e.stopPropagation();
                          handleDeleteMainCategory(item);
                        }}
                      />,
                    ]}
                  >
                    <Space>
                      <AppstoreOutlined style={{ color: '#1677ff' }} />
                      <Text strong>{item.name}</Text>
                      <Text type="secondary">({item.projects.length})</Text>
                    </Space>
                  </List.Item>
                )}
              />
            </Card>
          </Col>

          {/* 右侧：项目和小分类 */}
          <Col xs={24} lg={16}>
            <Card
              title={
                <Space>
                  <FolderOutlined />
                  项目与小分类
                  {selectedCategory && <Text type="secondary">- {selectedCategory.name}</Text>}
                </Space>
              }
              extra={
                selectedCategory && (
                  <Button
                    type="primary"
                    size="small"
                    icon={<PlusOutlined />}
                    onClick={() => setIsProjectModalOpen(true)}
                  >
                    添加项目
                  </Button>
                )
              }
            >
              {selectedCategory ? (
                selectedCategory.projects.length > 0 ? (
                  <Collapse accordion>
                    {selectedCategory.projects.map((project) => (
                      <Panel
                        key={project.id}
                        header={
                          <Space>
                            <FolderOutlined style={{ color: '#52c41a' }} />
                            <Text strong>{project.name}</Text>
                            <Text type="secondary">({project.subCategories.length} 个小分类)</Text>
                          </Space>
                        }
                        extra={
                          <Space>
                            <Button
                              type="link"
                              size="small"
                              icon={<PlusOutlined />}
                              onClick={(e) => {
                                e.stopPropagation();
                                setSelectedProject(project);
                                setIsSubCategoryModalOpen(true);
                              }}
                            >
                              添加小分类
                            </Button>
                            <Button
                              type="link"
                              danger
                              size="small"
                              icon={<DeleteOutlined />}
                              onClick={(e) => {
                                e.stopPropagation();
                                handleDeleteProject(project.id);
                              }}
                            />
                          </Space>
                        }
                      >
                        {project.subCategories.length > 0 ? (
                          <List
                            size="small"
                            dataSource={project.subCategories}
                            renderItem={(subItem) => (
                              <List.Item
                                actions={[
                                  <Button
                                    type="link"
                                    danger
                                    size="small"
                                    icon={<DeleteOutlined />}
                                    onClick={() => handleDeleteSubCategory(project.id, subItem.id)}
                                  >
                                    删除
                                  </Button>,
                                ]}
                              >
                                <Space>
                                  <TagsOutlined style={{ color: '#faad14' }} />
                                  <Text>{subItem.name}</Text>
                                </Space>
                              </List.Item>
                            )}
                          />
                        ) : (
                          <div style={{ textAlign: 'center', padding: '20px 0', color: '#999' }}>
                            暂无小分类，点击上方"添加小分类"按钮添加
                          </div>
                        )}
                      </Panel>
                    ))}
                  </Collapse>
                ) : (
                  <div style={{ textAlign: 'center', padding: '60px 0', color: '#999' }}>
                    <FolderOutlined style={{ fontSize: 48, marginBottom: 16 }} />
                    <div>暂无项目，点击右上角"添加项目"按钮添加</div>
                  </div>
                )
              ) : (
                <div style={{ textAlign: 'center', padding: '60px 0', color: '#999' }}>
                  <AppstoreOutlined style={{ fontSize: 48, marginBottom: 16 }} />
                  <div>请先在左侧选择一个大分类</div>
                </div>
              )}
            </Card>
          </Col>
        </Row>
      </Space>

      {/* 添加大分类 Modal */}
      <Modal
        title="添加大分类"
        open={isMainCategoryModalOpen}
        onOk={handleAddMainCategory}
        onCancel={() => {
          setIsMainCategoryModalOpen(false);
          mainCategoryForm.resetFields();
        }}
        okText="确定"
        cancelText="取消"
      >
        <Form form={mainCategoryForm} layout="vertical" style={{ marginTop: 16 }}>
          <Form.Item
            name="name"
            label="选择施工阶段"
            rules={[{ required: true, message: '请选择大分类' }]}
          >
            <Select
              placeholder="选择要添加的施工阶段"
              size="large"
              disabled={getAvailableMainCategories().length === 0}
            >
              {getAvailableMainCategories().map(cat => (
                <Select.Option key={cat.id} value={cat.name}>
                  {cat.name}
                </Select.Option>
              ))}
            </Select>
          </Form.Item>
          {getAvailableMainCategories().length === 0 && (
            <div style={{ color: '#999', fontSize: 12, marginTop: -8 }}>
              所有标准施工阶段都已添加
            </div>
          )}
        </Form>
      </Modal>

      {/* 添加项目 Modal */}
      <Modal
        title={`添加项目 - ${selectedCategory?.name}`}
        open={isProjectModalOpen}
        onOk={handleAddProject}
        onCancel={() => {
          setIsProjectModalOpen(false);
          projectForm.resetFields();
        }}
        okText="确定"
        cancelText="取消"
      >
        <Form form={projectForm} layout="vertical" style={{ marginTop: 16 }}>
          <Form.Item
            name="name"
            label="项目名称"
            rules={[{ required: true, message: '请输入项目名称' }]}
          >
            <Input placeholder="例如：拆除工程、防水工程" />
          </Form.Item>
        </Form>
      </Modal>

      {/* 添加小分类 Modal */}
      <Modal
        title={`添加小分类 - ${selectedProject?.name}`}
        open={isSubCategoryModalOpen}
        onOk={handleAddSubCategory}
        onCancel={() => {
          setIsSubCategoryModalOpen(false);
          subCategoryForm.resetFields();
        }}
        okText="确定"
        cancelText="取消"
      >
        <Form form={subCategoryForm} layout="vertical" style={{ marginTop: 16 }}>
          <Form.Item
            name="name"
            label="小分类名称"
            rules={[{ required: true, message: '请输入小分类名称' }]}
          >
            <Input placeholder="例如：墙体拆除、地面拆除" />
          </Form.Item>
        </Form>
      </Modal>
    </Layout>
  );
};

export default CategorySettings;
