import React from 'react'
import { Navigate, useLocation } from 'react-router-dom'
import { localStorageService } from '../services/LocalStorageService.ts'

interface PrivateRouteProps {
  element: React.ReactElement
}

export const PrivateRoute: React.FC<PrivateRouteProps> = ({ element }) => {
  const location = useLocation()
  const token = localStorageService.getAccessToken()

  if (!token) {
    return <Navigate to="/login" state={{ from: location }} replace />
  }

  return element
}