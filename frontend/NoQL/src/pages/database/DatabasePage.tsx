import { useParams } from 'react-router'
import { useEffect, useState } from 'react'
import { Database } from '../../types/Database.ts'
import databaseApi from '../../services/api/databaseApi.ts'

export function DatabasePage() {
  const { id } = useParams<string>()
  const [database, setDatabase] = useState<Database | null>(null)
  const [databaseLoading, setDatabaseLoading] = useState<boolean>(false)

  useEffect(() => {
    loadDatabase()
  }, [])

  // load database from API
  async function loadDatabase() {
    setDatabaseLoading(true)
    try {
      const response = await databaseApi.getById(id || '')
      setDatabase(response.data)
    } catch (error: unknown) {
      console.log(error) // TODO: handle
    } finally {
      setDatabaseLoading(false)
    }
  }

  const databaseData =
    <div>
      {
        database
          ? <div>url: {`${database.host}:${database.port}`}</div>
          : <div>database not found</div>
      }
    </div>

  return (
    <>
      <h1>Database: {id}</h1>
      {databaseLoading
        ? <div>loading...</div>
        : databaseData
      }
    </>
  )
}