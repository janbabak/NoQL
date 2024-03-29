import { styled, Theme } from '@mui/material/styles'
import { ReactNode } from 'react'

interface DrawerHeaderProps {
  children?: ReactNode,
  theme: Theme,
}

export function DrawerHeader({ children, theme }: DrawerHeaderProps) {
  interface StyledDrawerHeaderProps {
    theme: Theme,
  }

  const StyledDrawerHeader =
    styled('div')(({ theme }: StyledDrawerHeaderProps) => ({
      display: 'flex',
      alignItems: 'center',
      padding: theme.spacing(0, 1),
      // necessary for content to be below app bar
      ...theme.mixins.toolbar,
      justifyContent: 'flex-end'
    }))

  return <StyledDrawerHeader theme={theme}>{children}</StyledDrawerHeader>
}