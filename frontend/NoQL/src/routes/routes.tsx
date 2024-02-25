import { createBrowserRouter } from 'react-router-dom'
import { DashboardPage } from '../pages/dashboard/DashboardPage.tsx'
import { DatabasePage } from '../pages/database/DatabasePage.tsx'

export const router = createBrowserRouter([
  {
    path: '/',
    element: <DashboardPage />
  },
  {
    path: '/database/:id',
    element: <DatabasePage />
  }
])