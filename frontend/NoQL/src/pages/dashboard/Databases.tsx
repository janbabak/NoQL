import { useEffect, useState } from 'react'
import { Database } from '../../types/Database.ts'
import { DatabaseCard } from './DatabaseCard.tsx'
import databaseApi from '../../services/api/databaseApi.ts'
import { Typography } from '@mui/material'
import styles from './dashboard.module.css'

export function Databases() {
  const [databases, setDatabases] = useState<Database[]>([])
  const [databasesLoading, setDatabasesLoading] = useState<boolean>(false)

  useEffect(() => {
    loadDatabases()
  }, [])

  // Load list of databases from the backend
  async function loadDatabases() {
    setDatabasesLoading(true)
    try {
      const response = await databaseApi.getAll()
      setDatabases(response.data)
    } catch (error: unknown) {
      console.log(error) // TODO: handle, show user
    } finally {
      setDatabasesLoading(false)
    }
  }

  const Loading = <div>loading ...</div>

  const DatabasesList =
    <ul>{
      databases.map((db: Database) =>
        <DatabaseCard database={db} key={db.id} className={styles.databaseCard} />)
    }</ul>

  return (
    <>
      <Typography variant="h4" component="h2">Databases</Typography>
      {databasesLoading ? Loading : DatabasesList}
    </>
  )
}