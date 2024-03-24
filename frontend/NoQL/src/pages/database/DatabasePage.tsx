import { useParams } from 'react-router'
import { useEffect, useRef, useState } from 'react'
import { Database } from '../../types/Database.ts'
import databaseApi, { QueryResponse } from '../../services/api/databaseApi.ts'
import { Box, Tab, Tabs, TextField, Typography } from '@mui/material'
import { LoadingButton } from '@mui/lab'
import styles from './Database.module.css'
import { QueryEditor } from './QueryEditor.tsx'
import { QueryResultComponent } from './QueryResultComponent.tsx'

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

  /* eslint-disable  @typescript-eslint/no-explicit-any */
  const naturalLanguageQuery = useRef<any>(null)

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
    setPage(0)
    setQueryLoading(true)
    try {
      const naturalLanguage = tab == 0
      const response = naturalLanguage
        ? await databaseApi.queryNaturalLanguage(
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

  // pagination-------------------------------------------------------------------------------------

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

  // get next page
  async function onPageChange(page: number, pageSize: number) {
    console.log('new page is: ' + page) // TODO: remove
    setPageSize(pageSize)
    setPage(page)

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

  async function onPageSizeChange(newPageSize: number) {
    setPageSize(0)
    onPageChange(0, newPageSize)
  }

  // tabs-------------------------------------------------------------------------------------------

  const [
    tab,
    setTab
  ] = useState<number>(0)

  const [
    queryLanguageQuery,
    setQueryLanguageQuery
  ] = useState<string>('')

  // const queryLanguageQuery = useRef<string>('')

  function handleTabChange(_event: React.SyntheticEvent, newValue: number) {
    setTab(newValue)
  }

  function editQueryInEditor(query: string) {
    setQueryLanguageQuery(query)
    setTab(1)
    setShowGeneratedQuery(false)
  }

  const PageContent =
    <>
      <Typography variant="h4" component="h2">{database?.name}</Typography>

      <Box sx={{ borderBottom: 1, borderColor: 'divider' }}>
        <Tabs
          value={tab}
          aria-label="basic tabs example"
          onChange={handleTabChange}
          sx={{ borderRadius: '0.25rem', marginTop: '1.5rem' }}
        >
          <Tab label="Natural language" sx={{ borderRadius: '0.25rem' }} />
          <Tab label="Query language" sx={{ borderRadius: '0.25rem' }} />
        </Tabs>
      </Box>

      <div className={styles.queryInput} role="tabpanel" hidden={tab != 0}>
        <TextField
          id="query"
          label="Query"
          variant="outlined"
          inputRef={naturalLanguageQuery}
          fullWidth
        />
      </div>

      <div role="tabpanel" hidden={tab != 1} className={styles.queryEditor}>
        <QueryEditor value={queryLanguageQuery} setValue={setQueryLanguageQuery} />
      </div>

      <LoadingButton
        loading={queryLoading}
        fullWidth
        variant="contained"
        onClick={queryDatabase}
        className={styles.queryButton}
      >Query</LoadingButton>

      <QueryResultComponent
        queryResponse={queryResult}
        showGeneratedQuery={showGeneratedQuery}
        editQueryInEditor={editQueryInEditor}
        totalCount={totalCount}
        page={page}
        pageSize={pageSize}
        onPageChange={onPageChange}
        onPageSizeChange={onPageSizeChange}
      />
    </>

  return (
    <>
      <Typography variant="h2" component="h1">Query</Typography>
      {!databaseLoading && PageContent}
    </>
  )
}