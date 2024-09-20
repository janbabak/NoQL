import { configureStore } from '@reduxjs/toolkit'
import chatHistoryReducer from '././chat/chatHistorySlice.ts'
import chatReducer from './chat/chatSlice.ts'
import snackbarReducer from './snackbarSlice.ts'
import authReducer from './authSlice.ts'

export const store = configureStore({
  reducer: {
    chatHistoryReducer,
    chatReducer,
    snackbarReducer,
    authReducer,
  }
})

export type RootState = ReturnType<typeof store.getState>
export type AppDispatch = typeof store.dispatch