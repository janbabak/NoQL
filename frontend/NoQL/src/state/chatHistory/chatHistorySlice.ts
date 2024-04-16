import { createSlice } from '@reduxjs/toolkit'
import { ChatHistoryItem } from '../../types/Chat.ts'

interface ChatHistoryState {
  chatHistory: ChatHistoryItem[]
}

const initialState: ChatHistoryState = {
  chatHistory: [
    {
      id: 'test',
      name: 'test'
    }
  ]
}

const chatHistorySlice = createSlice({
  name: 'chatHistory',
  initialState,
  reducers: {
    // example reducer
    sort: (state): void => {
      state.chatHistory.sort()
      console.log(state.chatHistory)
    },
    addElement: (state): void => {
      state.chatHistory = [
        {
          id: 'added',
          name: 'added'
        }
      ]
      console.log(state)
    }
  }
})

export const { sort, addElement } = chatHistorySlice.actions;

export default chatHistorySlice.reducer;