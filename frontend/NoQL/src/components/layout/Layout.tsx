import { Outlet } from 'react-router-dom'
import * as React from 'react'
import Box from '@mui/material/Box'
import CssBaseline from '@mui/material/CssBaseline'
import { CustomAppBar } from './CusomAppBar.tsx'
import { Main } from './Main.tsx'
import { PersistentDrawer } from './PersistentDrawer.tsx'

export function Layout() {
  const [
    navigationDrawerOpen,
    setNavigationDrawerOpen
  ] = React.useState<boolean>(false)

  const drawerWidth = 240

  function handleDrawerOpen(): void {
    setNavigationDrawerOpen(true)
  }

  function handleDrawerClose(): void {
    setNavigationDrawerOpen(false)
  }

  return (
    <Box sx={{ display: 'flex' }}>

      <CssBaseline />

      <CustomAppBar
        handleDrawerOpen={handleDrawerOpen}
        open={navigationDrawerOpen}
      />

      <PersistentDrawer
        open={navigationDrawerOpen}
        handleDrawerClose={handleDrawerClose}
      />

      <Main open={navigationDrawerOpen}>
        <Outlet />
      </Main>
    </Box>
  )
}
