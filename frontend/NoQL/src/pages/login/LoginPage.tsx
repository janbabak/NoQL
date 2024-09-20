import { Card, TextField, Typography } from '@mui/material'
import { LoadingButton } from '@mui/lab'
import styles from './Login.module.css'

export function LoginPage() {
  return (
    <div className={styles.page}>
      <Card className={styles.card}>
        <form noValidate onSubmit={null} className={styles.form}>

          <Typography variant="h4" component="h2" style={{ marginBottom: '1rem' }}>
            Login
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
            id="password"
            type="password"
            variant="standard"
            label="Password"
            fullWidth
          />

          <LoadingButton
            loading={false}
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