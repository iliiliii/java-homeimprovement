import { ReactNode, useEffect, useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { Layout as AntLayout, Menu, Button, Dropdown, Avatar, Typography } from 'antd';
import type { MenuProps } from 'antd';
import {
  DashboardOutlined,
  FolderOutlined,
  TeamOutlined,
  LineChartOutlined,
  CheckCircleOutlined,
  SettingOutlined,
  LogoutOutlined,
  UserOutlined,
  HomeOutlined,
  FileTextOutlined,
  TagsOutlined,
  MenuFoldOutlined,
  MenuUnfoldOutlined,
} from '@ant-design/icons';
import { message } from 'antd';

const { Header, Sider, Content } = AntLayout;
const { Text } = Typography;

interface LayoutProps {
  children: ReactNode;
}

const Layout = ({ children }: LayoutProps) => {
  const [collapsed, setCollapsed] = useState(false);
  const navigate = useNavigate();
  const location = useLocation();

  useEffect(() => {
    const isLoggedIn = localStorage.getItem('isLoggedIn');
    if (!isLoggedIn && location.pathname !== '/login') {
      navigate('/login');
    }
  }, [navigate, location]);

  const handleLogout = () => {
    localStorage.removeItem('isLoggedIn');
    localStorage.removeItem('username');
    message.success('已退出登录，期待您的再次光临');
    navigate('/login');
  };

  const username = localStorage.getItem('username') || '管理员';

  const menuItems: MenuProps['items'] = [
    {
      key: '/dashboard',
      icon: <DashboardOutlined />,
      label: '总览看板',
      onClick: () => navigate('/dashboard'),
    },
    {
      key: '/projects',
      icon: <FolderOutlined />,
      label: '项目管理',
      onClick: () => navigate('/projects'),
    },
    {
      key: '/customers',
      icon: <TeamOutlined />,
      label: '客户管理',
      onClick: () => navigate('/customers'),
    },
    {
      key: '/progress',
      icon: <LineChartOutlined />,
      label: '进度跟踪',
      onClick: () => navigate('/progress'),
    },
    {
      key: '/quality',
      icon: <CheckCircleOutlined />,
      label: '质量检查',
      onClick: () => navigate('/quality'),
    },
    {
      key: '/team',
      icon: <UserOutlined />,
      label: '人员管理',
      onClick: () => navigate('/team'),
    },
    {
      type: 'divider',
    },
    {
      key: 'settings',
      icon: <SettingOutlined />,
      label: '系统配置',
      children: [
        {
          key: '/settings/news',
          icon: <FileTextOutlined />,
          label: '资讯设置',
          onClick: () => navigate('/settings/news'),
        },
        {
          key: '/settings/categories',
          icon: <TagsOutlined />,
          label: '分类设置',
          onClick: () => navigate('/settings/categories'),
        },
      ],
    },
  ];

  const userMenuItems: MenuProps['items'] = [
    {
      key: 'logout',
      icon: <LogoutOutlined />,
      label: '退出登录',
      onClick: handleLogout,
    },
  ];

  return (
    <AntLayout style={{ height: '100vh', overflow: 'hidden' }}>
      <Sider
        trigger={null}
        collapsible
        collapsed={collapsed}
        style={{
          overflow: 'auto',
          height: '100vh',
          position: 'fixed',
          left: 0,
          top: 0,
          bottom: 0,
        }}
      >
        <div style={{
          height: 64,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          borderBottom: '1px solid rgba(255, 255, 255, 0.1)',
        }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
            <HomeOutlined style={{ fontSize: 24, color: '#1677ff' }} />
            {!collapsed && (
              <div>
                <div style={{ color: '#fff', fontWeight: 600, fontSize: 14 }}>装修管理</div>
                <div style={{ color: 'rgba(255, 255, 255, 0.65)', fontSize: 12 }}>Management System</div>
              </div>
            )}
          </div>
        </div>
        <Menu
          theme="dark"
          mode="inline"
          selectedKeys={[location.pathname]}
          defaultOpenKeys={['settings']}
          items={menuItems}
          style={{ borderRight: 0 }}
        />
      </Sider>
      <AntLayout style={{ marginLeft: collapsed ? 80 : 200, transition: 'all 0.2s' }}>
        <Header style={{
          padding: '0 24px',
          background: '#fff',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          borderBottom: '1px solid #f0f0f0',
          position: 'sticky',
          top: 0,
          zIndex: 1,
        }}>
          <Button
            type="text"
            icon={collapsed ? <MenuUnfoldOutlined /> : <MenuFoldOutlined />}
            onClick={() => setCollapsed(!collapsed)}
            style={{ fontSize: 16 }}
          />
          <div style={{ display: 'flex', alignItems: 'center', gap: 16 }}>
            <Text type="secondary">欢迎，{username}</Text>
            <Dropdown menu={{ items: userMenuItems }} placement="bottomRight">
              <Avatar icon={<UserOutlined />} style={{ cursor: 'pointer', backgroundColor: '#1677ff' }} />
            </Dropdown>
          </div>
        </Header>
        <Content style={{
          height: 'calc(100vh - 64px)',
          overflow: 'hidden',
          display: 'flex',
          flexDirection: 'column'
        }}>
          <div style={{
            flex: 1,
            overflow: 'auto',
            padding: '24px'
          }}>
            {children}
          </div>
        </Content>
      </AntLayout>
    </AntLayout>
  );
};

export default Layout;
