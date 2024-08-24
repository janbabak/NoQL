import { Typography } from '@mui/material'
import { CustomModels } from './CustomModels.tsx'

export function CustomModelsPage() {
  return (
    <>
      <Typography variant="h2" component="h1">Custom model</Typography>
      <CustomModels />
    </>
  )
}