import { styled, Theme, useTheme } from '@mui/material/styles'
import MuiAppBar from '@mui/material/AppBar'
import Toolbar from '@mui/material/Toolbar'
import IconButton from '@mui/material/IconButton'
import MenuIcon from '@mui/icons-material/Menu'
import Typography from '@mui/material/Typography'
import React, { SetStateAction } from 'react'
import { localStorageService } from '../../services/LocalStorageService.ts'
import { useLocation, useNavigate } from 'react-router-dom'
import { Button } from '@mui/material'
import LogoutIcon from '@mui/icons-material/Logout'

interface CustomAppBarProps {
  drawerOpen: boolean,
  setDrawerOpen: React.Dispatch<SetStateAction<boolean>>,
}

export function CustomAppBar(
  {
    drawerOpen,
    setDrawerOpen
  }: CustomAppBarProps) {

  interface StyledAppBarProps {
    theme: Theme,
    open: boolean,
  }

  const location = useLocation()
  const navigate = useNavigate()

  const theme: Theme = useTheme()

  function logOut(): void {
    localStorageService.clearUserId()
    localStorageService.clearAccessToken()
    localStorageService.clearRefreshToken()
    navigate('/login')
  }

  const StyledAppBar =
    styled(MuiAppBar, {
      shouldForwardProp: (prop: PropertyKey): boolean => prop !== 'open'
    })(({ theme, open }: StyledAppBarProps) => ({
      transition: theme.transitions.create(['margin', 'width'], {
        easing: theme.transitions.easing.sharp,
        duration: theme.transitions.duration.leavingScreen
      }),
      ...(open && {
        width: '100%',
        transition: theme.transitions.create(['margin', 'width'], {
          easing: theme.transitions.easing.easeOut,
          duration: theme.transitions.duration.enteringScreen
        })
      })
    }))

  const OpenDrawerButton =
    <IconButton
      color="inherit"
      aria-label="open drawer"
      onClick={() => setDrawerOpen(true)}
      edge="start"
      sx={{ mr: 2, ...(drawerOpen && { display: 'none' }) }}
    >
      <MenuIcon />
    </IconButton>

  return (
    <StyledAppBar position="fixed" open={drawerOpen} theme={theme}>
      <Toolbar style={{ display: 'flex', justifyContent: 'space-between' }}>
        <div style={{ display: 'flex', alignItems: 'center' }}>
          {OpenDrawerButton}
          <Typography variant="h6" noWrap component="div">
            NoQL
          </Typography>
        </div>

        {location.pathname !== '/login' && location.pathname !== '/register' &&
          <Button
            onClick={logOut}
            startIcon={<LogoutIcon />}
            sx={{ color: 'white' }}
          >
            logout
          </Button>}
      </Toolbar>
    </StyledAppBar>
  )
}
