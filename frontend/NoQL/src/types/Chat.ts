interface ChatDto {
  id: string,
  name: string,
  modificationData: string
}

interface ChatFromApi { // TODO: rename to just a chat
  id: string,
  name: string,
  messages: MessageWithResponse[],
  modificationDate: string, // TODO date
}

interface MessageWithResponse {
  id: string,
  message: string,
  response: string,
  timestamp: string, // TODO date
}

export type { ChatDto, ChatFromApi, MessageWithResponse }