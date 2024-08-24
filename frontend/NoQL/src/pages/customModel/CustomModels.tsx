import { useEffect, useState } from 'react'
import { CustomModel } from '../../types/CustomModel.ts'
import customModelApi from '../../services/api/customModelApi.ts'
import { AppDispatch } from '../../state/store.ts'
import { useDispatch } from 'react-redux'
import { showErrorWithMessageAndError } from '../../components/snackbar/GlobalSnackbar.helpers.ts'
import { CustomModelCard } from './CustomModelCard.tsx'
import styles from './CustomModel.module.css'

export function CustomModels() {

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

  const CustomModelsList =
    <ul>{
      models.map((model: CustomModel) =>
        <CustomModelCard
          customModel={model}
          key={model.id}
          className={styles.customModelCard}
        />)
    }</ul>

  return (
    <>
      {modelsLoading ? 'Loading...' : CustomModelsList}
    </>
  )
}