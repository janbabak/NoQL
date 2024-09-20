import { Card, TextField, Typography } from '@mui/material'
import { LoadingButton } from '@mui/lab'
import styles from './Login.module.css'
import { useForm } from 'react-hook-form'
import { useState } from 'react'
import { AuthenticationRequest } from '../../types/Authentication.ts'
import { authenticationApi } from '../../services/api/authenticationApi.ts'
import { AppDispatch } from '../../state/store.ts'
import { useDispatch } from 'react-redux'
import { showErrorMessage } from '../../components/snackbar/GlobalSnackbar.helpers.ts'

export function LoginPage() {

  const defaultValues: AuthenticationRequest = {
    email: 'babka@email.com',
    password: '12345678'
  }

  const form = useForm<AuthenticationRequest>({ defaultValues })

  const {
    register,
    handleSubmit,
  } = form

  const { errors } = form.formState

  const dispatch: AppDispatch = useDispatch()

  const [
    loading,
    setLoading
  ] = useState<boolean>(false)

  async function onSubmit(data: AuthenticationRequest): Promise<void> {
    setLoading(true)
    try {
      await authenticationApi.authenticate(data)
    } catch (error: unknown) {
      const errorMessage = (error as any)?.response.data || 'Password or username is incorrect'
      showErrorMessage(dispatch, errorMessage) // TODO: remove dispatch parameter
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className={styles.page}>
      <Card className={styles.card}>
        <form
          noValidate
          onSubmit={handleSubmit(onSubmit)}
          className={styles.form}
        >
          <Typography
            variant="h4"
            component="h2"
            style={{ marginBottom: '1rem' }}
          >
            Login
          </Typography>

          <TextField
            autoFocus
            margin="dense"
            id="username"
            label="Username"
            placeholder="john.doe@email.com"
            type="email"
            variant="standard"
            fullWidth
            error={!!errors.email}
            helperText={errors.email?.message}
            {...register('email', {
              required: 'Username is required',
              pattern: {
                value: /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,4}$/,
                message: 'Enter a valid email address'
              }
            })}
          />

          <TextField
            margin="dense"
            id="password"
            label="Password"
            type="password"
            variant="standard"
            fullWidth
            error={!!errors.password}
            helperText={errors.password?.message}
            {...register('password', {
              required: 'Password is required'
            })}
          />

          <LoadingButton
            loading={loading}
            type="submit"
            variant="contained"
            style={{ margin: '1.5rem 0' }}
            fullWidth
          >
            Login
          </LoadingButton>

          <div>Don't have account? <a href="/register">Register</a></div>
        </form>
      </Card>
    </div>
  )
}