import { Typography } from '@mui/material'
import { CreateUpdateDatabaseForm } from '../../../components/form/CreateUpdateDatabaseForm.tsx'
import databaseApi from '../../../services/api/databaseApi.ts'

export function DatabaseSettings() {
  return (
    <>
      <Typography variant="h2" component="h1">Settings</Typography>

      <CreateUpdateDatabaseForm
        action="update"
        onClose={() => {}}
        submit={databaseApi.update}
      />
    </>
  )
}