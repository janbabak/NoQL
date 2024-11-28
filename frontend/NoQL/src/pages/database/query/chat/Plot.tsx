import { Paper } from '@mui/material'

interface PlotProps {
  plotUrl: string
}

export function Plot({ plotUrl }: PlotProps) {
  return (
    <Paper elevation={2} style={{ marginBottom: '2rem', display: 'flex', justifyContent: 'center' }}>
      <img src={import.meta.env.VITE_BACKEND_URL + plotUrl} alt="plot" />
    </Paper>
  )
}