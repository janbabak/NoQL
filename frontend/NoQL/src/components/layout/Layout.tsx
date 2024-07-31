import { Outlet } from 'react-router-dom'
import Box from '@mui/material/Box'
import { ApplicationBar } from '../navigationDrawer/CusomAppBar.tsx'
import { GlobalSnackbar } from '../snackbar/GlobalSnackbar.tsx'
import { Drawer } from '../navigationDrawer/Drawer.tsx'
import { useState } from 'react'
import styles from './Layout.module.css';

export function Layout() {
  const [
    drawerOpen,
    setDrawerOpen
  ] = useState<boolean>(false)

  return (
    <Box sx={{ display: 'flex' }}>

      <ApplicationBar
        drawerOpen={drawerOpen}
        setDrawerOpen={setDrawerOpen}
      />

      <Drawer
        drawerOpen={drawerOpen}
        setDrawerOpen={setDrawerOpen}
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
