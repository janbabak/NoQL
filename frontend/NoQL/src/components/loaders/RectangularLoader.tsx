import { Paper, Skeleton } from '@mui/material'

interface RectangularLoaderProps {
  className?: string
  elevation?: number
  height?: number // height in px
}

export function RectangularLoader({ className, elevation = 3, height = 10 }: RectangularLoaderProps) {
  return (
    <Paper elevation={elevation}>
      <Skeleton
        className={className}
        animation="wave"
        variant="rectangular"
        height={height}
      />
    </Paper>
  )
}