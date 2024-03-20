interface QueryResult {
  columnNames: string[]
  rows: string[][]
}

interface QueryResponse {
  query: string
  result: QueryResult
  totalCount: number
}

export type { QueryResponse, QueryResult }