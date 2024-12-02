import { Chat, ChatResponse } from '../../types/Chat.ts'
import { ActionReducerMapBuilder, createAsyncThunk, createSlice, PayloadAction } from '@reduxjs/toolkit'
import chatApi from '../../services/api/chatApi.ts'
import { AxiosResponse } from 'axios'
import messageApi from '../../services/api/messageApi.ts'
import { RetrievedData } from '../../types/Query.ts'

interface ChatState {
  chatNew: Chat | null,
  loading: boolean,
  error: string | undefined,
}

const initialState: ChatState = {
  chatNew: null,
  loading: false,
  error: undefined
}

interface ChatResponseAndName {
  message: ChatResponse,
  name: string
}

const chatSlice = createSlice({
  name: 'chat',
  initialState,
  reducers: {
    addMessage: (state: ChatState, action: PayloadAction<ChatResponse>): void => {
      if (!state.chatNew) {
        return
      }
      state.chatNew.messages = [
        ...state.chatNew.messages,
        action.payload
      ]
    },
    addMessageAndChangeName: (state: ChatState, action: PayloadAction<ChatResponseAndName>): void => {
      if (!state.chatNew) {
        return
      }
      state.chatNew = {
        ...state.chatNew,
        messages: [
          ...state.chatNew.messages,
          action.payload.message
        ],
        name: action.payload.name
      }
    },
    setChatToNull: (state: ChatState): void => {
      state.chatNew = null
    },
    setChat: (state: ChatState, action: PayloadAction<Chat>): void => {
      state.chatNew = action.payload
    }
  },
  extraReducers: (builder: ActionReducerMapBuilder<ChatState>): void => {
    builder
      .addCase(fetchChat.fulfilled,
        (state: ChatState, action: PayloadAction<Chat>): void => {
          state.chatNew = action.payload
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
      .addCase(loadChatMessageData.fulfilled,
        (state: ChatState, action: PayloadAction<{data: RetrievedData, messageId: string}>): void => {

        if (!state.chatNew || !state.chatNew.messages) {
          return
        }

        const messageIndex: number = state.chatNew.messages.findIndex(
          (message: ChatResponse): boolean => message.messageId === action.payload.messageId)

        if (messageIndex !== -1) {
          state.chatNew.messages[messageIndex].data = action.payload.data
        }
      })

  }
})

export const fetchChat = createAsyncThunk('chat/fetchChatNew',
  async (chatId: string): Promise<Chat> => {
    return await chatApi.getById(chatId)
      .then((response: AxiosResponse<Chat>) => response.data)
      .catch((error) => error)
  }
)

export const loadChatMessageData = createAsyncThunk(
  'chat/loadChatMessagePage',
  async (payload: { messageId: string, page: number, pageSize: number }) => {
    return await messageApi.getMessageData(payload.messageId, payload.page, payload.pageSize)
      .then((response: AxiosResponse<RetrievedData>) => {
        return {
          data: response.data,
          messageId: payload.messageId
        }
      })
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