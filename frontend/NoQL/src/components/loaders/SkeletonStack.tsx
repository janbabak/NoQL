import React, { ReactElement } from 'react'
import { RectangleSkeleton } from './RectangleSkeleton.tsx'
import styles from './Loaders.module.css'

interface LoadersStackProps {
  count?: number // number of loaders to render
  height?: number // height of each loader in px
  style?: React.CSSProperties
}

export function SkeletonStack({ count = 3, height = 100, style }: LoadersStackProps) {
  return (
    <div style={style}>
      {[...Array(count)].map((_, index: number): ReactElement => {
        return (
          <RectangleSkeleton
            key={index}
            className={styles.skeletonCard}
            height={height}
          />
        )
      })}
    </div>
  )
}