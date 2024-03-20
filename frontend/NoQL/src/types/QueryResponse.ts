interface QueryResult {
  columnNames: string[]
  rows: string[][]
}

interface QueryResponse {
  query: string
  result: QueryResult
  totalCount: number // total count of rows (response is paginated, so it does not contain all of them)
}

export type { QueryResponse, QueryResult }