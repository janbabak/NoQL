import { Databases } from './Databases.tsx'
import { Typography } from '@mui/material'

export function DashboardPage() {

  return (
    <>
      <Typography variant="h2" component="h1">Dashboard</Typography>
      <Databases />
    </>
  )
}