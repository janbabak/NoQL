import { Snackbar } from '@mui/material'
import { useDispatch, useSelector } from 'react-redux'
import { AppDispatch, RootState } from '../../state/store.ts'
import { SnackbarConfig } from './GlobalSnackbar.types.ts'
import { closeSnackbar, DEFAULT_ANCHOR, DEFAULT_DURATION } from '../../state/snackbarSlice.ts'
import { ReactElement, useMemo } from 'react'

export function GlobalSnackbar(): ReactElement {

  const open: boolean = useSelector((state: RootState) => {
    return state.snackbarReducer.open
  })

  const config: SnackbarConfig = useSelector((state: RootState) => {
    return state.snackbarReducer.config
  })

  const dispatch: AppDispatch = useDispatch()

  function handleClose(): void {
    dispatch(closeSnackbar())
  }

  const autoHideDuration: number | null = useMemo((): number | null => {
    if (config.persist) {
      return null
    }
    if (config.duration) {
      return config.duration
    }
    return DEFAULT_DURATION
  }, [config.persist, config.duration])

  return (
    <Snackbar
      open={open}
      message={config.message}
      autoHideDuration={autoHideDuration}
      anchorOrigin={config.anchor || DEFAULT_ANCHOR}
      onClose={handleClose}
    />
  )
}