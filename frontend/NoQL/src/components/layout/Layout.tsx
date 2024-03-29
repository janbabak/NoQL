import { Outlet } from 'react-router-dom'
import * as React from 'react';
import Box from '@mui/material/Box';
import CssBaseline from '@mui/material/CssBaseline';
import { CustomAppBar } from './CusomAppBar.tsx'
import { Main } from './Main.tsx'
import { PersistentDrawer } from './PersistentDrawer.tsx'

export function Layout() {
  const [
    open,
    setOpen
  ] = React.useState(false);

  const handleDrawerOpen = () => {
    setOpen(true);
  };

  return (
    <Box sx={{ display: 'flex' }}>

      <CssBaseline />

      <CustomAppBar handleDrawerOpen={handleDrawerOpen} open={open} />

      <PersistentDrawer open={open} setOpen={setOpen}/>

      <Main open={open}>
        <Outlet />
      </Main>
    </Box>
  );
}
