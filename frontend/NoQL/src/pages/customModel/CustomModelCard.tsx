import { CustomModel } from '../../types/CustomModel.ts'
import { Card, CardActionArea, CardContent, Typography } from '@mui/material'
import React, { useState } from 'react'
import DeleteRoundedIcon from '@mui/icons-material/DeleteRounded'
import IconButton from '@mui/material/IconButton'
import styles from './CustomModel.module.css'
import { ConfirmDialog } from '../../components/ConfirmDialog.tsx'
import { Link } from 'react-router-dom'

interface CustomModelCardProps {
  customModel: CustomModel
  style?: React.CSSProperties,
  className?: string,
  deleteCustomModel: (customModelId: string) => void,
  deleteCustomModelLoading: boolean,
}

export function CustomModelCard(
  {
    customModel,
    style,
    className,
    deleteCustomModel,
    deleteCustomModelLoading
  }: CustomModelCardProps) {

  const [
    dialogOpen,
    setDialogOpen
  ] = useState<boolean>(false)

  return (
    <Card elevation={3} style={style} className={className}>
      <CardActionArea component={Link} to={customModel.id}>
        <CardContent>
          <div className={styles.customModelCardHeader}>
            <Typography variant="h6" component="h3">{customModel.name}</Typography>
            <IconButton aria-label={`delete database ${customModel.name}`} onClick={() => setDialogOpen(true)}>
              <DeleteRoundedIcon />
            </IconButton>
          </div>

          <Typography>{customModel.host}:{customModel.port}</Typography>

        </CardContent>
      </CardActionArea>

      <ConfirmDialog
        title={`Delete custom model "${customModel.name}"?`}
        open={dialogOpen}
        setOpen={setDialogOpen}
        confirm={() => deleteCustomModel(customModel.id)}
        deleteButtonLoading={deleteCustomModelLoading}
      />
    </Card>
  )
}