import { ActionReducerMapBuilder, createAsyncThunk, createSlice, PayloadAction } from '@reduxjs/toolkit'
import { ChatHistoryItem } from '../../types/Chat.ts'
import databaseApi from '../../services/api/databaseApi.ts'
import { AxiosResponse } from 'axios'

interface ChatHistoryState {
  chatHistory: ChatHistoryItem[]
  loading: boolean,
  error: string | undefined,
}

const initialState: ChatHistoryState = {
  chatHistory: [],
  loading: false,
  error: undefined
}

const chatHistorySlice = createSlice({
  name: 'chatHistory',
  initialState,
  reducers: {
    // example reducer
    addElement: (state: ChatHistoryState, action: PayloadAction<ChatHistoryItem>): void => {
      state.chatHistory = [
        ...state.chatHistory,
        action.payload
      ]
      console.log(state)
    }
  },
  extraReducers: (builder: ActionReducerMapBuilder<ChatHistoryState>): void => {
    builder
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

export const { addElement } = chatHistorySlice.actions

export default chatHistorySlice.reducer