import { SnackbarOrigin } from '@mui/material-next/Snackbar'

enum SnackbarType {
  INFO = 'info',
  SUCCESS = 'success',
  WARNING = 'warning',
  ERROR = 'error',
}

interface SnackbarConfig {
  message: string
  type?: SnackbarType,
  duration?: number, // in milliseconds
  persist?: boolean // if true, the snackbar will not auto hide
  anchor?: SnackbarOrigin
}

export type { SnackbarConfig }
export { SnackbarType }