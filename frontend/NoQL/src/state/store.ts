import { configureStore } from '@reduxjs/toolkit'
import chatHistoryReducer from '././chat/chatHistorySlice.ts'
import chatReducer from './chat/chatSlice.ts'

export const store = configureStore({
  reducer: {
    chatHistoryReducer,
    chatReducer
  }
})

export type RootState = ReturnType<typeof store.getState>
export type AppDispatch = typeof store.dispatch