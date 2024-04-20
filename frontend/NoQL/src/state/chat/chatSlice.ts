import { Chat, ChatQueryWithResponse } from '../../types/Chat.ts'
import { ActionReducerMapBuilder, createAsyncThunk, createSlice, PayloadAction } from '@reduxjs/toolkit'
import chatApi from '../../services/api/chatApi.ts'
import { AxiosResponse } from 'axios'

interface ChatState {
  chat: Chat | null,
  loading: boolean,
  error: string | undefined,
}

const initialState: ChatState = {
  chat: null,
  loading: false,
  error: undefined
}

interface MessageWithNamePayload {
  message: ChatQueryWithResponse,
  name: string
}

const chatSlice = createSlice({
  name: 'chat',
  initialState,
  reducers: {
    addMessage: (state: ChatState, action: PayloadAction<ChatQueryWithResponse>): void => {
      if (!state.chat) {
        return
      }
      state.chat.messages = [
        ...state.chat.messages,
        action.payload
      ]
    },
    addMessageAndChangeName: (state: ChatState, action: PayloadAction<MessageWithNamePayload>): void => {
      if (!state.chat) {
        return
      }
      state.chat = {
        ...state.chat,
        messages: [
          ...state.chat.messages,
          action.payload.message
        ],
        name: action.payload.name
      }
    },
    setChatToNull: (state: ChatState): void => {
      state.chat = null
    }
  },
  extraReducers: (builder: ActionReducerMapBuilder<ChatState>): void => {
    builder
      // fetch history
      .addCase(fetchChat.fulfilled,
        (state: ChatState, action: PayloadAction<Chat>): void => {
          state.chat = action.payload
          state.loading = false
          state.error = undefined
        })
      .addCase(fetchChat.pending, (state: ChatState): void => {
        state.loading = false
      })
      .addCase(fetchChat.rejected,
        (state: ChatState, action): void => {
          state.loading = false
          state.error = action.error.message
        })
  }
})

export const fetchChat = createAsyncThunk('chat/fetchChat',
  async (chatId: string): Promise<Chat> => {
    return await chatApi.getById(chatId)
      .then((response: AxiosResponse<Chat>) => response.data)
      .catch((error) => error)
  }
)

export const {
  addMessage,
  addMessageAndChangeName,
  setChatToNull} = chatSlice.actions

export default chatSlice.reducer