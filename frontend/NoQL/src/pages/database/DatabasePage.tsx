import { useParams } from 'react-router'
import { useEffect, useRef, useState } from 'react'
import { Database } from '../../types/Database.ts'
import databaseApi, { QueryResponse } from '../../services/api/databaseApi.ts'
import { TextField, Typography } from '@mui/material'
import { LoadingButton } from '@mui/lab';
import styles from './Database.module.css'

export function DatabasePage() {
  const { id } = useParams<string>()
  const [database, setDatabase] = useState<Database | null>(null)
  const [databaseLoading, setDatabaseLoading] = useState<boolean>(false)
  const [queryResult, setQueryResult] = useState<QueryResponse | null>(null)
  const [queryLoading, setQueryLoading] = useState<boolean>(false)
  const usersQuery = useRef<string>('')

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
      const response = await databaseApi.queryNaturalLanguage(id || '', usersQuery.current.value)
      setQueryResult(response.data)
    } catch (error: unknown) {
      console.log(error) // TODO: handles
    } finally {
      setQueryLoading(false)
    }
  }

  return (
    <>
      <Typography variant="h2" component="h1">Query</Typography>
      <Typography variant="h4" component="h2">{database?.name}</Typography>

      <div className={styles.queryInput}>
        <TextField
          id="query"
          label="Query"
          variant="outlined"
          inputRef={usersQuery}
          fullWidth
        />
      </div>

      <LoadingButton
        loading={queryLoading}
        fullWidth
        variant="contained"
        onClick={queryDatabase}>Query</LoadingButton>

      <div style={{margin: '2rem 0'}}>
        {JSON.stringify(queryResult)}
      </div>
    </>
  )
}