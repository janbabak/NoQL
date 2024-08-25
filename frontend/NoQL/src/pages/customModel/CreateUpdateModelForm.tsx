import { CreateUpdateCustomModelRequest, CustomModel } from '../../types/CustomModel.ts'
import { AxiosResponse } from 'axios'
import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { showErrorMessage } from '../../components/snackbar/GlobalSnackbar.helpers.ts'
import { AppDispatch } from '../../state/store.ts'
import { useDispatch } from 'react-redux'
import { Button, TextField } from '@mui/material'
import styles from '../dashboard/Dashboard.module.css'
import { LoadingButton } from '@mui/lab'

interface CreateUpdateModelFormProps {
  action: 'create' | 'update';
  onClose?: () => void; // used when form is in dialog
  modelId?: string; // required only when action is set to 'update'
  submit: ((data: CreateUpdateCustomModelRequest) => Promise<AxiosResponse<CustomModel, any>>)
}

export function CreateUpdateModelForm(
  {
    action,
    onClose,
    submit,
    modelId
  }: CreateUpdateModelFormProps) {

  const defaultValues: CreateUpdateCustomModelRequest = {
    name: '',
    host: '',
    port: 8080
  }

  const form = useForm<CreateUpdateCustomModelRequest>({ defaultValues: defaultValues })

  const {
    control,
    register,
    handleSubmit,
    formState,
    reset
  } = form

  const { errors } = formState

  const dispatch: AppDispatch = useDispatch()

  const [
    submitLoading,
    setSubmitLoading
  ] = useState<boolean>(false)

  function handleClose(): void {
    if (action === 'create') {
      reset(defaultValues)
    }

    if (onClose) {
      onClose()
    }
  }

  async function onSubmit(data: CreateUpdateCustomModelRequest): Promise<void> {
    setSubmitLoading(true)

    try {
      await submit(data)
      handleClose()
    } catch (error: unknown) {
      const errorMessage = (error as any)?.response?.data || 'Something went wrong. Please try again later.'
      showErrorMessage(dispatch, errorMessage)
    } finally {
      setSubmitLoading(false)
    }
  }

  const actionButtons =
    <div className={styles.dialogButtonsContainer}>
      <Button onClick={handleClose} aria-label="Cancel">
        Cancel
      </Button>

      <LoadingButton loading={submitLoading} type="submit">
        {action === 'create' ? 'Create' : 'Update'}
      </LoadingButton>
    </div>

  return (
    <form noValidate onSubmit={handleSubmit(onSubmit)}>
      <TextField
        autoFocus
        margin="dense"
        id="name"
        label="Name"
        placeholder="My model"
        type="text"
        fullWidth
        variant="standard"
        error={!!errors.name}
        helperText={errors.name?.message}
        {...register('name', {
          required: 'Name is required',
          maxLength: { value: 32, message: 'Name maximum allowed length is 32' }
        })}
      />

      <TextField
        margin="dense"
        id="host"
        label="Host"
        placeholder="localhost"
        type="text"
        fullWidth
        variant="standard"
        error={!!errors.host}
        helperText={errors.host?.message}
        {...register('host', {
          required: 'Name is required',
          maxLength: { value: 253, message: 'Host maximum length is 253' }
        })}
      />

      <TextField
        margin="dense"
        id="port"
        label="Port"
        placeholder="5432"
        type="number"
        fullWidth
        variant="standard"
        error={!!errors.port}
        helperText={errors.port?.message}
        {...register('port', {
          required: 'Port is required',
          min: { value: 1, message: 'Port has to be greater than 0' },
          pattern: { value: /^[0-9]+$/, message: 'Port must be an integer' }
        })}
      />

      {actionButtons}
    </form>
  )
}