import { MessageWithResponse } from './Chat.ts'

interface QueryRequest {
  chatId: string,
  message: string,
}

interface QueryResult {
  columnNames: string[]
  rows: string[][]
}

interface QueryResponse {
  result: QueryResult | null
  totalCount: number | null // total count of rows (response is paginated, so it does not contain all of them)
  messageWithResponse: MessageWithResponse
  errorMessage: string | null
}

interface Chat {
  /**
   * List of messages, where the first message is use's query, second message is LLM response to that query
   * and so on... (even indices contain user's queries and odd indices contain LLM responses)
   */
  messages: string[]
}

export type { QueryRequest, QueryResponse, QueryResult, Chat }