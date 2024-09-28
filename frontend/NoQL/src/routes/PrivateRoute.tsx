import React from 'react'
import { Navigate, useLocation } from 'react-router-dom'

interface PrivateRouteProps {
  element: React.ReactElement
}

export const PrivateRoute: React.FC<PrivateRouteProps> = ({ element }) => {
  const location = useLocation()

  const token = localStorage.getItem('token')
  console.log(token)
  if (!token) {
    return <Navigate to="/login" state={{ from: location }} replace />
  }

  return element
}