import { styled, Theme } from '@mui/material/styles'
import { DrawerHeader } from '../navigationDrawer/DrawerHeader.tsx'
import { ReactNode } from 'react'

interface MainProps {
  open: boolean,
  drawerWidth: number,
  theme: Theme,
  children: ReactNode,
}

export function Main({ open, drawerWidth, theme, children }: MainProps) {

  interface StyledMainProps {
    theme: Theme,
    open: boolean,
  }

  const StyledMain = styled('main', {
      shouldForwardProp: (prop: PropertyKey): boolean => prop !== 'open'
    }
  )(({ theme, open }: StyledMainProps) => ({
    flexGrow: 1,
    paddingTop: theme.spacing(3),
    paddingLeft: theme.spacing(20),
    paddingRight: theme.spacing(20),
    transition: theme.transitions.create('margin', {
      easing: theme.transitions.easing.sharp,
      duration: theme.transitions.duration.leavingScreen
    }),
    marginLeft: `-${drawerWidth}px`,
    ...(open && {
      transition: theme.transitions.create('margin', {
        easing: theme.transitions.easing.easeOut,
        duration: theme.transitions.duration.enteringScreen
      }),
      marginLeft: 0
    })
  }))

  return (
    <StyledMain open={open} theme={theme}>
      <DrawerHeader theme={theme} />
      {children}
    </StyledMain>
  )
}