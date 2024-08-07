import { Database } from '../../types/Database.ts'
import { Button, Card, CardActions, CardContent, Typography } from '@mui/material'
import Chip from '@mui/material-next/Chip'
import React, { useState } from 'react'
import styles from './Dashboard.module.css'
import DeleteRoundedIcon from '@mui/icons-material/DeleteRounded'
import IconButton from '@mui/material/IconButton'
import { ConfirmDialog } from '../../components/ConfirmDialog.tsx'

interface DatabaseCardProps {
  database: Database,
  style?: React.CSSProperties,
  className?: string,
  deleteDatabase: (databaseId: string) => void,
  deleteDatabaseLoading: boolean,
}

export function DatabaseCard(
  {
    database,
    style,
    className,
    deleteDatabase,
    deleteDatabaseLoading
  }: DatabaseCardProps) {

  const [
    dialogOpen,
    setDialogOpen
  ] = useState<boolean>(false)

  return (
    <Card elevation={3} style={style} className={className}>
      <CardContent>
        <div className={styles.databaseCardHeader}>

          <span className={styles.databaseCardHeadingAndTag}>
            <Typography variant="h6" component="h3">{database.name}</Typography>
            <Chip label={database.engine} color="error" />
          </span>

          <IconButton aria-label="delete" onClick={() => setDialogOpen(true)}>
            <DeleteRoundedIcon />
          </IconButton>

        </div>
        <Typography>{database.host}:{database.port}/{database.database}</Typography>
      </CardContent>

      <CardActions>
        <Button href={`database/${database.id}`}>Query</Button>
      </CardActions>

      <ConfirmDialog
        title={`Delete database "${database.name}"?`}
        open={dialogOpen}
        setOpen={setDialogOpen}
        confirm={() => deleteDatabase(database.id)}
        deleteButtonLoading={deleteDatabaseLoading}
      />
    </Card>
  )
}