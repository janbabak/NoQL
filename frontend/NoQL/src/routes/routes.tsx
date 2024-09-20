import { createBrowserRouter } from 'react-router-dom'
import { DatabasePage } from '../pages/database/DatabasePage.tsx'
import { Layout } from '../components/layout/Layout.tsx'
import { DashboardPage } from '../pages/dashboard/DashboardPage.tsx'
import { CustomModelsPage } from '../pages/customModel/CustomModelsPage.tsx'
import { CustomModelDetail } from '../pages/customModel/CustomModelDetail.tsx'
import { LoginPage } from '../pages/login/LoginPage.tsx'
import { RegisterPage } from '../pages/login/RegisterPage.tsx'

export const router = createBrowserRouter([
  {
    path: '/',
    element: <Layout />,
    children: [
      {
        path: '/',
        element: <DashboardPage />
      },
      {
        path: '/database/:id',
        element: <DatabasePage />
      },
      {
        path: '/customModels',
        element: <CustomModelsPage />
      },
      {
        path: '/customModels/:id',
        element: <CustomModelDetail />
      },
      {
        path: '/login',
        element: <LoginPage />
      },
      {
        path: '/register',
        element: <RegisterPage />
      }
    ]
  },
])