import { Modal, Card, Upload, Button, Space, Image, Popconfirm, Typography, Empty, message, Timeline, Tag, Input, Form } from 'antd';
import { PlusOutlined, DeleteOutlined, HistoryOutlined, ClockCircleOutlined, UserOutlined, FolderAddOutlined, FolderOutlined } from '@ant-design/icons';
import { useState } from 'react';
import type { UploadFile } from 'antd/es/upload/interface';
import type { ProjectDesign, RoomDesign, DesignImage } from '@/data/mockData';
import dayjs from 'dayjs';

const { Text, Title } = Typography;

interface DesignManagementProps {
  open: boolean;
  onCancel: () => void;
  projectId: string;
  projectName: string;
  designData: ProjectDesign | null;
  onUpdate: (data: ProjectDesign) => void;
}

const DesignManagement = ({
  open,
  onCancel,
  projectId,
  projectName,
  designData,
  onUpdate,
}: DesignManagementProps) => {
  const [versionModalOpen, setVersionModalOpen] = useState(false);
  const [selectedRoom, setSelectedRoom] = useState<RoomDesign | null>(null);
  const [addRoomModalOpen, setAddRoomModalOpen] = useState(false);
  const [form] = Form.useForm();

  // 添加房间
  const handleAddRoom = () => {
    form.validateFields().then(values => {
      if (!designData) {
        // 如果项目还没有设计稿数据，创建新的
        const newDesign: ProjectDesign = {
          projectId,
          rooms: [{
            id: `room-${Date.now()}`,
            roomName: values.roomName,
            images: [],
            versionHistory: [],
          }],
        };
        onUpdate(newDesign);
      } else {
        // 添加到现有数据
        const newRoom: RoomDesign = {
          id: `room-${Date.now()}`,
          roomName: values.roomName,
          images: [],
          versionHistory: [],
        };

        onUpdate({
          ...designData,
          rooms: [...designData.rooms, newRoom],
        });
      }

      form.resetFields();
      setAddRoomModalOpen(false);
      message.success(`房间"${values.roomName}"已添加`);
    });
  };

  // 删除房间
  const handleDeleteRoom = (roomId: string, roomName: string) => {
    if (!designData) return;

    onUpdate({
      ...designData,
      rooms: designData.rooms.filter(r => r.id !== roomId),
    });
    message.success(`房间"${roomName}"已删除`);
  };

  // 处理图片上传
  const handleUpload = (roomId: string, fileList: UploadFile[]) => {
    if (!designData) return;

    const newImages: DesignImage[] = fileList.map((file, index) => ({
      id: `img-${Date.now()}-${index}`,
      url: file.url || file.thumbUrl || '',
      name: file.name,
      uploadTime: dayjs().format('YYYY-MM-DD HH:mm:ss'),
      uploader: '当前用户',
      version: 1,
    }));

    const updatedRooms = designData.rooms.map(room => {
      if (room.id === roomId) {
        // 如果该房间已有图片，将旧图片移到历史版本
        const currentImages = room.images;
        if (currentImages.length > 0) {
          const versionHistory = room.versionHistory || [];
          const updatedHistory = [
            ...currentImages.map(img => ({
              ...img,
              version: img.version + 1,
            })),
            ...versionHistory,
          ];

          return {
            ...room,
            images: [...room.images, ...newImages],
            versionHistory: updatedHistory,
          };
        }

        return {
          ...room,
          images: [...room.images, ...newImages],
        };
      }
      return room;
    });

    onUpdate({
      ...designData,
      rooms: updatedRooms,
    });

    message.success(`已上传 ${newImages.length} 张图片`);
  };

  // 删除图片
  const handleDeleteImage = (roomId: string, imageId: string) => {
    if (!designData) return;

    const updatedRooms = designData.rooms.map(room => {
      if (room.id === roomId) {
        // 将被删除的图片移到历史版本
        const deletedImage = room.images.find(img => img.id === imageId);
        if (deletedImage) {
          const versionHistory = room.versionHistory || [];
          return {
            ...room,
            images: room.images.filter(img => img.id !== imageId),
            versionHistory: [
              { ...deletedImage, version: deletedImage.version + 1 },
              ...versionHistory,
            ],
          };
        }
      }
      return room;
    });

    onUpdate({
      ...designData,
      rooms: updatedRooms,
    });

    message.success('图片已删除');
  };

  // 查看版本历史
  const handleViewHistory = (room: RoomDesign) => {
    setSelectedRoom(room);
    setVersionModalOpen(true);
  };

  // 渲染房间卡片
  const renderRoomCard = (room: RoomDesign) => {
    const hasHistory = room.versionHistory && room.versionHistory.length > 0;

    return (
      <Card
        key={room.id}
        title={
          <Space>
            <FolderOutlined style={{ color: '#1677ff' }} />
            <Text strong style={{ fontSize: 16 }}>{room.roomName}</Text>
            <Tag color="blue">{room.images.length} 张图片</Tag>
            {hasHistory && (
              <Tag color="orange">{room.versionHistory.length} 个历史版本</Tag>
            )}
          </Space>
        }
        extra={
          <Space>
            {hasHistory && (
              <Button
                type="link"
                icon={<HistoryOutlined />}
                onClick={() => handleViewHistory(room)}
              >
                版本历史
              </Button>
            )}
            <Popconfirm
              title="确定删除此房间？"
              description="删除后该房间的所有图片都将丢失"
              onConfirm={() => handleDeleteRoom(room.id, room.roomName)}
              okText="确定"
              cancelText="取消"
              okType="danger"
            >
              <Button
                type="link"
                danger
                icon={<DeleteOutlined />}
              >
                删除房间
              </Button>
            </Popconfirm>
          </Space>
        }
        style={{ marginBottom: 16 }}
      >
        <Space direction="vertical" style={{ width: '100%' }} size="middle">
          {/* 图片展示 */}
          {room.images.length > 0 ? (
            <div style={{ display: 'flex', flexWrap: 'wrap', gap: 8 }}>
              <Image.PreviewGroup>
                {room.images.map((image) => (
                  <div key={image.id} style={{ position: 'relative' }}>
                    <Image
                      width={150}
                      height={150}
                      src={image.url}
                      style={{ objectFit: 'cover', borderRadius: 4 }}
                      placeholder={
                        <div style={{
                          width: 150,
                          height: 150,
                          display: 'flex',
                          alignItems: 'center',
                          justifyContent: 'center',
                          background: '#f0f0f0',
                        }}>
                          加载中...
                        </div>
                      }
                    />
                    <Popconfirm
                      title="确定删除此图片？"
                      description="删除的图片将保存在版本历史中"
                      onConfirm={() => handleDeleteImage(room.id, image.id)}
                      okText="确定"
                      cancelText="取消"
                    >
                      <Button
                        type="primary"
                        danger
                        size="small"
                        icon={<DeleteOutlined />}
                        style={{
                          position: 'absolute',
                          top: 4,
                          right: 4,
                        }}
                      />
                    </Popconfirm>
                    <div style={{
                      position: 'absolute',
                      bottom: 0,
                      left: 0,
                      right: 0,
                      background: 'rgba(0,0,0,0.7)',
                      color: 'white',
                      padding: '4px 8px',
                      fontSize: 12,
                    }}>
                      <div style={{
                        overflow: 'hidden',
                        textOverflow: 'ellipsis',
                        whiteSpace: 'nowrap'
                      }}>
                        {image.name}
                      </div>
                      <div style={{ fontSize: 10, opacity: 0.9 }}>
                        <ClockCircleOutlined /> {image.uploadTime}
                      </div>
                    </div>
                  </div>
                ))}
              </Image.PreviewGroup>
            </div>
          ) : (
            <Empty
              description={`暂无${room.roomName}的设计图`}
              image={Empty.PRESENTED_IMAGE_SIMPLE}
            />
          )}

          {/* 上传按钮 */}
          <Upload
            listType="picture-card"
            beforeUpload={() => false}
            multiple
            showUploadList={false}
            onChange={({ fileList }) => handleUpload(room.id, fileList)}
          >
            <div>
              <PlusOutlined />
              <div style={{ marginTop: 8 }}>上传设计图</div>
            </div>
          </Upload>
        </Space>
      </Card>
    );
  };

  return (
    <>
      <Modal
        title={
          <Space>
            <FolderOutlined style={{ fontSize: 20, color: '#1677ff' }} />
            <span style={{ fontSize: 18 }}>{projectName} - 设计稿管理</span>
          </Space>
        }
        open={open}
        onCancel={onCancel}
        width={1200}
        footer={[
          <Button key="close" onClick={onCancel}>
            关闭
          </Button>,
        ]}
      >
        <div style={{ marginBottom: 16 }}>
          <Button
            type="primary"
            icon={<FolderAddOutlined />}
            onClick={() => setAddRoomModalOpen(true)}
          >
            添加房间
          </Button>
          <Text type="secondary" style={{ marginLeft: 16 }}>
            按房间分类管理设计图纸，可自定义添加房间
          </Text>
        </div>

        <div style={{ maxHeight: '60vh', overflowY: 'auto' }}>
          {designData && designData.rooms.length > 0 ? (
            designData.rooms.map(room => renderRoomCard(room))
          ) : (
            <Empty
              description="暂无房间分类，请先添加房间"
              image={Empty.PRESENTED_IMAGE_SIMPLE}
              style={{ padding: '40px 0' }}
            >
              <Button
                type="primary"
                icon={<FolderAddOutlined />}
                onClick={() => setAddRoomModalOpen(true)}
              >
                添加第一个房间
              </Button>
            </Empty>
          )}
        </div>
      </Modal>

      {/* 添加房间Modal */}
      <Modal
        title={
          <Space>
            <FolderAddOutlined style={{ color: '#1677ff' }} />
            <span>添加房间</span>
          </Space>
        }
        open={addRoomModalOpen}
        onCancel={() => {
          setAddRoomModalOpen(false);
          form.resetFields();
        }}
        onOk={handleAddRoom}
        okText="添加"
        cancelText="取消"
      >
        <Form
          form={form}
          layout="vertical"
          style={{ marginTop: 24 }}
        >
          <Form.Item
            name="roomName"
            label="房间名称"
            rules={[
              { required: true, message: '请输入房间名称' },
              { max: 20, message: '房间名称不能超过20个字' },
            ]}
          >
            <Input
              placeholder="例如：客厅、主卧、次卧、厨房、卫生间等"
              size="large"
              maxLength={20}
              showCount
            />
          </Form.Item>
        </Form>
      </Modal>

      {/* 版本历史Modal */}
      <Modal
        title={
          <Space>
            <HistoryOutlined style={{ color: '#1677ff' }} />
            <span>{selectedRoom?.roomName} - 版本历史</span>
          </Space>
        }
        open={versionModalOpen}
        onCancel={() => {
          setVersionModalOpen(false);
          setSelectedRoom(null);
        }}
        width={800}
        footer={[
          <Button
            key="close"
            onClick={() => {
              setVersionModalOpen(false);
              setSelectedRoom(null);
            }}
          >
            关闭
          </Button>,
        ]}
      >
        {selectedRoom?.versionHistory && selectedRoom.versionHistory.length > 0 ? (
          <Timeline
            items={selectedRoom.versionHistory.map((image, index) => ({
              color: 'blue',
              children: (
                <Card size="small" style={{ marginBottom: 8 }}>
                  <Space direction="vertical" style={{ width: '100%' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                      <Text strong>版本 {image.version}</Text>
                      <Tag color="blue">历史版本</Tag>
                    </div>
                    <Image
                      width={200}
                      src={image.url}
                      style={{ objectFit: 'cover', borderRadius: 4 }}
                    />
                    <div>
                      <Text type="secondary">文件名：</Text>
                      <Text>{image.name}</Text>
                    </div>
                    <div>
                      <Text type="secondary">
                        <ClockCircleOutlined /> 上传时间：
                      </Text>
                      <Text>{image.uploadTime}</Text>
                    </div>
                    <div>
                      <Text type="secondary">
                        <UserOutlined /> 上传人：
                      </Text>
                      <Text>{image.uploader}</Text>
                    </div>
                  </Space>
                </Card>
              ),
            }))}
          />
        ) : (
          <Empty description="暂无历史版本" image={Empty.PRESENTED_IMAGE_SIMPLE} />
        )}
      </Modal>
    </>
  );
};

export default DesignManagement;
