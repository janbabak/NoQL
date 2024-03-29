import { styled } from '@mui/material/styles';
import { ReactNode } from 'react'

const StyledDrawerHeader = styled('div')(({ theme }) => ({
  display: 'flex',
  alignItems: 'center',
  padding: theme.spacing(0, 1),
  // necessary for content to be below app bar
  ...theme.mixins.toolbar,
  justifyContent: 'flex-end',
}));

export function DrawerHeader({ children }: {
  children?: ReactNode,
}) {
  return <StyledDrawerHeader>{children}</StyledDrawerHeader>;
}