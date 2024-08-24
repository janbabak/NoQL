import { CustomModel } from '../../types/CustomModel.ts'
import { Card, CardContent, Typography } from '@mui/material'
import React from 'react'

interface CustomModelCardProps {
  customModel: CustomModel
  style?: React.CSSProperties,
  className?: string,
}

export function CustomModelCard(
  {
    customModel,
    style,
    className
  }: CustomModelCardProps) {

  return (
    <Card elevation={3} style={style} className={className}>
      <CardContent>
        <Typography variant="h6" component="h3">{customModel.name}</Typography>
        <Typography>{customModel.host}:{customModel.port}</Typography>
      </CardContent>
    </Card>
  )
}