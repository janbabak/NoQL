import { Outlet } from 'react-router-dom'
import * as React from 'react'
import Box from '@mui/material/Box'
import CssBaseline from '@mui/material/CssBaseline'
import { ApplicationBar } from '../navigationDrawer/CusomAppBar.tsx'
import { Main } from './Main.tsx'
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

      <Main>
        <Outlet />
      </Main>

      <GlobalSnackbar />
    </Box>
  )
}
