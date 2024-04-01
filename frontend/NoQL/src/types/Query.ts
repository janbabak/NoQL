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

interface ChatRequest {
  /**
   * List of messages, where the first message is use's query, second message is LLM response to that query
   * and so on... (even indices contain user's queries and odd indices contain LLM responses)
   */
  messages: string[]
}

export type { QueryResponse, QueryResult, ChatRequest }