import { useParams } from 'react-router'
import { useEffect, useState } from 'react'
import { Database } from '../../types/Database.ts'
import databaseApi, { QueryResponse } from '../../services/api/databaseApi.ts'

export function DatabasePage() {
  const { id } = useParams<string>()
  const [database, setDatabase] = useState<Database | null>(null)
  const [databaseLoading, setDatabaseLoading] = useState<boolean>(false)
  const [queryResult, setQueryResult] = useState<QueryResponse | null>(null)
  const [queryLoading, setQueryLoading] = useState<boolean>(false)
  const usersQuery = 'select all users' // TODO: get from input

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

  // get query result
  async function queryDatabase() {
    setQueryLoading(true)
    try {
      const response = await databaseApi.queryNaturalLanguage(id || '', usersQuery)
      setQueryResult(response.data)
    } catch (error: unknown) {
      console.log(error) // TODO: handles
    } finally {
      setQueryLoading(false)
    }
  }

  const databaseData =
    <div>{
      database
        ? <div>url: {`${database.host}:${database.port}`}</div>
        : <div>database not found</div>
    }</div>

  const queryData =
    <div>
      {
        queryLoading
          ? <div>query loading</div>
          : <div>query: {queryResult?.query}</div>
      }
      <button onClick={queryDatabase}>Query db</button>
    </div>

  return (
    <>
      <h1>Database: {id}</h1>
      {databaseLoading
        ? <div>loading...</div>
        : databaseData
      }
      <h2>Query data</h2>
      {queryData}
    </>
  )
}