interface QueryRequest {
  query: string,
  model: string,
}

interface ConsoleResponse {
  data: RetrievedData | null
  dbQuery: string | null
  error: string | null
}

interface ChatResponse {
  data: RetrievedData | null
  messageId: string,
  nlQuery: string,
  dbQuery: string | null,
  plotUrl: string | null,
  description: string | null,
  timestamp: Date,
  error: string,
}

interface RetrievedData {
  columnNames: string[],
  rows: string[][],
  page: number,
  pageSize: number,
  totalCount: number,
}

export type {
  QueryRequest,
  ConsoleResponse,
  ChatResponse,
  RetrievedData
}