import { Database } from '../../types/Database.ts'
import { Button, Card, CardActions, CardContent, Typography } from '@mui/material'
import Chip from '@mui/material-next/Chip'
import React from 'react'
import styles from './dashboard.module.css'

export function DatabaseCard({ database, style, className }: {
  database: Database,
  style?: React.CSSProperties,
  className?: string
}) {
  return (
    <Card elevation={3} style={style} className={className}>
      <CardContent>
        <div className={styles.databaseCardFirstRow}>
          <Typography variant="h6" component="h3">{database.name}</Typography>
          <Chip label={database.engine} color="error" />
        </div>
        <Typography>{database.host}:{database.port}/{database.database}</Typography>
      </CardContent>

      <CardActions>
        <Button href={`database/${database.id}`}>Query</Button>
      </CardActions>
    </Card>
  )
}