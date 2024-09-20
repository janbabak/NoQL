export type RegisterRequest = {
  firstName: string
  lastName: string
  username: string
  password: string
}

export type AuthenticationRequest = {
  email: string
  password: string
}

export type AuthenticationResponse = {
  token: string
  user: User
}

export type User = {
  id: string
  firstName: string
  lastName: string
  email: string
}