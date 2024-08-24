import { createBrowserRouter } from 'react-router-dom'
import { DatabasePage } from '../pages/database/DatabasePage.tsx'
import { Layout } from '../components/layout/Layout.tsx'
import { DashboardPage } from '../pages/dashboard/DashboardPage.tsx'
import { CustomModelsPage } from '../pages/customModel/CustomModelsPage.tsx'

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
      }
    ]
  },
])