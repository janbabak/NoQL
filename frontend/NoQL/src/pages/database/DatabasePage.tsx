import { useParams } from 'react-router'
import { useEffect, useRef, useState } from 'react'
import { Database } from '../../types/Database.ts'
import databaseApi, { QueryResponse } from '../../services/api/databaseApi.ts'
import { Alert, CircularProgress, Fab, TextField, Typography } from '@mui/material'
import { LoadingButton } from '@mui/lab'
import styles from './Database.module.css'
import { ResultTable } from './ResultTable.tsx'
import { GeneratedQuery } from './GeneratedQuery.tsx'
import { Editor } from '@monaco-editor/react'
import { green } from '@mui/material/colors'
import ChangeHistoryRoundedIcon from '@mui/icons-material/ChangeHistoryRounded'
import CheckIcon from '@mui/icons-material/Check'


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

  const buttonSx = {
    ...(queryLoading && {
      bgcolor: green[500],
      '&:hover': {
        bgcolor: green[700]
      }
    })
  }

  const queryLanguageQuery = useRef<any>('')
  function handleEditorDidMount(editor, monaco) {
    queryLanguageQuery.current = editor;
  }

  /* eslint-disable  @typescript-eslint/no-explicit-any */
  const naturalLanguageQuery = useRef<any>('')

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
  async function queryDatabase(naturalLanguage: boolean) {
    setPage(0)
    setQueryLoading(true)
    try {
      const response = naturalLanguage
        ? await databaseApi.queryNaturalLanguage(id || '', naturalLanguageQuery.current.value, pageSize)
        : await databaseApi.queryQueryLanguageQuery(id || '', queryLanguageQuery.current.getValue(), 0, pageSize)
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

      <div className={styles.queryInput}>
        <TextField
          id="query"
          label="Query"
          variant="outlined"
          inputRef={naturalLanguageQuery}
          fullWidth
        />
      </div>

      <Editor
        height="100px"
        language="sql"
        theme="vs-dark"
        onMount={handleEditorDidMount}
        options={{
          inlineSuggest: true,
          fontSize: '16px',
          formatOnType: true,
          autoClosingBrackets: true,
          minimap: { enabled: false }
        }}
      />

      <div style={{ position: 'relative' }}>

        <Fab
          aria-label="save"
          color="primary"
          sx={buttonSx}
          onClick={() => queryDatabase(false)}
        >
          {queryLoading ? <CheckIcon /> :
            <ChangeHistoryRoundedIcon style={{ transform: 'rotate(0.25turn) translateY(-2px)' }} />}
        </Fab>
        {queryLoading && (
          <CircularProgress
            size={68}
            sx={{
              color: green[500],
              position: 'absolute',
              top: -6,
              left: -6,
              zIndex: 1
            }}
          />
        )}
      </div>

      <LoadingButton
        loading={queryLoading}
        fullWidth
        variant="contained"
        onClick={() => queryDatabase(true)}
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