import { styled } from '@mui/material/styles'
import { DrawerHeader } from './DrawerHeader.tsx'
import { ReactNode } from 'react'

const drawerWidth = 240

const StyledMain = styled('main', { shouldForwardProp: (prop) => prop !== 'open' })<{
  open?: boolean;
}>(({ theme, open }) => ({
  flexGrow: 1,
  padding: theme.spacing(3),
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

export function Main({ children, open }: {
  children: ReactNode,
  open: boolean
}) {

  return (
    <StyledMain open={open}>
      <DrawerHeader />
      {children}
    </StyledMain>
  )
}