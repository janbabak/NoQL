export const localStorageService = {
  TOKEN_KEY: 'token',
  USER_ID_KEY: 'userId',

  getToken(): string | null {
    return window.localStorage.getItem(this.TOKEN_KEY)
  },

  setToken(token: string): void {
    window.localStorage.setItem(this.TOKEN_KEY, token)
  },

  clearToken(): void {
    window.localStorage.removeItem(this.TOKEN_KEY)
  },

  getUserId(): string | null {
    return window.localStorage.getItem(this.USER_ID_KEY)
  },

  setUserId(userId: string): void {
    window.localStorage.setItem(this.USER_ID_KEY, userId)
  },

  clearUserId(): void {
    window.localStorage.removeItem(this.USER_ID_KEY)
  },

  clearAll(): void {
    window.localStorage.clear()
  }
}