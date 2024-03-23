interface QueryResult {
  columnNames: string[]
  rows: string[][]
}

interface QueryResponse {
  result: QueryResult | null
  query: string
  totalCount: number | null // total count of rows (response is paginated, so it does not contain all of them)
  errorMessage: string | null
}

export type { QueryResponse, QueryResult }