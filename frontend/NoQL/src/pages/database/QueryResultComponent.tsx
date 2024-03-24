import { GeneratedQuery } from './GeneratedQuery.tsx'
import { Alert, Button } from '@mui/material'
import { ResultTable } from './ResultTable.tsx'
import { QueryResponse } from '../../types/QueryResponse.ts'
import React from 'react'

export function QueryResultComponent({
                                       queryResponse, showGeneratedQuery,
                                       editQueryInEditor,
                                       totalCount,
                                       page,
                                       pageSize,
                                       setPageSize,
                                       onPageChange,
                                     }: {
  queryResponse: QueryResponse | null,
  showGeneratedQuery: boolean,
  editQueryInEditor: (query: string) => void,
  totalCount: number | null,
  page: number,
  pageSize: number,
  setPageSize: React.Dispatch<React.SetStateAction<number>>
  onPageChange: (page: number, pageSize: number) => void,
}) {
  async function onPageSizeChange(newPageSize: number) {
    setPageSize(0)
    onPageChange(0, newPageSize)
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
    <>
      {queryResponse != null &&
        <div>
          {showGeneratedQuery &&
            <GeneratedQuery query={queryResponse.query} />
          }

          {queryResponse.errorMessage != null &&
            <Alert severity="error" action={EditQueryButton}>
              {queryResponse.errorMessage}
            </Alert>
          }

          {totalCount != null &&
            <ResultTable
              queryResponse={queryResponse}
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
  )
}