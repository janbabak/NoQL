import { TransitionProps } from '@mui/material/transitions'
import { Button, Dialog, DialogActions, DialogContent, DialogContentText, DialogTitle, Slide } from '@mui/material'
import React from 'react'

interface ConfirmDialogProps {
  title: string,
  content: string | undefined,
  open: boolean,
  setOpen: React.Dispatch<React.SetStateAction<boolean>>,
  confirm: () => void,
}

const Transition = React.forwardRef(function Transition(
  props: TransitionProps & {
    children: React.ReactElement<any, any>;
  },
  ref: React.Ref<unknown>
) {
  return <Slide direction="up" ref={ref} {...props} />
})

export function ConfirmDialog(
  {
    title,
    content,
    open,
    setOpen,
    confirm,
  }: ConfirmDialogProps
) {

  const handleClose = (): void => {
    setOpen(false)
  }

  return (
    <Dialog
      open={open}
      TransitionComponent={Transition}
      keepMounted
      onClose={handleClose}
      aria-describedby="alert-dialog-slide-description"
    >
      <DialogTitle>{title}</DialogTitle>

      <DialogContent>
        {content && <DialogContentText>{content}</DialogContentText>}
      </DialogContent>

      <DialogActions>
        <Button onClick={handleClose}>Cancel</Button>
        <Button onClick={(): void => {
          confirm()
          handleClose()
        }}>Delete</Button>
      </DialogActions>
    </Dialog>
  )
}