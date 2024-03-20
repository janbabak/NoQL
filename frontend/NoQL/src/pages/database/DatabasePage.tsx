import { useParams } from 'react-router'
import { useEffect, useRef, useState } from 'react'
import { Database } from '../../types/Database.ts'
import databaseApi, { QueryResponse } from '../../services/api/databaseApi.ts'
import { TextField, Typography } from '@mui/material'
import { LoadingButton } from '@mui/lab'
import styles from './Database.module.css'
import { ResultTable } from './ResultTable.tsx'
import { GeneratedQuery } from './GeneratedQuery.tsx'

export function DatabasePage() {
  const { id } = useParams<string>()

  const [
    database,
    setDatabase
  ] = useState<Database | null>(null)

  const [
    databaseLoading,
    setDatabaseLoading
  ] = useState<boolean>(false)

  const [
    queryResult,
    setQueryResult
  ] = useState<QueryResponse | null>(null)

  const [
    totalCount,
    setTotalCount
  ] = useState<number>(0)

  const [
    queryLoading,
    setQueryLoading
  ] = useState<boolean>(false)

  /* eslint-disable  @typescript-eslint/no-explicit-any */
  const usersQuery = useRef<any>('')

  useEffect(() => {
    void loadDatabase()
    // eslint-disable-next-line react-hooks/exhaustive-deps
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
      const response =
        await databaseApi.queryNaturalLanguage(id || '', usersQuery.current.value)
      setQueryResult(response.data)
      setTotalCount(response.data.totalCount)
    } catch (error: unknown) {
      console.log(error) // TODO: handles
    } finally {
      setQueryLoading(false)
    }
  }

  // get next page
  async function onPageChange(page: number, pageSize: number) {
    console.log('new page is: ' + page) // TODO: remove

    setQueryLoading(true)
    try {
      const response = await databaseApi.queryQueryLanguageQuery(
        id || '',
        queryResult?.query || '',
        page,
        pageSize,
        true)

      setQueryResult(response.data)
    } catch (error: unknown) {
      console.log(error) // TODO: handles
    } finally {
      setQueryLoading(false)
    }
  }

  const PageContent =
    <>
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
        onClick={queryDatabase}
        className={styles.queryButton}
      >Query</LoadingButton>

      {queryResult != null &&
        <div>
          <GeneratedQuery query={queryResult.query} />
          <ResultTable
            queryResult={queryResult}
            totalCount={totalCount}
            onPageChange={onPageChange}
          />
        </div>
      }
    </>

  return (
    <>
      <Typography variant="h2" component="h1">Query</Typography>
      {!databaseLoading && PageContent}
    </>
  )
}