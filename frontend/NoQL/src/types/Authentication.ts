export type RegisterRequest = {
  firstName: string
  lastName: string
  email: string
  password: string
}

export type AuthenticationRequest = {
  email: string
  password: string
}

export type AuthenticationResponse = {
  accessToken: string
  refreshToken: string
  user: User
}

export type User = {
  id: string
  firstName: string
  lastName: string
  email: string
  queryLimit: number
}