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
              type="text"
              fullWidth
              variant="standard"
              {...register('name', {
                required: 'Name is required'
              })}
              error={!!errors.name}
              helperText={errors.name?.message}
            />

            <div style={{ display: 'flex', gap: '1rem' }}>
              <TextField
                margin="dense"
                id="host"
                label="host"
                type="text"
                fullWidth
                variant="standard"
                {...register('host')}
              />

              <TextField
                margin="dense"
                id="port"
                label="port"
                type="number"
                fullWidth
                variant="standard"
                {...register('port')}
              />

              <TextField
                margin="dense"
                id="database"
                label="database"
                type="text"
                fullWidth
                variant="standard"
                {...register('database')}
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
                {...register('username')}
              />

              <TextField
                margin="dense"
                id="password"
                label="password"
                type="password"
                fullWidth
                variant="standard"
                {...register('password')}
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