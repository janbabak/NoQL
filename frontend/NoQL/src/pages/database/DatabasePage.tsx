import { useParams } from 'react-router'
import { useEffect, useRef, useState } from 'react'
import { Database } from '../../types/Database.ts'
import databaseApi, { QueryResponse } from '../../services/api/databaseApi.ts'
import { Alert, Box, Tab, Tabs, TextField, Typography } from '@mui/material'
import { LoadingButton } from '@mui/lab'
import styles from './Database.module.css'
import { ResultTable } from './ResultTable.tsx'
import { GeneratedQuery } from './GeneratedQuery.tsx'
import { Editor } from '@monaco-editor/react'

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

  /* eslint-disable  @typescript-eslint/no-explicit-any */
  const naturalLanguageQuery = useRef<any>('')

  const queryLanguageQuery = useRef<any>('')

  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  function handleEditorDidMount(editor: string, _monaco: unknown): void {
    queryLanguageQuery.current = editor
  }

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
          id || '', queryLanguageQuery.current.getValue(), 0, pageSize)
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

  function handleTabChange(_event: React.SyntheticEvent, newValue: number) {
    setTab(newValue)
  }

  const QueryResultElement =
    <>
      {queryResult != null &&
        <div>
          <GeneratedQuery query={queryResult.query} />


          {queryResult.errorMessage != null &&
            <Alert severity="error">{queryResult.errorMessage}</Alert>
          }

          {totalCount != null &&
            <ResultTable
              queryResult={queryResult}
              page={page}
              pageSize={pageSize}
              totalCount={totalCount}
              onPageChange={onPageChange}
              onPageSizeChange={onPageSizeChange}
            />
          }
        </div>
      }
    </>

  const PageContent =
    <>
      <Typography variant="h4" component="h2">{database?.name}</Typography>

      <Box sx={{ borderBottom: 1, borderColor: 'divider'}}>
        <Tabs
          value={tab}
          aria-label="basic tabs example"
          onChange={handleTabChange}
          sx={{borderRadius: '0.25rem', marginTop: '1.5rem'}}
        >
          <Tab label="Natural language" sx={{borderRadius: '0.25rem'}} />
          <Tab label="Query language" sx={{borderRadius: '0.25rem'}} />
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
        {/*https://microsoft.github.io/monaco-editor/docs.html#interfaces/editor.IStandaloneEditorConstructionOptions.html*/}
        <Editor
          height="200px"
          language="sql"
          theme="vs-dark"
          onMount={handleEditorDidMount}
          options={{
            inlineSuggest: true,
            fontSize: 16,
            fontFamily: 'monospace',
            lineHeight: 24,
            formatOnType: true,
            autoClosingBrackets: true,
            minimap: { enabled: false },
            padding: { top: 20 }
          }}
        />
      </div>

      <LoadingButton
        loading={queryLoading}
        fullWidth
        variant="contained"
        onClick={queryDatabase}
        className={styles.queryButton}
      >Query</LoadingButton>

      {QueryResultElement}
    </>

  return (
    <>
      <Typography variant="h2" component="h1">Query</Typography>
      {!databaseLoading && PageContent}
    </>
  )
}