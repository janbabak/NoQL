import { ChatResponse } from './Query.ts'

interface ChatHistoryItem {
  id: string,
  name: string,
}

interface Chat {
  id: string,
  name: string,
  messages: ChatResponse[],
  modificationDate: Date,
  databaseId: string
}

export type {
  ChatHistoryItem,
  Chat,
  ChatResponse,
}