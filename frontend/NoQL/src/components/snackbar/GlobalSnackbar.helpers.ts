import { AppDispatch } from '../../state/store.ts'
import { showSnackbar } from '../../state/snackbarSlice.ts'
import { SnackbarType } from './GlobalSnackbar.types.ts'

function showErrorWithMessageAndError(dispatch: AppDispatch, message: string, error: unknown): void {
  if (!message) {
    message = 'Something went wrong'
  }

  dispatch(showSnackbar({
    message: `${message}: ${(error as Error).message || 'Unknown error'}`,
    type: SnackbarType.ERROR,
    duration: 5000,
    persist: false
  }))
}

function showError(dispatch: AppDispatch, error: unknown): void {
  showErrorWithMessageAndError(dispatch, '', error)
}

function showErrorMessage(dispatch: AppDispatch, message: string): void {
  dispatch(showSnackbar({
    message: message,
    type: SnackbarType.ERROR,
    duration: 5000,
    persist: false
  }))
}


export {
  showErrorWithMessageAndError,
  showError,
  showErrorMessage
}
