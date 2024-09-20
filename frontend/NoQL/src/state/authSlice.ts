import { AuthenticationResponse, User } from '../types/Authentication.ts'
import { createSlice, PayloadAction } from '@reduxjs/toolkit'

interface AuthSlice {
  token: string | null,
  user: User | null,
}

const initialState: AuthSlice = {
  token: null,
  user: null,
}

const authSlice = createSlice({
  name: 'auth',
  initialState,
  reducers: {
    authenticate: (state: AuthSlice, action: PayloadAction<AuthenticationResponse>): void => {
      state.token = action.payload.token
      state.user = action.payload.user
    },
    logOut: (state: AuthSlice): void => {
      state.token = null
      state.user = null
    }
  }
})

export const {
  authenticate,
  logOut,
} = authSlice.actions

export default authSlice.reducer