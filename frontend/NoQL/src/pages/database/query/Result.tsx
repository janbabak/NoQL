import { Alert, Button, Paper } from '@mui/material'
import { ResultTable } from './ResultTable.tsx'
import { QueryResponse } from '../../../types/Query.ts'
import React, { useEffect, useState } from 'react'

interface Props {
  queryResponse: QueryResponse | null,
  showEditInConsoleButton: boolean,
  editQueryInConsole: (query: string) => void,
  page: number,
  pageSize: number,
  setPageSize: React.Dispatch<React.SetStateAction<number>>
  totalCount: number | null,
  onPageChange: (page: number, pageSize: number) => Promise<void>,
  loading: boolean,
}

export function Result(
  {
    queryResponse,
    showEditInConsoleButton,
    editQueryInConsole,
    page,
    pageSize,
    setPageSize,
    totalCount,
    onPageChange,
    loading
  }: Props) {

  const [
    timestamp,
    setTimestamp
  ] = useState<number>(Date.now());

  // Function to update the timestamp, triggering a reload of the image
  const reloadImage = (): void => {
    setTimestamp(Date.now());
  };

  useEffect((): void => {
    reloadImage()
  }, [queryResponse])

  async function onPageSizeChange(newPageSize: number): Promise<void> {
    setPageSize(0)
    await onPageChange(0, newPageSize)
  }

  const EditQueryButton =
    <Button
      onClick={(): void => {
        editQueryInConsole(queryResponse?.chatQueryWithResponse?.llmResult?.databaseQuery || '')
      }}
      size="small"
      color="inherit"
    >
      Edit query
    </ Button>

  return (
    <div style={{ marginTop: '3rem' }}>
      {queryResponse != null &&
        <div>
          {queryResponse.errorMessage != null &&
            <Alert
              severity="error"
              action={showEditInConsoleButton ? EditQueryButton : null}
            >
              {queryResponse.errorMessage}
            </Alert>
          }

          {queryResponse?.chatQueryWithResponse?.llmResult?.plotUrl != null &&
            // TODO: backend url
            <Paper elevation={2} style={{marginBottom: '2rem', display: 'flex', justifyContent: 'center'}}>
              <img src={'http://localhost:8080' + queryResponse.chatQueryWithResponse.llmResult.plotUrl + `?timestamp=${timestamp}`}
                   alt="plot"
              />
            </Paper>
          }

          {totalCount != null && queryResponse.data &&
            <ResultTable
              queryResult={queryResponse}
              page={page}
              pageSize={pageSize}
              totalCount={totalCount}
              onPageChange={onPageChange}
              onPageSizeChange={onPageSizeChange}
              loading={loading}
            />
          }
        </div>
      }
    </div>
  )
}