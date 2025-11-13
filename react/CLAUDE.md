# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a **Home Improvement Management System** (装修管理系统) - a comprehensive web application for managing renovation projects. The application is built with modern web technologies and is designed for managing construction projects, customers, quality checks, and project progress tracking.

**Tech Stack:**
- Vite + React 18 + TypeScript
- React Router for navigation
- TanStack Query for data fetching
- Ant Design (antd) for UI components
- dayjs for date handling

**Project Origin:** This is a Lovable.dev project (URL: https://lovable.dev/projects/66f34a38-aff1-4940-b1c1-2865ec2cdab9)

## Development Commands

```bash
# Install dependencies
npm i

# Start development server (runs on http://[::]:8080)
npm run dev

# Build for production
npm run build

# Build with development mode
npm run build:dev

# Lint code
npm run lint

# Preview production build
npm run preview
```

## Application Architecture

### Routing Structure

The app uses React Router with the following main routes:
- `/` - Redirects to `/login`
- `/login` - Login page (no authentication required)
- `/dashboard` - Overview dashboard with statistics
- `/projects` - Project management
- `/customers` - Customer management
- `/progress` - Project progress tracking
- `/quality` - Quality inspection checks
- `/settings/news` - News/announcements settings
- `/settings/categories` - Category configuration

All routes except `/login` are protected by a simple localStorage-based authentication check in `Layout.tsx:18-23`.

### Component Architecture

**Layout Structure:**
- `Layout.tsx` - Main layout wrapper with integrated sidebar navigation, header, and auth guard (uses Ant Design Layout, Sider, Menu components)
- Pages are wrapped in `Layout` component which provides the app shell
- Sidebar is collapsible and uses Ant Design Menu for navigation

**UI Components:**
All UI components come from Ant Design (antd). The project uses:
- Form components: Form, Input, Select, DatePicker, InputNumber, etc.
- Layout components: Layout, Sider, Header, Content
- Data display: Card, Table, List, Statistic, Progress, Timeline, Descriptions
- Feedback: Modal, message, Alert, Result
- Navigation: Menu, Dropdown
- Icons: @ant-design/icons

**State Management:**
- TanStack Query (`QueryClient`) for server state (configured in `App.tsx`)
- localStorage for authentication state (`isLoggedIn`, `username`)
- Local React state for UI interactions
- Ant Design Form for form state management

### Path Aliases

The project uses `@/` as an alias for the `src/` directory (configured in `vite.config.ts:18-20` and `tsconfig.json:8-11`).

Always use the `@/` alias when importing from src:
```typescript
import { Button } from "antd";
import { mockProjects } from "@/data/mockData";
import Layout from "@/components/Layout";
```

### Form System

Forms in this project are built using **Ant Design Form** component. Key features:

**Form Components:**
- `Form` - Main form wrapper with validation
- `Form.Item` - Individual form fields with labels and validation rules
- `Form.useForm()` - Hook to control form instance

**Validation:**
Forms use Ant Design's built-in validation through the `rules` prop:
```typescript
<Form.Item
  name="email"
  rules={[
    { required: true, message: '请输入邮箱' },
    { type: 'email', message: '请输入有效的邮箱地址' }
  ]}
>
  <Input placeholder="请输入邮箱" />
</Form.Item>
```

**Common Field Types:**
- Input, Input.TextArea - Text input
- InputNumber - Number input
- Select - Dropdown selection
- DatePicker, RangePicker - Date selection
- Checkbox, Radio - Boolean/choice selection
- Upload - File upload

**Usage Pattern:**
```typescript
import { Form, Input, Button } from 'antd';

const MyForm = () => {
  const [form] = Form.useForm();

  const handleSubmit = async (values) => {
    // Handle form submission
  };

  return (
    <Form form={form} onFinish={handleSubmit} layout="vertical">
      <Form.Item name="name" label="名称" rules={[{ required: true }]}>
        <Input />
      </Form.Item>
      <Button type="primary" htmlType="submit">提交</Button>
    </Form>
  );
};
```

### Data Layer

**Mock Data:** `src/data/mockData.ts` contains all mock data structures:
- `mockCustomers` - Customer data
- `mockProjects` - Project data with expenses and timeline
- `mockQualityChecks` - Quality inspection records
- `mockCategories` - Expense categories
- `mockNews` - News/announcement items

**Data Types:**
- `Customer` - Customer information
- `Project` - Project with status (planning/inProgress/completed/suspended), budget, expenses, timeline
- `QualityCheck` - Quality inspection with status (passed/failed/pending)

Currently using mock data. When implementing backend integration, replace imports from `mockData.ts` with actual API calls using TanStack Query.

### TypeScript Configuration

TypeScript is configured with relaxed settings for rapid development:
- `noImplicitAny: false`
- `noUnusedParameters: false`
- `noUnusedLocals: false`
- `strictNullChecks: false`

The project uses TypeScript 5.8+ with separate configs for app (`tsconfig.app.json`) and Node.js (`tsconfig.node.json`).

### Styling

**Ant Design Theme System:**
- Theme configured in `App.tsx` using `ConfigProvider`
- Chinese locale (zhCN) is set globally
- Custom theme tokens can be configured:
  ```typescript
  <ConfigProvider
    locale={zhCN}
    theme={{
      token: {
        colorPrimary: '#1677ff',
        borderRadius: 6,
      },
    }}
  >
  ```

**Styling Guidelines:**
- Use Ant Design components for UI consistency
- Use inline styles or CSS files for custom styling
- Ant Design provides built-in responsive grid system (Row, Col)
- Message API for notifications: `message.success()`, `message.error()`, etc.

### Internationalization

The application is in **Chinese (Simplified)** - all UI text, labels, and content are in Chinese. Ant Design components are configured with `zhCN` locale through `ConfigProvider`. When adding new features, maintain Chinese language for consistency.

## Development Workflow

1. **Adding New Routes:**
   - Create page component in `src/pages/`
   - Add route in `App.tsx` (before the catch-all `*` route)
   - Add menu item to `Layout.tsx` in the `menuItems` array
   - Wrap with `Layout` component for protected routes

2. **Creating New Components:**
   - Import components from `antd` package
   - For forms, use Ant Design Form component with validation rules
   - Place custom components in `src/components/`

3. **Working with Data:**
   - Import types and mock data from `@/data/mockData`
   - Use TanStack Query for data fetching patterns
   - Plan for future backend integration

4. **Testing Changes:**
   - Run `npm run dev` to start dev server
   - Test on `http://localhost:8080`
   - Run `npm run lint` to check for linting issues

## Code Conventions

- Use functional components with hooks
- Prefer `const` arrow functions for component definitions
- Use TypeScript interfaces for type definitions
- Follow existing naming conventions (camelCase for variables/functions, PascalCase for components)
- Keep components focused and single-responsibility
- Use Ant Design Form validation rules for form validation

## Known Patterns

- **Authentication:** Simple localStorage check, no real auth backend
- **Notifications:** Use Ant Design `message` API for toast notifications (`message.success()`, `message.error()`, etc.)
- **Form Handling:** Use Ant Design Form component with declarative validation rules
- **Icons:** Use `@ant-design/icons` for icons
- **Modals:** Use Ant Design Modal component or Modal.confirm() for confirmations
- **Date Handling:** Use `dayjs` library for date manipulation (required by Ant Design DatePicker)
