import {
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  MenuItem,
  TextField
} from '@mui/material'
import { Select } from '@mui/material-next'
import { Controller, useForm } from 'react-hook-form'
import databaseApi from '../../services/api/databaseApi.ts'
import { useState } from 'react'
import { AppDispatch } from '../../state/store.ts'
import { useDispatch } from 'react-redux'
import { showErrorMessage } from '../../components/snackbar/GlobalSnackbar.helpers.ts'
import { CreateDatabaseRequest, DatabaseEngine } from '../../types/Database.ts'

interface CreateDatabaseDialogProps {
  open: boolean;
  onClose: () => void;
}

export function CreateDatabaseDialog({ open, onClose }: CreateDatabaseDialogProps) {

  const form = useForm<CreateDatabaseRequest>({
    defaultValues: {
      name: 'fail',
      host: 'localhost',
      port: 5433,
      database: 'database',
      userName: 'user',
      password: 'password423432',
      engine: DatabaseEngine.POSTGRES
    }
  })

  const {
    control,
    register,
    handleSubmit,
    formState
  } = form

  const { errors } = formState

  const dispatch: AppDispatch = useDispatch()

  const [
    submitLoading,
    setSubmitLoading
  ] = useState<boolean>(false)


  async function onSubmit(data: CreateDatabaseRequest) {
    setSubmitLoading(true)
    try {
      await databaseApi.create(data)
      onClose()
    } catch (error: unknown) {
      const errorMessage = (error as any).response.data || 'Something went wrong. Please try again later.'
      showErrorMessage(dispatch, errorMessage)
    } finally {
      setSubmitLoading(false)
    }
  }

  return (
    <>
      <Dialog open={open} onClose={onClose} maxWidth="md" fullWidth>
        <DialogTitle>Create Database</DialogTitle>

        <DialogContent>

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

            <div style={{ display: 'flex', gap: '1rem'}}>
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

            <div style={{ display: 'flex', gap: '1rem' }}>
              <TextField
                style={{ marginTop: '0.75rem' }}
                id="username"
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
                style={{ marginTop: '0.75rem' }}
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

            <div style={{width: '100%', display: 'flex', justifyContent: 'flex-end', marginTop: '1rem'}}>
              <Button onClick={onClose}>Cancel</Button>
              <Button type="submit">Create</Button>
            </div>
          </form>

        </DialogContent>
      </Dialog>
    </>
  )
}