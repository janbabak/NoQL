import styles from './Login.module.css'
import { Card, TextField, Typography } from '@mui/material'
import { LoadingButton } from '@mui/lab'

export function RegisterPage() {
  return (
    <div className={styles.page}>
      <Card className={styles.card}>
        <form noValidate onSubmit={null} className={styles.form}>

          <Typography variant="h4" component="h2" style={{ marginBottom: '1rem' }}>
            Register
          </Typography>

          <TextField
            autoFocus
            margin="dense"
            id="username"
            placeholder="john.doe@email.com"
            type="email"
            variant="standard"
            fullWidth
            label="Username"
          />

          <TextField
            margin="dense"
            id="firstName"
            placeholder="John"
            type="text"
            variant="standard"
            fullWidth
            label="First name"
          />

          <TextField
            margin="dense"
            id="lastName"
            placeholder="Doe"
            type="text"
            variant="standard"
            fullWidth
            label="Last name"
          />

          <TextField
            margin="dense"
            id="password"
            type="password"
            variant="standard"
            label="Password"
            fullWidth
          />

          <TextField
            margin="dense"
            id="password"
            type="password"
            variant="standard"
            label="Verify password"
            fullWidth
          />

          <LoadingButton
            loading={false}
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