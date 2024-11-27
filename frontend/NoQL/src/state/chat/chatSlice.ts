import { Chat, ChatNew, ChatQueryWithResponse } from '../../types/Chat.ts'
import { ActionReducerMapBuilder, createAsyncThunk, createSlice, PayloadAction } from '@reduxjs/toolkit'
import chatApi from '../../services/api/chatApi.ts'
import { AxiosResponse } from 'axios'

interface ChatState {
  chat: Chat | null,
  chatNew: ChatNew | null,
  loading: boolean,
  error: string | undefined,
}

const initialState: ChatState = {
  chat: null,
  chatNew: null,
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
    },
    setChat: (state: ChatState, action: PayloadAction<Chat>): void => {
      state.chat = action.payload
    }
  },
  extraReducers: (builder: ActionReducerMapBuilder<ChatState>): void => {
    builder
      // fetch chat
      .addCase(fetchChat.fulfilled,
        (state: ChatState, action: PayloadAction<Chat>): void => {
          state.chat = action.payload
          state.loading = false
          state.error = undefined
        })
      .addCase(fetchChat.pending, (state: ChatState): void => {
        state.loading = true
      })
      .addCase(fetchChat.rejected,
        (state: ChatState, action): void => {
          state.loading = false
          state.error = action.error.message
        })
      // fetch chat new
      .addCase(fetchChatNew.fulfilled,
        (state: ChatState, action: PayloadAction<ChatNew>): void => {
          state.chatNew = action.payload
          state.loading = false
          state.error = undefined
        })
      .addCase(fetchChatNew.pending, (state: ChatState): void => {
        state.loading = true
      })
      .addCase(fetchChatNew.rejected,
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

export const fetchChatNew = createAsyncThunk('chat/fetchChatNew',
  async (chatId: string): Promise<ChatNew> => {
    return await chatApi.getByIdNew(chatId)
      .then((response: AxiosResponse<ChatNew>) => response.data)
      .catch((error) => error)
  }
)

export const {
  addMessage,
  addMessageAndChangeName,
  setChatToNull,
  setChat
} = chatSlice.actions

export default chatSlice.reducer