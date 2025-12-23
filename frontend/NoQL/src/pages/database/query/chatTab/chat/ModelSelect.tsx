import { FormControl, LinearProgress, MenuItem } from '@mui/material'
import { Select, SelectChangeEvent } from '@mui/material-next'
import React, { memo, useEffect, useState } from 'react'
import styles from './Chat.module.css'
import { ModelOption } from '../../../../../types/CustomModel.ts'
import customModelApi from '../../../../../services/api/customModelApi.ts'
import { showErrorWithMessageAndError } from '../../../../../components/snackbar/GlobalSnackbar.helpers.ts'
import { useDispatch } from 'react-redux'
import { AppDispatch } from '../../../../../state/store.ts'
import { localStorageService } from '../../../../../services/LocalStorageService.ts'

interface ModelSelectProps {
  model: string,
  setModel: React.Dispatch<React.SetStateAction<string>>
}

const ModelSelect = memo(({ model, setModel }: ModelSelectProps) => {

  const [
    modelOptions,
    setModelOptions
  ] = useState<ModelOption[]>([
    // fallback if not loaded from backend
    { label: 'GPT 5 mini', value: 'gpt-5-mini' },
    { label: 'GPT 5.2', value: 'gpt-5.2' },
    { label: 'GPT 5 nano', value: 'gpt-5-nano' },
    { label: 'GPT 4o mini', value: 'gpt-4o-mini' },
    { label: 'GPT 4o', value: 'gpt-4o' },
    { label: 'Claude 4.5 haiku', value: 'claude-haiku-4-5-20251001' },
  ])

  const [
    modelOptionsLoading,
    setModelOptionsLoading
  ] = useState<boolean>(false)

  const dispatch: AppDispatch = useDispatch()

  useEffect(() => {
    void loadModelOptions()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  async function loadModelOptions(): Promise<void> {
    setModelOptionsLoading(true)
    try {
      const response = await customModelApi.getAllModels(
        localStorageService.getUserId()
      )
      setModelOptions(response.data)
    } catch (error: unknown) {
      showErrorWithMessageAndError(dispatch, 'Failed to load models', error)
    } finally {
      setModelOptionsLoading(false)
    }
  }

  function selectModel(event: SelectChangeEvent): void {
    setModel(event.target.value)
  }

  return (
    <div>
      <FormControl>
        <Select
          value={model}
          onChange={selectModel}
          size="small"
          className={styles.modelSelect}
        >
          {modelOptions.map((modelOption: ModelOption) => {
            return (
              <MenuItem
                key={modelOption.value}
                value={modelOption.value}
              >
                {modelOption.label}
              </MenuItem>)
          })}
          {modelOptionsLoading && <LinearProgress />}
        </Select>
      </FormControl>
    </div>
  )
})

export { ModelSelect }