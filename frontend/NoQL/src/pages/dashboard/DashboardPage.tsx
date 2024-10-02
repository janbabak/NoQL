import { Databases } from './Databases.tsx'
import { Typography } from '@mui/material'
import { memo } from 'react'

const DashboardPage = memo(() => {

  return (
    <>
      <Typography variant="h2" component="h1">Dashboard</Typography>
      <Databases />
    </>
  )
})

export { DashboardPage }