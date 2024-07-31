import { Outlet } from 'react-router-dom'
import Box from '@mui/material/Box'
import CssBaseline from '@mui/material/CssBaseline'
import { ApplicationBar } from '../navigationDrawer/CusomAppBar.tsx'
import { Theme, useTheme } from '@mui/material/styles'
import { GlobalSnackbar } from '../snackbar/GlobalSnackbar.tsx'
import { Drawer } from '../navigationDrawer/Drawer.tsx'
import { useState } from 'react'

export function Layout() {
  const drawerWidth = 240
  const theme: Theme = useTheme()
  const [
    navigationDrawerOpen,
    setNavigationDrawerOpen
  ] = useState<boolean>(false)

  function handleDrawerOpen(): void {
    setNavigationDrawerOpen(true)
  }

  function handleDrawerClose(): void {
    setNavigationDrawerOpen(false)
  }

  return (
    <Box sx={{ display: 'flex' }}>

      <CssBaseline />

      <ApplicationBar
        open={navigationDrawerOpen}
        handleDrawerOpen={handleDrawerOpen}
        drawerWidth={drawerWidth}
        theme={theme}
      />

      <Drawer
        drawerOpen={navigationDrawerOpen}
        setDrawerOpen={setNavigationDrawerOpen}
      />

      <main style={{
        border: '3px solid red',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        width: '100%',
        margin: '5rem auto 1rem auto',
      }}>
        <div>
          <Outlet />
        </div>
      </main>

      <GlobalSnackbar />
    </Box>
  )
}
