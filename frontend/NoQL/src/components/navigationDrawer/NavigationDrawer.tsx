import { Box, SwipeableDrawer } from '@mui/material'
import List from '@mui/material/List'
import ListItem from '@mui/material/ListItem'
import ListItemButton from '@mui/material/ListItemButton'
import ListItemIcon from '@mui/material/ListItemIcon'
import ListItemText from '@mui/material/ListItemText'
import React, { Dispatch, SetStateAction } from 'react'
import { NavigationDrawerLink } from './navigationDrawer.types.ts'
import { Link } from 'react-router-dom'

interface DrawerProps {
  drawerOpen: boolean,
  setDrawerOpen: Dispatch<SetStateAction<boolean>>
  links: NavigationDrawerLink[]
}

export function NavigationDrawer({ drawerOpen, setDrawerOpen, links }: DrawerProps) {

  const toggleDrawer = (open: boolean) =>
    (event: React.KeyboardEvent | React.MouseEvent): void => {
      if (event
        && event.type === 'keydown'
        && ((event as React.KeyboardEvent).key === 'Tab' || (event as React.KeyboardEvent).key === 'Shift')
      ) {
        return
      }

      setDrawerOpen(open)
    }

  const list =
    <Box
      sx={{ width: 250 }}
      role="presentation"
      onClick={toggleDrawer(false)}
      onKeyDown={toggleDrawer(false)}
    >
      <List>
        {links.map((link, index) => (
          <ListItem key={index} disablePadding>
            <ListItemButton component={Link} to={link.href}>
              <ListItemIcon>
                {link.icon}
              </ListItemIcon>
              <ListItemText primary={link.text} />
            </ListItemButton>
          </ListItem>
        ))}
      </List>
    </Box>

  return (
    <SwipeableDrawer
      open={drawerOpen}
      onClose={toggleDrawer(false)}
      onOpen={toggleDrawer(true)}
    >
      {list}
    </SwipeableDrawer>
  )
}