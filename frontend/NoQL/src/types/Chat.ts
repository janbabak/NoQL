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
  query: string,
  response: string,
  timestamp: string, // TODO date
}

export type { ChatHistoryItem, Chat, ChatQueryWithResponse }