import { createSlice, PayloadAction } from '@reduxjs/toolkit'
import { SnackbarConfig, SnackbarType } from '../components/snackbar/GlobalSnackbar.types.ts'
import { SnackbarOrigin } from '@mui/material-next/Snackbar'

interface SnackbarState {
  open: boolean,
  config: SnackbarConfig,
}

const DEFAULT_DURATION: number = 5000
const DEFAULT_ANCHOR: SnackbarOrigin = { vertical: 'bottom', horizontal: 'center' }
const DEFAULT_TYPE: SnackbarType = SnackbarType.INFO

const initialState: SnackbarState = {
  open: false,
  config: {
    message: 'default',
    type: SnackbarType.INFO,
    duration: DEFAULT_DURATION,
    persist: false,
    anchor: DEFAULT_ANCHOR,
  }
}

const snackbarSlice =
  createSlice({
    name: 'snackbar',
    initialState: initialState,
    reducers: {
      showSnackbar: (state: SnackbarState, action: PayloadAction<SnackbarConfig>): void => {
        state.open = true
        state.config = action.payload
      },
      closeSnackbar: (state: SnackbarState): void => {
        state.open = false
      }
    }
  })

export const {
  showSnackbar,
  closeSnackbar
} = snackbarSlice.actions

export {
  DEFAULT_ANCHOR,
  DEFAULT_DURATION,
  DEFAULT_TYPE,
}

export default snackbarSlice.reducer