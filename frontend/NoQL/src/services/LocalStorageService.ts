export const localStorageService = {
  ACCESS_TOKEN_KEY: 'accessToken',
  REFRESH_TOKEN_KEY: 'refreshToken',
  USER_ID_KEY: 'userId',

  /**
   * Get access token from local storage.
   */
  getAccessToken(): string | null {
    return window.localStorage.getItem(this.ACCESS_TOKEN_KEY)
  },

  /**
   * Set access token to local storage.
   * @param token jwt token
   */
  setAcessToken(token: string): void {
    window.localStorage.setItem(this.ACCESS_TOKEN_KEY, token)
  },

  /**
   * Get refresh token from local storage.
   */
  getRefreshToken(): string | null {
    return window.localStorage.getItem(this.REFRESH_TOKEN_KEY)
  },

  /**
   * Set refresh token to local storage.
   * @param token jwt token
   */
  setRefreshToken(token: string): void {
    window.localStorage.setItem(this.REFRESH_TOKEN_KEY, token)
  },

  /**
   * Remove authentication token from local storage.
   */
  clearToken(): void {
    window.localStorage.removeItem(this.ACCESS_TOKEN_KEY)
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