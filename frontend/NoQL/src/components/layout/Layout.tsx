import { Outlet } from 'react-router-dom'
import Box from '@mui/material/Box'
import { CustomAppBar } from '../navigationDrawer/CustomAppBar.tsx'
import { GlobalSnackbar } from '../snackbar/GlobalSnackbar.tsx'
import { NavigationDrawer } from '../navigationDrawer/NavigationDrawer.tsx'
import { useState } from 'react'
import styles from './Layout.module.css';
import DashboardIcon from '@mui/icons-material/Dashboard';
import PsychologyIcon from '@mui/icons-material/Psychology';

export function Layout() {
  const [
    drawerOpen,
    setDrawerOpen
  ] = useState<boolean>(false)

  const navigationDrawerLinks = [
    {
      text: 'Dashboard',
      icon: <DashboardIcon />,
      href: '/'
    },{
      text: 'Custom models',
      icon: <PsychologyIcon />,
      href: '/customModels'
    },
  ]

  return (
    <Box className={styles.layout}>

      <CustomAppBar
        drawerOpen={drawerOpen}
        setDrawerOpen={setDrawerOpen}
      />

      <NavigationDrawer
        drawerOpen={drawerOpen}
        setDrawerOpen={setDrawerOpen}
        links={navigationDrawerLinks}
      />

      <main className={styles.main}>
        <div style={{width: '100%'}}>
          <Outlet />
        </div>
      </main>

      <GlobalSnackbar />
    </Box>
  )
}
