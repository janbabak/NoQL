import { ChatNew, ChatResponse, ChatResponseData } from '../../types/Chat.ts'
import { ActionReducerMapBuilder, createAsyncThunk, createSlice, PayloadAction } from '@reduxjs/toolkit'
import chatApi from '../../services/api/chatApi.ts'
import { AxiosResponse } from 'axios'
import messageApi from '../../services/api/messageApi.ts'

interface ChatState {
  chatNew: ChatNew | null,
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
    setChat: (state: ChatState, action: PayloadAction<ChatNew>): void => {
      state.chatNew = action.payload
    }
  },
  extraReducers: (builder: ActionReducerMapBuilder<ChatState>): void => {
    builder
      // fetch chat new
      .addCase(fetchChat.fulfilled,
        (state: ChatState, action: PayloadAction<ChatNew>): void => {
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
        (state: ChatState, action: PayloadAction<{data: ChatResponseData, messageId: string}>): void => {

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
  async (chatId: string): Promise<ChatNew> => {
    return await chatApi.getByIdNew(chatId)
      .then((response: AxiosResponse<ChatNew>) => response.data)
      .catch((error) => error)
  }
)

export const loadChatMessageData = createAsyncThunk(
  'chat/loadChatMessagePage',
  async (payload: { messageId: string, page: number, pageSize: number }) => {
    return await messageApi.getMessageData(payload.messageId, payload.page, payload.pageSize)
      .then((response: AxiosResponse<ChatResponseData>) => {
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