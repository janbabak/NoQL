import { ChatQueryWithResponse } from './Chat.ts'

interface QueryRequest {
  chatId: string,
  query: string,
  model: string,
}

interface RetrievedData {
  columnNames: string[]
  rows: string[][]
}

interface QueryResponse {
  data: RetrievedData | null
  totalCount: number | null // total count of rows (response is paginated, so it does not contain all of them)
  chatQueryWithResponse: ChatQueryWithResponse
  errorMessage: string | null
}

export type { QueryRequest, QueryResponse, RetrievedData }