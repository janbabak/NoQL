import { Outlet } from 'react-router-dom'
import Box from '@mui/material/Box'
import CssBaseline from '@mui/material/CssBaseline'
import { ApplicationBar } from '../navigationDrawer/CusomAppBar.tsx'
import { GlobalSnackbar } from '../snackbar/GlobalSnackbar.tsx'
import { Drawer } from '../navigationDrawer/Drawer.tsx'
import { useState } from 'react'
import styles from './Layout.module.css';

export function Layout() {
  const [
    navigationDrawerOpen,
    setNavigationDrawerOpen
  ] = useState<boolean>(false)

  return (
    <Box sx={{ display: 'flex' }}>

      <CssBaseline />

      <ApplicationBar
        open={navigationDrawerOpen}
        setDrawerOpen={setNavigationDrawerOpen}
      />

      <Drawer
        drawerOpen={navigationDrawerOpen}
        setDrawerOpen={setNavigationDrawerOpen}
      />

      <main className={styles.main}>
        <div>
          <Outlet />
        </div>
      </main>

      <GlobalSnackbar />
    </Box>
  )
}
