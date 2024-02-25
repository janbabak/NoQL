import { createBrowserRouter } from 'react-router-dom'
import App from '../App.tsx'
import { ExamplePage } from '../pages/ExamplePage.tsx'

export const router = createBrowserRouter([
  {
    path: '/',
    element: <App />,
  },
  {
    path: '/example',
    element: <ExamplePage />
  }
])