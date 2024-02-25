import { useEffect, useState } from 'react'
import { Database } from '../../types/Database.ts'
import { DatabaseCard } from './DatabaseCard.tsx'
import databaseApi from '../../services/api/databaseApi.ts'

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

  return (
    <>
      <h2>Databases</h2>
      {
        databasesLoading
          ? <div>loading ...</div>
          : <ul>{databases.map((db: Database) => <DatabaseCard database={db} key={db.id} />)}</ul>
      }
    </>
  )
}