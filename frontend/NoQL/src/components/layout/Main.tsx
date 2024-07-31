import { ReactNode } from 'react'

interface MainProps {
  children: ReactNode,
}

export function Main({children }: MainProps) {
  return (
    <main style={{
      border: '3px solid red',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      width: '100%',
      margin: '5rem auto 1rem auto',
    }}>
      <div>
        {children}
      </div>
    </main>
  )
}