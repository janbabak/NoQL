interface ChatHistoryItem {
  id: string,
  name: string,
}

interface Chat {
  id: string,
  name: string,
  messages: ChatQueryWithResponse[],
  modificationDate: string, // TODO date
}

interface ChatQueryWithResponse {
  id: string,
  nlQuery: string, // natural language query
  llmResult: LLMResult, // large language model result
  timestamp: string, // TODO date
}

interface LLMResult {
  databaseQuery: string | null,
  plotUrl: string | null
}

export type { ChatHistoryItem, Chat, ChatQueryWithResponse, LLMResult }