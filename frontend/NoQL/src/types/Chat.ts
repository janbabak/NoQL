import { Database } from './Database.ts'

interface ChatDto {
  id: string,
  name: string,
  modificationData: string
}

interface ChatFromApi { // TODO: rename to just a chat
  id: string,
  database: Database,
  messages: MessageWithResponse[],
  modificationDate: string, // TODO date
  name: string,
}

interface MessageWithResponse {
  id: string,
  message: string,
  response: string,
  timestamp: string, // TODO date
}

export type { ChatDto, ChatFromApi, MessageWithResponse }