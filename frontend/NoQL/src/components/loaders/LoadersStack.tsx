import { ReactElement } from 'react'
import { RectangularLoader } from './RectangularLoader.tsx'
import styles from './Loaders.module.css'

interface LoadersStackProps {
  count?: number // number of loaders to render
  height?: number // height of each loader in px
}

export function LoadersStack({ count = 3, height = 100 }: LoadersStackProps) {
  return (
    <div>
      {[...Array(count)].map((_, index: number): ReactElement => {
        return (
          <RectangularLoader
            key={index}
            className={styles.skeletonCard}
            height={height}
          />
        )
      })}
    </div>
  )
}