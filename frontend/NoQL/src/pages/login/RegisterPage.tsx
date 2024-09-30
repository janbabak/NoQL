import styles from './Login.module.css'
import { Card, TextField, Typography } from '@mui/material'
import { LoadingButton } from '@mui/lab'
import { RegisterRequest } from '../../types/Authentication.ts'
import { useForm } from 'react-hook-form'
import { useNavigate } from 'react-router-dom'
import { AppDispatch } from '../../state/store.ts'
import { useDispatch } from 'react-redux'
import { useState } from 'react'
import { authenticationApi } from '../../services/api/authenticationApi.ts'
import { localStorageService } from '../../services/LocalStorageService.ts'
import { showErrorMessage } from '../../components/snackbar/GlobalSnackbar.helpers.ts'

interface RegisterFromFields extends RegisterRequest {
  verifyPassword: string
}

export function RegisterPage() {

  const defaultValues: RegisterFromFields = {
    email: 'jan@email.com',
    password: '12345678',
    verifyPassword: '12345678',
    firstName: 'jan',
    lastName: 'jan'
  }

  const form = useForm<RegisterFromFields>({ defaultValues })

  const {
    register,
    handleSubmit
  } = form

  const { errors } = form.formState

  const navigate = useNavigate()

  const dispatch = useDispatch<AppDispatch>()

  const [
    loading,
    setLoading
  ] = useState<boolean>(false)

  async function onSubmit(data: RegisterRequest): Promise<void> {
    setLoading(true)
    try {
      const response = await authenticationApi.register(data)
      localStorageService.setAccessToken(response.data.token)
      localStorageService.setRefreshToken(response.data.refreshToken)
      localStorageService.setUserId(response.data.user.id)
      navigate('/')
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
        <form noValidate onSubmit={handleSubmit(onSubmit)} className={styles.form}>

          <Typography variant="h4" component="h2" style={{ marginBottom: '1rem' }}>
            Register
          </Typography>

          <TextField
            autoFocus
            margin="dense"
            id="email"
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
                value: /^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,6}$/,
                message: 'Enter a valid email address'
              }
            })}
          />

          <TextField
            margin="dense"
            id="firstName"
            label="First name"
            placeholder="John"
            type="text"
            variant="standard"
            fullWidth
            error={!!errors.firstName}
            helperText={errors.firstName?.message}
            {...register('firstName', {
              required: 'First name is required',
              maxLength: { value: 32, message: 'First name maximum allowed length is 32' }
            })}
          />

          <TextField
            margin="dense"
            id="lastName"
            label="Last name"
            placeholder="Doe"
            type="text"
            variant="standard"
            fullWidth
            error={!!errors.lastName}
            helperText={errors.lastName?.message}
            {...register('lastName', {
              required: 'Last name is required',
              maxLength: { value: 32, message: 'Last name maximum allowed length is 32' }
            })}
          />

          <TextField
            margin="dense"
            id="password"
            type="password"
            variant="standard"
            label="password"
            fullWidth
            error={!!errors.password}
            helperText={errors.password?.message}
            {...register('password', {
              required: 'Password is required',
              minLength: { value: 8, message: 'Password minimum allowed length is 8' },
              maxLength: { value: 64, message: 'Password maximum allowed length is 64' }
            })}
          />

          <TextField
            margin="dense"
            id="verifyPassword"
            type="password"
            variant="standard"
            label="Verify Password"
            fullWidth
            error={!!errors.verifyPassword}
            helperText={errors.verifyPassword?.message}
            {...register('verifyPassword', {
              required: 'Password is required',
              minLength: { value: 8, message: 'Password minimum allowed length is 8' },
              maxLength: { value: 64, message: 'Password maximum allowed length is 64' },
              validate: value => value === form.getValues().password || 'Passwords do not match'
            })}
          />

          <LoadingButton
            loading={loading}
            type="submit"
            variant="contained"
            style={{ margin: '1.5rem 0' }}
            fullWidth
          >
            Register
          </LoadingButton>

          <div>Already have account? <a href="/login">Login</a></div>
        </form>
      </Card>
    </div>
  )
}