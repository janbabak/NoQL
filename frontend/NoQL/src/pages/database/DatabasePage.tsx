import { useParams } from 'react-router'
import { useEffect, useRef, useState } from 'react'
import { Database } from '../../types/Database.ts'
import databaseApi, { QueryResponse } from '../../services/api/databaseApi.ts'
import { Typography } from '@mui/material'
import { LoadingButton } from '@mui/lab'
import styles from './Database.module.css'
import { QueryInputTabs } from './QueryInputTabs.tsx'
import { NATURAL_LANGUAGE_TAB } from './Constants.ts'
import { Result } from './Result.tsx'

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
    queryLoading,
    setQueryLoading
  ] = useState<boolean>(false)

  const [
    showGeneratedQuery,
    setShowGeneratedQuery
  ] = useState<boolean>(true)

  const [
    tab,
    setTab
  ] = useState<number>(NATURAL_LANGUAGE_TAB)

  const [
    queryLanguageQuery,
    setQueryLanguageQuery
  ] = useState<string>('')

  const [
    page,
    setPage
  ] = useState<number>(0)

  const [
    pageSize,
    setPageSize
  ] = useState<number>(10)

  const [
    totalCount,
    setTotalCount
  ] = useState<number | null>(0)

  const naturalLanguageQuery = useRef<string>('')

  useEffect((): void => {
    void loadDatabase()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  // load database from API
  async function loadDatabase(): Promise<void> {
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
  async function queryDatabase(): Promise<void> {
    setPage(0)
    setQueryLoading(true)
    try {
      const naturalLanguage = tab == 0
      const response = naturalLanguage
        ? await databaseApi.queryNaturalLanguage(
          // eslint-disable-next-line @typescript-eslint/ban-ts-comment
          // @ts-ignore
          id || '', naturalLanguageQuery.current.value, pageSize)
        : await databaseApi.queryQueryLanguageQuery(
          id || '', queryLanguageQuery, 0, pageSize)
      setQueryResult(response.data)
      setTotalCount(response.data.totalCount)
    } catch (error: unknown) {
      console.log(error) // TODO: handles
    } finally {
      setQueryLoading(false)
      setShowGeneratedQuery(true)
    }
  }

  // get next page
  async function onPageChange(page: number, pageSize: number): Promise<void> {
    setPageSize(pageSize)
    setPage(page)

    setQueryLoading(true)
    try {
      const response = await databaseApi.queryQueryLanguageQuery(
        id || '',
        queryResult?.query || '',
        page,
        pageSize)

      setQueryResult(response.data)
    } catch (error: unknown) {
      console.log(error) // TODO: handles
    } finally {
      setQueryLoading(false)
    }
  }

  function editQueryInEditor(query: string) {
    setQueryLanguageQuery(query)
    setTab(1)
    setShowGeneratedQuery(false)
  }

  return (
    <>
      <Typography variant="h2" component="h1">Query</Typography>

      {!databaseLoading &&
        <>
          <Typography variant="h4" component="h2">{database?.name}</Typography>

          <QueryInputTabs
            tab={tab}
            setTab={setTab}
            naturalLanguageQuery={naturalLanguageQuery}
            queryLanguageQuery={queryLanguageQuery}
            setQueryLanguageQuery={setQueryLanguageQuery}
          />

          <LoadingButton
            loading={queryLoading}
            fullWidth
            variant="contained"
            onClick={queryDatabase}
            className={styles.queryButton}
          >Query</LoadingButton>

          <Result
            queryResponse={queryResult}
            showGeneratedQuery={showGeneratedQuery}
            editQueryInEditor={editQueryInEditor}
            page={page}
            pageSize={pageSize}
            setPageSize={setPageSize}
            totalCount={totalCount}
            onPageChange={onPageChange}
          />
        </>
      }
    </>
  )
}