import { Outlet } from 'react-router-dom'
import styles from './Layout.module.css'

export function Layout() {
  return (
    <>
      <header className={styles.header}>header</header>
      <main className={styles.main}>
        <Outlet />
      </main>
      <footer className={styles.footer}>footer</footer>
    </>
  )
}