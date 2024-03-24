import { GeneratedQuery } from './GeneratedQuery.tsx'
import { Alert, Button } from '@mui/material'
import { ResultTable } from './ResultTable.tsx'
import { QueryResponse } from '../../types/QueryResponse.ts'

export function QueryResultComponent({
                                       queryResponse, showGeneratedQuery,
                                       editQueryInEditor,
                                       totalCount,
                                       page,
                                       pageSize,
                                       onPageChange,
                                       onPageSizeChange
                                     }: {
  queryResponse: QueryResponse | null,
  showGeneratedQuery: boolean,
  editQueryInEditor: (query: string) => void,
  totalCount: number | null,
  page: number,
  pageSize: number,
  onPageChange: (page: number, pageSize: number) => void,
  onPageSizeChange: (newPageSize: number) => void,
}) {
  const EditQueryButon =
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
            <Alert severity="error" action={EditQueryButon}>
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
    </>
  )
}