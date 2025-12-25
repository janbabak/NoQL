import { Paper } from '@mui/material'
import styles from './Chat.module.css'
import { ENV } from '@/env'

interface PlotProps {
  plotUrl: string
}

/**
 * Image of generated plot.
 */
export function Plot({ plotUrl }: PlotProps) {
  return (
    <Paper elevation={2} className={styles.plot}>
      <img src={ENV.BACKEND_URL+ plotUrl} alt="plot" />
    </Paper>
  )
}