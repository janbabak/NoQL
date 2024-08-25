import customModelApi from '../../services/api/customModelApi.ts'
import { CreateUpdateCustomModelRequest, CustomModel } from '../../types/CustomModel.ts'
import { AxiosResponse } from 'axios'
import { Dialog, DialogContent, DialogTitle } from '@mui/material'
import { CreateUpdateModelForm } from './CreateUpdateModelForm.tsx'

interface CreateModelDialogProps {
  open: boolean
  onClose: () => void
}

export function CreateModelDialog({ open, onClose }: CreateModelDialogProps) {

  async function createModel(data: CreateUpdateCustomModelRequest): Promise<AxiosResponse<CustomModel>> {
    return await customModelApi.create(data)
  }

  return (
    <>
      <Dialog open={open} onClose={onClose} maxWidth="md" fullWidth>
        <DialogTitle>Create Custom Model</DialogTitle>

        <DialogContent>
          <CreateUpdateModelForm
            action="create"
            onClose={onClose}
            submit={createModel}
          />
        </DialogContent>
      </Dialog>
    </>
  )
}