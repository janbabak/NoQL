import { configureStore } from '@reduxjs/toolkit'
import chatHistoryReducer from './chatHistory/chatHistorySlice.ts'

export const store = configureStore({
  reducer: {
    chatHistoryReducer
  }
})

export type RootState = ReturnType<typeof store.getState>
export type AppDispatch = typeof store.dispatch