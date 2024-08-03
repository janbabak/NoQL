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

interface CreateDatabaseDialogProps {
  open: boolean;
  onClose: () => void;
}

type FormValues = {
  name: string;
  host: string;
  port: number;
  database: string;
  username: string;
  password: string;
  engine: string;
}

export function CreateDatabaseDialog({ open, onClose }: CreateDatabaseDialogProps) {

  const form = useForm<FormValues>({
    defaultValues: {
      name: 'def name',
      host: 'def host',
      port: 2,
      database: 'def database',
      username: 'def username',
      password: 'def password',
      engine: 'postgres'
    }
  })

  const {
    control,
    register,
    handleSubmit,
    formState
  } = form

  const { errors } = formState

  function onSubmit(data: FormValues) {
    console.log(data)
  }

  return (
    <>
      <Dialog open={open} onClose={onClose}>
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
                maxLength: { value: 32, message: 'Name maximum allowed length is 32' },
              })}
            />

            <div style={{ display: 'flex', gap: '1rem' }}>
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
                  maxLength: { value: 253, message: 'Host maximum length is 253' },
                })}
              />

              <TextField
                margin="dense"
                id="port"
                label="port"
                placeholder="5432"
                type="number"
                fullWidth
                variant="standard"
                error={!!errors.port}
                helperText={errors.port?.message}
                {...register('port', {
                  required: 'Port is required',
                  min: { value: 1, message: 'Port has to be greater than 0'},
                  pattern: { value: /^[0-9]+$/, message: 'Port must be an integer' },
                })}
              />

              <TextField
                margin="dense"
                id="database"
                label="database"
                placeholder='eshopUsers'
                type="text"
                fullWidth
                variant="standard"
                error={!!errors.database}
                helperText={errors.database?.message}
                {...register('database', {
                  required: 'Database is required',
                  maxLength: { value: 253, message: 'Database maximum length is 253' },
                })}
              />
            </div>

            <div style={{ display: 'flex', gap: '1rem' }}>
              <TextField
                margin="dense"
                id="username"
                label="username"
                type="text"
                fullWidth
                variant="standard"
                error={!!errors.username}
                helperText={errors.username?.message}
                {...register('username', {
                  required: 'Username is required',
                  maxLength: { value: 128, message: 'Username maximum length is 128' }
                })}
              />

              <TextField
                margin="dense"
                id="password"
                label="password"
                type="password"
                fullWidth
                variant="standard"
                error={!!errors.password}
                helperText={errors.password?.message}
                {...register('password', {
                  required: 'Password is required',
                  minLength: { value: 128, message: 'Password maximum length is 128' },
                })}
              />

              <Controller
                name="engine"
                control={control}
                render={({ field }) => (
                  <Select
                    id="engine"
                    label="engine"
                    variant="standard"
                    {...field} // Use field from Controller
                  >
                    <MenuItem value="postgres">Postgres</MenuItem>
                    <MenuItem value="mysql">MySql</MenuItem>
                  </Select>
                )}
              />
            </div>

            <DialogActions>
              <Button onClick={onClose}>Cancel</Button>
              <Button type="submit">Create</Button>
            </DialogActions>
          </form>

        </DialogContent>
      </Dialog>
    </>
  )
}