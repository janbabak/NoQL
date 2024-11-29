import { ChatQueryWithResponse, ChatResponseData } from './Chat.ts'

interface QueryRequest {
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

interface ConsoleResponse { // TODO: find right file for this type
  data: ChatResponseData | null
  dbQuery: string | null
  error: string | null
}

export type { QueryRequest, QueryResponse, RetrievedData, ConsoleResponse }