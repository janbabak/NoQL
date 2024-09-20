// src/components/PrivateRoute.tsx
import React from 'react'
import { Navigate, useLocation } from 'react-router-dom'
import { useSelector } from 'react-redux'
import { RootState } from '../state/store.ts'

interface PrivateRouteProps {
  element: React.ReactElement
}

export const PrivateRoute: React.FC<PrivateRouteProps> = ({ element }) => {
  const token: string | null = useSelector((state: RootState) => state.authReducer.token)
  const location = useLocation()

  console.log(token)

  if (!token) {
    return <Navigate to="/login" state={{ from: location }} replace />
  }

  return element
}