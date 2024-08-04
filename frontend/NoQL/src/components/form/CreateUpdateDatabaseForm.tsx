import { Button, MenuItem, TextField } from '@mui/material'
import styles from '../../pages/dashboard/Dashboard.module.css'
import { Controller, useForm } from 'react-hook-form'
import { Select } from '@mui/material-next'
import { CreateDatabaseRequest, Database, DatabaseEngine, UpdateDatabaseRequest } from '../../types/Database.ts'
import { LoadingButton } from '@mui/lab'
import { AppDispatch } from '../../state/store.ts'
import { useDispatch } from 'react-redux'
import { useEffect, useState } from 'react'
import { showErrorMessage } from '../snackbar/GlobalSnackbar.helpers.ts'
import { AxiosResponse } from 'axios'
import databaseApi from '../../services/api/databaseApi.ts'

interface CreateUpdateDatabaseProps {
  action: 'create' | 'update';
  onClose?: () => void;
  submit: ((data: UpdateDatabaseRequest) => Promise<AxiosResponse<Database, any>>) | ((data: CreateDatabaseRequest) => Promise<AxiosResponse<Database, any>>);
  databaseId?: string;
}

export function CreateUpdateDatabaseForm(
  {
    action,
    onClose,
    submit,
    databaseId
  }: CreateUpdateDatabaseProps) {

  const defaultValues: CreateDatabaseRequest = {
    name: '',
    host: '',
    port: 5433,
    database: '',
    userName: '',
    password: '',
    engine: DatabaseEngine.POSTGRES
  }

  useEffect(() => {
    if (action === 'update' && databaseId) {
      databaseApi.getById(databaseId)
        .then((response): void => {
          const database = response.data
          form.reset({
            name: database.name,
            host: database.host,
            port: database.port,
            database: database.database,
            userName: database.userName,
            password: database.password,
            engine: database.engine
          })
        })
    }
  }, [])

  const form = useForm<CreateDatabaseRequest>({ defaultValues: defaultValues })

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

  function handleClose() {
    if (action === 'create') {
      reset(defaultValues)
    }

    if (onClose) {
      onClose()
    }
  }

  async function onSubmit(data: CreateDatabaseRequest | UpdateDatabaseRequest): Promise<void> {
    setSubmitLoading(true)
    try {
      console.log(data)
      // @ts-ignore
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
        placeholder="My local postgres"
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

      <div className={styles.formRow}>
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

        <TextField
          margin="dense"
          id="database"
          label="Database"
          placeholder="eshopUsers"
          type="text"
          fullWidth
          variant="standard"
          error={!!errors.database}
          helperText={errors.database?.message}
          {...register('database', {
            required: 'Database is required',
            maxLength: { value: 253, message: 'Database maximum length is 253' }
          })}
        />
      </div>

      <div className={styles.formRow}>
        <TextField
          id="username"
          className={styles.formInputSecondRow}
          label="Username"
          type="text"
          fullWidth
          variant="standard"
          error={!!errors.userName}
          helperText={errors.userName?.message}
          {...register('userName', {
            required: 'Username is required',
            maxLength: { value: 128, message: 'Username maximum length is 128' }
          })}
        />

        <TextField
          id="password"
          className={styles.formInputSecondRow}
          label="Password"
          type="password"
          fullWidth
          variant="standard"
          error={!!errors.password}
          helperText={errors.password?.message}
          {...register('password', {
            required: 'Password is required',
            maxLength: { value: 128, message: 'Password maximum length is 128' }
          })}
        />

        <Controller
          name="engine"
          control={control}
          render={({ field }) => (
            <Select
              id="engine"
              label="Engine"
              variant="standard"
              {...field}
              style={{ width: '100%' }}
            >
              {Object.keys(DatabaseEngine)
                .filter((key: string) => isNaN(Number(key)))
                .map((key) => {
                  return (
                    <MenuItem key={key} value={DatabaseEngine[key as keyof typeof DatabaseEngine]}>
                      {key}
                    </MenuItem>)
                })}
            </Select>
          )}
        />
      </div>

      {actionButtons}
    </form>
  )
}