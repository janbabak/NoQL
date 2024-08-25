import { useEffect, useState } from 'react'
import { CustomModel } from '../../types/CustomModel.ts'
import customModelApi from '../../services/api/customModelApi.ts'
import { AppDispatch } from '../../state/store.ts'
import { useDispatch } from 'react-redux'
import { showErrorWithMessageAndError } from '../../components/snackbar/GlobalSnackbar.helpers.ts'
import { CustomModelCard } from './CustomModelCard.tsx'
import styles from './CustomModel.module.css'
import { SkeletonStack } from '../../components/loaders/SkeletonStack.tsx'
import { Button, Typography } from '@mui/material'
import AddIcon from '@mui/icons-material/Add'
import { CreateModelDialog } from './CreateModelDialog.tsx'

export function CustomModelsPage() {

  const dispatch: AppDispatch = useDispatch()

  const [
    models,
    setModels
  ] = useState<CustomModel[]>([])

  const [
    modelsLoading,
    setModelsLoading
  ] = useState<boolean>(false)

  useEffect((): void => {
    void loadModels()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  async function loadModels(): Promise<void> {
    setModelsLoading(true)
    try {
      const response = await customModelApi.getAll()
      setModels(response.data)
    } catch (error: unknown) {
      showErrorWithMessageAndError(dispatch, 'Failed to load custom models', error)
    } finally {
      setModelsLoading(false)
    }
  }

  const [
    deleteModelLoading,
    setDeleteModelLoading
  ] = useState<boolean>(false)

  async function deleteModel(modelId: string): Promise<void> {
    setDeleteModelLoading(true)
    try {
      await customModelApi.delete(modelId)
    } catch (error: unknown) {
      showErrorWithMessageAndError(dispatch, 'Failed to delete custom model', error)
    } finally {
      setDeleteModelLoading(false)
    }
    void loadModels()
  }

  const [
    createModelDialogOpen,
    setCreateModelDialogOpen
  ] = useState<boolean>(false)


  function openCreateModelDialog(): void {
    setCreateModelDialogOpen(true)
  }

  function closeCreateModelDialog(): void {
    setCreateModelDialogOpen(false)
    void loadModels()
  }

  const CustomModelsList =
    <ul>{
      models.map((model: CustomModel) =>
        <CustomModelCard
          customModel={model}
          key={model.id}
          className={styles.customModelCard}
          deleteCustomModel={deleteModel}
          deleteCustomModelLoading={deleteModelLoading}
        />)
    }</ul>

  const Header =
    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-end' }}>
      <Typography variant="h2" component="h1">Custom models</Typography>
      <div>
        <Button
          variant="contained"
          startIcon={<AddIcon />}
          onClick={openCreateModelDialog}>
          Create
        </Button>
      </div>
    </div>

  return (
    <>
      {Header}

      {modelsLoading
        ? <SkeletonStack height={104} style={{margin: '1rem 0'}}/>
        : CustomModelsList}

      <CreateModelDialog
        open={createModelDialogOpen}
        onClose={closeCreateModelDialog}
      />
    </>
  )
}