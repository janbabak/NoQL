import {
  Dialog,
  DialogContent,
  DialogTitle
} from '@mui/material'
import databaseApi from '../../services/api/databaseApi.ts'
import { CreateUpdateDatabaseForm } from '../../components/form/CreateUpdateDatabaseForm.tsx'
import { CreateDatabaseRequest, Database } from '../../types/Database.ts'
import type { AxiosResponse } from 'axios'

interface CreateDatabaseDialogProps {
  open: boolean;
  onClose: () => void;
}

export function CreateDatabaseDialog({ open, onClose }: CreateDatabaseDialogProps) {

  async function createDatabase(data: CreateDatabaseRequest): Promise<AxiosResponse<Database>> {
    return await databaseApi.create(data)
  }

  return (
    <>
      <Dialog open={open} onClose={onClose} maxWidth="md" fullWidth>
        <DialogTitle>Create Database</DialogTitle>

        <DialogContent>
          <CreateUpdateDatabaseForm
            action="create"
            onClose={onClose}
            submit={createDatabase}
          />
        </DialogContent>
      </Dialog>
    </>
  )
}