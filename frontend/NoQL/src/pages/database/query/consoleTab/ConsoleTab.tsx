import { CONSOLE_TAB } from '../Constants.ts'
import styles from '../Query.module.css'
import { QueryEditor } from './QueryEditor.tsx'
import IconButton from '@mui/material/IconButton'
import PlayCircleFilledWhiteRoundedIcon from '@mui/icons-material/PlayCircleFilledWhiteRounded'
import React, { memo, useState } from 'react'
import databaseApi from '../../../../services/api/databaseApi.ts'
import { ConsoleResponse } from '../../../../types/Query.ts'
import { LinearProgress } from '@mui/material'
import { AppDispatch } from '../../../../state/store.ts'
import { useDispatch } from 'react-redux'
import { showErrorWithMessageAndError } from '../../../../components/snackbar/GlobalSnackbar.helpers.ts'
import { ResultTable } from '../ResultTable.tsx'

interface ConsoleTabProps {
  databaseId: string
  tab: number
  queryLanguageQuery: string,
  setQueryLanguageQuery: React.Dispatch<React.SetStateAction<string>>,
}

const ConsoleTab = memo((
  {
    databaseId,
    tab,
    queryLanguageQuery,
    setQueryLanguageQuery
  }: ConsoleTabProps) => {

  const dispatch: AppDispatch = useDispatch()

  const [
    queryResult,
    setQueryResult
  ] = useState<ConsoleResponse | null>(null)

  const [
    queryLoading,
    setQueryLoading
  ] = useState<boolean>(false)

  async function executeQuery(): Promise<void> {
    setQueryLoading(true)
    try {
      const response = await databaseApi.queryQueryLanguageQuery(
        databaseId, queryLanguageQuery, 0, queryResult?.data?.pageSize || 10) // TODO: default limit?
      setQueryResult(response.data)
    } catch (error: unknown) {
      showErrorWithMessageAndError(dispatch, 'Failed to execute query', error)
    } finally {
      setQueryLoading(false)
    }
  }

  async function onPageChange(page: number, pageSize: number): Promise<void> {
    setQueryLoading(true)
    try {
      const response = await databaseApi.queryQueryLanguageQuery(
        databaseId,
        queryResult?.dbQuery || '',
        page,
        pageSize)

      setQueryResult(response.data)
    } catch (error: unknown) {
      showErrorWithMessageAndError(dispatch, 'Failed to execute query', error)
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
      {queryLoading && <LinearProgress />}

      {ExecuteButton}

      {queryResult?.data &&
        <ResultTable
          data={queryResult?.data}
          onPageChange={onPageChange}
          loading={queryLoading}
        />
      }
    </div>
  )
})

export { ConsoleTab }