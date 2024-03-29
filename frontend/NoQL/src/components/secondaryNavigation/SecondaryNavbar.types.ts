import { ReactNode } from 'react'

interface SecondaryNavbarProps {
  subpages: SecondaryNavbarItem[]
}

interface SecondaryNavbarItem {
  label: string,
  component: ReactNode,
  buttonIcon?: ReactNode,
}

export type { SecondaryNavbarProps, SecondaryNavbarItem }