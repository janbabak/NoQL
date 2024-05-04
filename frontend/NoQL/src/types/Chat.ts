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
  nlquery: string, // natural language query
  chatResponseResult: ChatResponseResult,
  timestamp: string, // TODO date
}

interface ChatResponseResult {
  databaseQuery: string | null,
  plotUrl: string | null
}

export type { ChatHistoryItem, Chat, ChatQueryWithResponse, ChatResponseResult }