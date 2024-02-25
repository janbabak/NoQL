interface QueryResult {
  columnNames: string[]
  rows: string[][]
}

interface QueryResponse {
  query: string
  result: QueryResult
}

export type { QueryResponse, QueryResult }