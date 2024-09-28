export const localStorageService = {
  TOKEN_KEY: 'token',
  USER_ID_KEY: 'userId',

  /**
   * Get authentication token from local storage.
   */
  getToken(): string | null {
    return window.localStorage.getItem(this.TOKEN_KEY)
  },

  /**
   * Set authentication token to local storage.
   * @param token jwt token
   */
  setToken(token: string): void {
    window.localStorage.setItem(this.TOKEN_KEY, token)
  },

  /**
   * Remove authentication token from local storage.
   */
  clearToken(): void {
    window.localStorage.removeItem(this.TOKEN_KEY)
  },

  /**
   * Get user id from local storage.
   */
  getUserId(): string | null {
    return window.localStorage.getItem(this.USER_ID_KEY)
  },

  /**
   * Set user id to local storage.
   * @param userId user identifier
   */
  setUserId(userId: string): void {
    window.localStorage.setItem(this.USER_ID_KEY, userId)
  },

  /**
   * Remove user id from local storage.
   */
  clearUserId(): void {
    window.localStorage.removeItem(this.USER_ID_KEY)
  },

  /**
   * Clear all local storage.
   */
  clearAll(): void {
    window.localStorage.clear()
  }
}