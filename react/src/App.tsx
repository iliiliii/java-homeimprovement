import { ConfigProvider, App as AntApp } from 'antd';
import zhCN from 'antd/locale/zh_CN';
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import LoginEnhanced from "./pages/LoginEnhanced";
import Dashboard from "./pages/Dashboard";
import Projects from "./pages/Projects";
import Customers from "./pages/Customers";
import Progress from "./pages/Progress";
import Quality from "./pages/Quality";
import TeamManagement from "./pages/TeamManagement";
import NewsSettings from "./pages/NewsSettings";
import CategorySettings from "./pages/CategorySettings";
import NotFound from "./pages/NotFound";

const queryClient = new QueryClient();

const App = () => (
  <ConfigProvider
    locale={zhCN}
    theme={{
      token: {
        colorPrimary: '#1677ff',
        borderRadius: 6,
      },
    }}
  >
    <AntApp>
      <QueryClientProvider client={queryClient}>
        <BrowserRouter>
          <Routes>
            <Route path="/" element={<Navigate to="/login" replace />} />
            <Route path="/login" element={<LoginEnhanced />} />
            <Route path="/dashboard" element={<Dashboard />} />
            <Route path="/projects" element={<Projects />} />
            <Route path="/customers" element={<Customers />} />
            <Route path="/progress" element={<Progress />} />
            <Route path="/quality" element={<Quality />} />
            <Route path="/team" element={<TeamManagement />} />
            <Route path="/settings/news" element={<NewsSettings />} />
            <Route path="/settings/categories" element={<CategorySettings />} />
            {/* ADD ALL CUSTOM ROUTES ABOVE THE CATCH-ALL "*" ROUTE */}
            <Route path="*" element={<NotFound />} />
          </Routes>
        </BrowserRouter>
      </QueryClientProvider>
    </AntApp>
  </ConfigProvider>
);

export default App;
