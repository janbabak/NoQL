import { SecondaryNavbarItem, SecondaryNavbarProps } from './SecondaryNavbar.types.ts'
import { useState } from 'react'
import { Button } from '@mui/material'
import styles from './SecondaryNavbar.module.css'

export function SecondaryNavbar({ subpages }: SecondaryNavbarProps) {
  const [
    activePageIndex,
    setActivePageIndex
  ] = useState<number>(0)


  const NavigationButtons =
    <div className={styles.navigationButtonsContainer}>
      {
        subpages.map((subpage: SecondaryNavbarItem, index: number) => {
          return (
            <Button
              onClick={() => setActivePageIndex(index)}
              variant={activePageIndex == index ? "contained" : "outlined"}
              startIcon={subpage.buttonIcon}
              size="small"
              key={index}
            >
              {subpage.label}
            </Button>
          )
        })
      }
    </div>

  return (
    <>
      { NavigationButtons }

      {
        subpages.map((subpage: SecondaryNavbarItem, index: number) => {
          return (
            <div hidden={index != activePageIndex} key={index}>
              {subpage.component}
            </div>
          )
        })
      }
    </>
  )
}