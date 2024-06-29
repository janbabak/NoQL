import { Databases } from './Databases.tsx'
import { Button, Typography } from '@mui/material'
import { useDispatch } from 'react-redux'
import { AppDispatch } from '../../state/store.ts'
import { showSnackbar } from '../../state/snackbarSlice.ts'
import { SnackbarType } from '../../components/snackbar/GlobalSnackbar.types.ts'

export function DashboardPage() {

  const dispatch: AppDispatch = useDispatch()

  return (
    <>
      <Typography variant="h2" component="h1">Dashboard</Typography>
      <Databases />
      <Button
        variant="contained"
        onClick={() => dispatch(showSnackbar({ message: 'Hello', duration: 2000, type: SnackbarType.ERROR }))}
      >
        Show snack
      </Button>
    </>
  )
}