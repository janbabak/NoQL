import { CONSOLE_TAB } from './Constants.ts'
import styles from './Query.module.css'
import { QueryEditor } from './QueryEditor.tsx'
import IconButton from '@mui/material/IconButton'
import PlayCircleFilledWhiteRoundedIcon from '@mui/icons-material/PlayCircleFilledWhiteRounded'
import React, { useState } from 'react'
import databaseApi from '../../../services/api/databaseApi.ts'
import { QueryResponse } from '../../../types/Query.ts'
import { Result } from './Result.tsx'
import { LinearProgress } from '@mui/material'

interface ConsoleTabProps {
  databaseId: string
  tab: number
  queryLanguageQuery: string,
  setQueryLanguageQuery: React.Dispatch<React.SetStateAction<string>>,
}

export function ConsoleTab(
  {
    databaseId,
    tab,
    queryLanguageQuery,
    setQueryLanguageQuery
  }: ConsoleTabProps) {

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

  async function executeQuery(): Promise<void> {
    setPage(0)
    setQueryLoading(true)
    try {
      const response = await databaseApi.queryQueryLanguageQuery(
        databaseId, queryLanguageQuery, 0, pageSize)
      setQueryResult(response.data)
    } catch (error: unknown) {
      console.log(error) // TODO: handles
    } finally {
      setQueryLoading(false)
    }
  }

  async function onPageChange(page: number, pageSize: number): Promise<void> {
    setPageSize(pageSize)
    setPage(page)

    setQueryLoading(true)
    try {
      const response = await databaseApi.queryQueryLanguageQuery(
        databaseId,
        queryResult?.chatQueryWithResponse.nlquery || '',
        page,
        pageSize)

      setQueryResult(response.data)
    } catch (error: unknown) {
      console.log(error) // TODO: handles
    } finally {
      setQueryLoading(false)
    }
  }

  const ExecuteButton =
    <IconButton
      onClick={executeQuery}
      className={styles.executeButton}
      aria-label="execute query"
      size="large"
      color="success"
    >
      <PlayCircleFilledWhiteRoundedIcon fontSize="inherit" />
    </IconButton>

  return (
    <div
      role="tabpanel"
      hidden={tab != CONSOLE_TAB}
      className={styles.editorTab}
    >
      <QueryEditor value={queryLanguageQuery} setValue={setQueryLanguageQuery} />
      { queryLoading && <LinearProgress /> }

      {ExecuteButton}

      <Result
        queryResponse={queryResult}
        editQueryInConsole={(_query: string) => console.log('implement')}
        showEditInConsoleButton={false}
        page={page}
        pageSize={pageSize}
        setPageSize={setPageSize}
        totalCount={queryResult?.totalCount || 1}
        onPageChange={onPageChange}
        loading={queryLoading}
      />
    </div>
  )
}