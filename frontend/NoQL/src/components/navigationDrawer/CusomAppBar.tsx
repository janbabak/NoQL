import { styled, Theme, useTheme } from '@mui/material/styles'
import MuiAppBar from '@mui/material/AppBar'
import Toolbar from '@mui/material/Toolbar'
import IconButton from '@mui/material/IconButton'
import MenuIcon from '@mui/icons-material/Menu'
import Typography from '@mui/material/Typography'

interface CustomAppBarProps {
  open: boolean,
  handleDrawerOpen: () => void,
}

export function ApplicationBar(
  {
    open,
    handleDrawerOpen,
  }: CustomAppBarProps) {

  interface StyledAppBarProps {
    theme: Theme,
    open: boolean,
  }

  const theme: Theme = useTheme()

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
      onClick={handleDrawerOpen}
      edge="start"
      sx={{ mr: 2, ...(open && { display: 'none' }) }}
    >
      <MenuIcon />
    </IconButton>

  return (
    <StyledAppBar position="fixed" open={open} theme={theme}>
      <Toolbar>
        {OpenDrawerButton}
        <Typography variant="h6" noWrap component="div">
          NoQL
        </Typography>
      </Toolbar>
    </StyledAppBar>
  )
}
