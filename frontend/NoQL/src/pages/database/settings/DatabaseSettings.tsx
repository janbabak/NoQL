import { Typography } from '@mui/material'
import { CreateUpdateDatabaseForm } from '../../../components/form/CreateUpdateDatabaseForm.tsx'
import databaseApi from '../../../services/api/databaseApi.ts'
import { Database, UpdateDatabaseRequest } from '../../../types/Database.ts'
import type { AxiosResponse } from 'axios'

interface DatabaseSettingsProps {
  databaseId: string
}

export function DatabaseSettings({ databaseId }: DatabaseSettingsProps) {

  async function updateDatabase(data: UpdateDatabaseRequest): Promise<AxiosResponse<Database>> {
    return await databaseApi.update(data)
  }

  return (
    <>
      <Typography variant="h2" component="h1">Settings</Typography>

      <CreateUpdateDatabaseForm
        action="update"
        submit={updateDatabase}
        databaseId={databaseId}
      />
    </>
  )
}