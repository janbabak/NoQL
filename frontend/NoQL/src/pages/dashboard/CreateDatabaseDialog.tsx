import { Button, Dialog, DialogActions, DialogContent, DialogTitle, MenuItem, TextField } from '@mui/material'
import { Select } from '@mui/material-next'

interface CreateDatabaseDialogProps {
  open: boolean;
  onClose: () => void;
}

export function CreateDatabaseDialog({ open, onClose }: CreateDatabaseDialogProps): JSX.Element {
  return (
    <>
      <Dialog open={open} onClose={onClose}>
        <DialogTitle>Create Database</DialogTitle>

        <DialogContent>

          <TextField
            autoFocus
            margin="dense"
            id="name"
            label="Name"
            type="text"
            fullWidth
            variant="standard"
          />

          <div style={{ display: 'flex', gap: '1rem' }}>
            <TextField
              margin="dense"
              id="host"
              label="host"
              type="text"
              fullWidth
              variant="standard"
            />

            <TextField
              margin="dense"
              id="port"
              label="port"
              type="number"
              fullWidth
              variant="standard"
            />

            <TextField
              margin="dense"
              id="database"
              label="database"
              type="text"
              fullWidth
              variant="standard"
            />
          </div>

          <div style={{ display: 'flex', gap: '1rem' }}>
            <TextField
              margin="dense"
              id="username"
              label="username"
              type="text"
              fullWidth
              variant="standard"
            />

            <TextField
              margin="dense"
              id="password"
              label="password"
              type="password"
              fullWidth
              variant="standard"
            />

            <Select
              id="engine"
              label="engine"
              variant="standard"
            >
              <MenuItem value="mysql">Postgres</MenuItem>
              <MenuItem value="mongodb">MySql</MenuItem>
            </Select>
          </div>
        </DialogContent>

        <DialogActions>
          <Button onClick={onClose} color="primary">
            Cancel
          </Button>
          <Button onClick={onClose} color="primary">
            Create
          </Button>
        </DialogActions>
      </Dialog>
    </>
  )
}