import { useParams } from 'react-router'
import React, { useEffect, useRef, useState } from 'react'
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
    queryResponse,
    setQueryResponse
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

  const [
    tab,
    setTab
  ] = useState<number>(0)

  const [
    queryLanguageQuery,
    setQueryLanguageQuery
  ] = useState<string>('')

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

  // get query response
  async function queryDatabase(): Promise<void> {
    setPage(0)
    setQueryLoading(true)
    try {
      const naturalLanguage = tab == 0
      const response = naturalLanguage
        ? await databaseApi.queryNaturalLanguage(
          id || '', naturalLanguageQuery.current, pageSize)
        : await databaseApi.queryQueryLanguageQuery(
          id || '', queryLanguageQuery, 0, pageSize)
      setQueryResponse(response.data)
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
        queryResponse?.query || '',
        page,
        pageSize,
        true)

      setQueryResponse(response.data)
    } catch (error: unknown) {
      console.log(error) // TODO: handles
    } finally {
      setQueryLoading(false)
    }
  }

  function handleTabChange(_event: React.SyntheticEvent, newValue: number): void {
    setTab(newValue)
  }

  function editQueryInEditor(query: string) {
    setQueryLanguageQuery(query)
    setTab(1)
    setShowGeneratedQuery(false)
  }

  const NaturalLanguageTab =
    <div className={styles.queryInput} role="tabpanel" hidden={tab != 0}>
      <TextField
        id="query"
        label="Query"
        variant="outlined"
        inputRef={naturalLanguageQuery}
        fullWidth
      />
    </div>

  const QueryLanguageTab =
    <div role="tabpanel" hidden={tab != 1} className={styles.queryEditor}>
      <QueryEditor value={queryLanguageQuery} setValue={setQueryLanguageQuery} />
    </div>

  const QueryTabs =
    <>
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

      {NaturalLanguageTab}
      {QueryLanguageTab}
    </>

  const PageContent =
    <>
      <Typography variant="h4" component="h2">{database?.name}</Typography>

      {QueryTabs}

      <LoadingButton
        loading={queryLoading}
        fullWidth
        variant="contained"
        onClick={queryDatabase}
        className={styles.queryButton}
      >Query</LoadingButton>

      <QueryResultComponent
        queryResponse={queryResponse}
        showGeneratedQuery={showGeneratedQuery}
        editQueryInEditor={editQueryInEditor}
        totalCount={totalCount}
        page={page}
        pageSize={pageSize}
        onPageChange={onPageChange}
        setPageSize={setPageSize}
      />
    </>

  return (
    <>
      <Typography variant="h2" component="h1">Query</Typography>
      {!databaseLoading && PageContent}
    </>
  )
}