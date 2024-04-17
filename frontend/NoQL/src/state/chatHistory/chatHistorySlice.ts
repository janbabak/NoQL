import { ActionReducerMapBuilder, createAsyncThunk, createSlice, PayloadAction } from '@reduxjs/toolkit'
import { Chat, ChatHistoryItem } from '../../types/Chat.ts'
import databaseApi from '../../services/api/databaseApi.ts'
import { AxiosResponse } from 'axios'
import chatApi from '../../services/api/chatApi.ts'

interface ChatHistoryState {
  chatHistory: ChatHistoryItem[]
  loading: boolean,
  error: string | undefined,
  createNewChatLoading: boolean,
  activeChatIndex: number,
}

const initialState: ChatHistoryState = {
  chatHistory: [],
  loading: false,
  error: undefined,
  createNewChatLoading: false,
  activeChatIndex: 0
}

const chatHistorySlice = createSlice({
  name: 'chatHistory',
  initialState,
  reducers: {
    setActiveChatIndex: (state: ChatHistoryState, action: PayloadAction<number>): void => {
      state.activeChatIndex = action.payload
    }
  },
  extraReducers: (builder: ActionReducerMapBuilder<ChatHistoryState>): void => {
    builder
      // fetch chat history
      .addCase(fetchChatHistory.fulfilled,
        (state: ChatHistoryState, action: PayloadAction<ChatHistoryItem[]>): void => {
          state.chatHistory = action.payload
          state.loading = false
          state.error = undefined
        })
      .addCase(fetchChatHistory.pending, (state: ChatHistoryState): void => {
        state.loading = true
      })
      .addCase(fetchChatHistory.rejected,
        (state: ChatHistoryState, action): void => {
          state.loading = false
          state.error = action.error.message
        })
      // create new chat
      .addCase(createNewChat.fulfilled,
        (state: ChatHistoryState, action: PayloadAction<Chat>): void => {
          state.chatHistory = [
            { id: action.payload.id, name: action.payload.name },
            ...state.chatHistory
          ]
          state.createNewChatLoading = false
          state.activeChatIndex = 0
        })
      .addCase(createNewChat.pending, (state: ChatHistoryState): void => {
        state.createNewChatLoading = true
      })
      .addCase(createNewChat.rejected,
        (state: ChatHistoryState, action): void => {
          state.createNewChatLoading = false
          state.error = action.error.message
        })
  }
})

export const fetchChatHistory
  = createAsyncThunk('chatHistory/fetchChatHistory',
  async (databaseId: string): Promise<ChatHistoryItem[]> => {
    return await databaseApi.getChatHistoryByDatabaseId(databaseId)
      .then((response: AxiosResponse<ChatHistoryItem[]>) => response.data)
      .catch((error) => error)
  }
)

export const createNewChat
  = createAsyncThunk('chatHistory/createNewChat',
  async (databaseId: string): Promise<Chat> => {
    return await chatApi.createNewChat(databaseId)
      .then((response: AxiosResponse<Chat>) => response.data)
      .catch((error) => error)
  }
)

export const { setActiveChatIndex } = chatHistorySlice.actions

export default chatHistorySlice.reducer