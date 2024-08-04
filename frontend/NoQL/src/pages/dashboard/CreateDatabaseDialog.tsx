import {
  Dialog,
  DialogContent,
  DialogTitle
} from '@mui/material'
import databaseApi from '../../services/api/databaseApi.ts'
import { CreateUpdateDatabaseForm } from '../../components/form/CreateUpdateDatabaseForm.tsx'

interface CreateDatabaseDialogProps {
  open: boolean;
  onClose: () => void;
}

export function CreateDatabaseDialog({ open, onClose }: CreateDatabaseDialogProps) {

  return (
    <>
      <Dialog open={open} onClose={onClose} maxWidth="md" fullWidth>
        <DialogTitle>Create Database</DialogTitle>

        <DialogContent>
          <CreateUpdateDatabaseForm
            action="create"
            onClose={onClose}
            submit={databaseApi.create}
          />
        </DialogContent>
      </Dialog>
    </>
  )
}