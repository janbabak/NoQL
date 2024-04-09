import { ChatQueryWithResponse } from './Chat.ts'

interface QueryRequest {
  chatId: string,
  query: string,
}

interface QueryResult {
  columnNames: string[]
  rows: string[][]
}

interface QueryResponse {
  result: QueryResult | null
  totalCount: number | null // total count of rows (response is paginated, so it does not contain all of them)
  chatQueryWithResponse: ChatQueryWithResponse
  errorMessage: string | null
}

export type { QueryRequest, QueryResponse, QueryResult }