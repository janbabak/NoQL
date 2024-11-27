interface ChatHistoryItem {
  id: string,
  name: string,
}

interface Chat {
  id: string,
  name: string,
  messages: ChatQueryWithResponse[],
  modificationDate: Date,
}

interface ChatNew {
  id: string,
  name: string,
  messages: ChatResponse[],
  modificationDate: Date,
  databaseId: string
}

interface ChatQueryWithResponse {
  id: string,
  nlQuery: string, // natural language query
  llmResult: LLMResult, // large language model result
  timestamp: Date,
}

interface ChatResponse {
  data: ChatResponseData | null
  messageId: string,
  nlQuery: string,
  dbQuery: string,
  plotUrl: string,
  timestamp: Date,
  error: string,
}

interface ChatResponseData {
  columnNames: string[],
  rows: string[][],
  page: number,
  pageSize: number,
  totalCount: number,
}

interface LLMResult {
  databaseQuery: string | null,
  plotUrl: string | null
}

export type {
  ChatHistoryItem,
  Chat,
  ChatQueryWithResponse,
  LLMResult,
  ChatNew,
  ChatResponse,
  ChatResponseData
}