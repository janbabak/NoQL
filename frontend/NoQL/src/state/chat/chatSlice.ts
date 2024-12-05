import { Chat, ChatResponse } from '../../types/Chat.ts'
import { ActionReducerMapBuilder, createAsyncThunk, createSlice, PayloadAction } from '@reduxjs/toolkit'
import chatApi from '../../services/api/chatApi.ts'
import { AxiosResponse } from 'axios'
import messageApi from '../../services/api/messageApi.ts'
import { RetrievedData } from '../../types/Query.ts'

interface ChatState {
  chat: Chat | null,
  /*
   * Watched by watcher - if it changes chat view scrolls to the bottom.
   * Changes if new chat has been fetched or if new message arrives this increments.
   * Watching chat doesn't work because if message result changes (new page is loaded) we don't want to scroll.
   */
  chatChanged: number,
  loading: boolean,
  error: string | undefined,
}

const initialState: ChatState = {
  chat: null,
  chatChanged: 0,
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
      if (!state.chat) {
        return
      }
      state.chat.messages = [
        ...state.chat.messages,
        action.payload
      ]
      state.chatChanged++
    },
    addMessageAndChangeName: (state: ChatState, action: PayloadAction<ChatResponseAndName>): void => {
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
      state.chatChanged++
    },
    setChatToNull: (state: ChatState): void => {
      state.chat = null
      state.chatChanged++
    },
    setChat: (state: ChatState, action: PayloadAction<Chat>): void => {
      state.chat = action.payload
      state.chatChanged++
    }
  },
  extraReducers: (builder: ActionReducerMapBuilder<ChatState>): void => {
    builder
      .addCase(fetchChat.fulfilled,
        (state: ChatState, action: PayloadAction<Chat>): void => {
          state.chat = action.payload
          state.loading = false
          state.error = undefined
          state.chatChanged++
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

        if (!state.chat || !state.chat.messages) {
          return
        }

        const messageIndex: number = state.chat.messages.findIndex(
          (message: ChatResponse): boolean => message.messageId === action.payload.messageId)

        if (messageIndex !== -1) {
          state.chat.messages[messageIndex].data = action.payload.data
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