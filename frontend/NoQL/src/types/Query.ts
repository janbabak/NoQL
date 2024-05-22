import { ChatQueryWithResponse } from './Chat.ts'

interface QueryRequest {
  chatId: string,
  query: string,
  model: LlmModel,
}

enum LlmModel {
  GPT_4o = 'GPT_4o',
  GPT_4 = 'GPT_4',
  GPT_4_TURBO = 'GPT_4_TURBO',
  GPT_4_32K = 'GPT_4_32K',
  GPT_3_5_TURBO = 'GPT_3_5_TURBO',
  LLAMA3_70B = 'LLAMA3_70B',
  LLAMA3_13B_CHAT = 'LLAMA3_13B_CHAT',
}

interface RetrievedData {
  columnNames: string[]
  rows: string[][]
}

interface QueryResponse {
  data: RetrievedData | null
  totalCount: number | null // total count of rows (response is paginated, so it does not contain all of them)
  chatQueryWithResponse: ChatQueryWithResponse
  errorMessage: string | null
}

export type { QueryRequest, QueryResponse, RetrievedData }
export { LlmModel }