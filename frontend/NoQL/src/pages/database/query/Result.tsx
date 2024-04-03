import { Alert, Button } from '@mui/material'
import { ResultTable } from './ResultTable.tsx'
import { QueryResponse } from '../../../types/Query.ts'
import React from 'react'

interface Props {
  queryResponse: QueryResponse | null,
  showGeneratedQuery: boolean
  editQueryInEditor: (query: string) => void,
  page: number,
  pageSize: number,
  setPageSize: React.Dispatch<React.SetStateAction<number>>
  totalCount: number | null,
  onPageChange: (page: number, pageSize: number) => Promise<void>
}

export function Result(
  {
    queryResponse,
    editQueryInEditor,
    page,
    pageSize,
    setPageSize,
    totalCount,
    onPageChange
  }: Props) {

  async function onPageSizeChange(newPageSize: number): Promise<void> {
    setPageSize(0)
    await onPageChange(0, newPageSize)
  }

  const EditQueryButton =
    <Button
      onClick={() => editQueryInEditor(queryResponse?.query || '')}
      size="small"
      color="inherit"
    >
      Edit query
    </ Button>

  return (
    <div style={{marginTop: '3rem'}}>
      {queryResponse != null &&
        <div>
          {queryResponse.errorMessage != null &&
            <Alert severity="error" action={EditQueryButton}>
              {queryResponse.errorMessage}
            </Alert>
          }

          {totalCount != null &&
            <ResultTable
              queryResult={queryResponse}
              page={page}
              pageSize={pageSize}
              totalCount={totalCount}
              onPageChange={onPageChange}
              onPageSizeChange={onPageSizeChange}
            />
          }
        </div>
      }
    </div>
  )
}