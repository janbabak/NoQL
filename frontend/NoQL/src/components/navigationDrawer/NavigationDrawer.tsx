import { Box, SwipeableDrawer } from '@mui/material'
import List from '@mui/material/List'
import ListItem from '@mui/material/ListItem'
import ListItemButton from '@mui/material/ListItemButton'
import ListItemIcon from '@mui/material/ListItemIcon'
import InboxIcon from '@mui/icons-material/MoveToInbox'
import MailIcon from '@mui/icons-material/Mail'
import ListItemText from '@mui/material/ListItemText'
import Divider from '@mui/material/Divider'
import React, { Dispatch, SetStateAction } from 'react'

interface DrawerProps {
  drawerOpen: boolean,
  setDrawerOpen: Dispatch<SetStateAction<boolean>>
}

export function NavigationDrawer({ drawerOpen, setDrawerOpen }: DrawerProps) {

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
        {['Dashboard', 'Starred', 'Send email', 'Drafts'].map((text, index) => (
          <ListItem key={text} disablePadding>
            <ListItemButton component="a" href="/">
              <ListItemIcon>
                {index % 2 === 0 ? <InboxIcon /> : <MailIcon />}
              </ListItemIcon>
              <ListItemText primary={text} />
            </ListItemButton>
          </ListItem>
        ))}
      </List>
      <Divider />

      {/*TODO: parameter*/}
      <List>
        {['All mail', 'Trash', 'Spam'].map((text, index) => (
          <ListItem key={text} disablePadding>
            <ListItemButton>
              <ListItemIcon>
                {index % 2 === 0 ? <InboxIcon /> : <MailIcon />}
              </ListItemIcon>
              <ListItemText primary={text} />
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