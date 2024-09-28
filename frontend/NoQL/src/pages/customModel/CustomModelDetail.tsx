import { useParams } from 'react-router'
import { CustomModel, UpdateCustomModelRequest } from '../../types/CustomModel.ts'
import { CreateUpdateModelForm } from './CreateUpdateModelForm.tsx'
import customModelApi from '../../services/api/customModelApi.ts'
import { AxiosResponse } from 'axios'
import { useNavigate } from 'react-router-dom'
import { Typography } from '@mui/material'

export function CustomModelDetail() {

  const { id } = useParams<string>()

  async function updateCustomModel(data: UpdateCustomModelRequest): Promise<AxiosResponse<CustomModel>> {
    return await customModelApi.update(id || '', data)
  }

  const navigate = useNavigate()

  function cancelUpdate(): void {
    navigate('/customModels')
  }

  return (
    <>
      <Typography variant="h2" component="h1">Custom Model Detail</Typography>

      <CreateUpdateModelForm
        action={'update'}
        submit={updateCustomModel} modelId={id}
        onClose={cancelUpdate}
      />

    </>
  )
}