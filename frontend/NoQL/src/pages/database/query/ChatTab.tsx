import { CHAT_TAB } from './Constants.ts'
import styles from './Query.module.css'
import { TextField } from '@mui/material'
import { LoadingButton } from '@mui/lab'
import SendRoundedIcon from '@mui/icons-material/SendRounded'
import React, { useEffect, useRef, useState } from 'react'
import databaseApi from '../../../services/api/databaseApi.ts'
import { QueryResponse } from '../../../types/Query.ts'
import { Result } from './Result.tsx'
import { ChatHistory } from './ChatHistory.tsx'
import { ChatHistoryItem, Chat } from '../../../types/Chat.ts'
import { ChatView } from './ChatView.tsx'
import { AxiosResponse } from 'axios'
import { useDispatch, useSelector } from 'react-redux'
import { AppDispatch, RootState } from '../../../state/store.ts'
import { addMessage, addMessageAndChangeName, fetchChat, setChatToNull } from '../../../state/chat/chatSlice.ts'
import { fetchChatHistory, renameChat } from '../../../state/chat/chatHistorySlice.ts'

interface ChatTabProps {
  databaseId: string,
  tab: number,
  editQueryInConsole: (query: string) => void,
}

export function ChatTab({ databaseId, tab, editQueryInConsole }: ChatTabProps) {

  const NEW_CHAT_NAME: string = 'New chat'
  const CHAT_NAME_MAX_LENGTH: number = 32

  const dispatch: AppDispatch = useDispatch()

  const activeChatIndexRedux: number = useSelector((state: RootState) => {
    return state.chatHistoryReducer.activeChatIndex
  })

  const chat: Chat | null = useSelector((state: RootState) => {
    return state.chatReducer.chat
  })

  const chatHistory: ChatHistoryItem[] = useSelector((state: RootState) => {
    return state.chatHistoryReducer.chatHistory
  })

  const [
    queryResult,
    setQueryResult
  ] = useState<QueryResponse | null>(null)

  const [
    queryLoading,
    setQueryLoading
  ] = useState<boolean>(false)

  const [
    pageLoading,
    setPageLoading
  ] = useState<boolean>(false)

  const [
    page,
    setPage
  ] = useState<number>(0)

  const [
    pageSize,
    setPageSize
  ] = useState<number>(10)

  const naturalLanguageQuery: React.MutableRefObject<string> = useRef<string>('')

  // TODO: fix multiple calls
  useEffect((): void => {
    void loadChatHistoryAndChatAndResult(0)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  /**
   * Load chat history, then active chat content, then query the chat for the result.
   * Creates new chat if there isn't any. // TODO that
   */
  async function loadChatHistoryAndChatAndResult(chatIndex: number): Promise<void> {
    // chat history
    let result = await dispatch(fetchChatHistory(databaseId))

    // chat
    // @ts-ignore
    if (result.payload.length > chatIndex && chatIndex >= 0) {
      // @ts-ignore
      result = await dispatch(fetchChat(result.payload[chatIndex].id))
    } else {
      setQueryResult(null)
      dispatch(setChatToNull())
      return
    }

    // chat result
    // @ts-ignore
    if (result.payload.messages.length > 0) {
      // @ts-ignore
      loadQueryLanguageQuery(result.payload.id)
    } else {
      setQueryResult(null)
    }

    console.log()
  }

  /**
   * Query model using the natural language chat.
   */
  async function queryChat(): Promise<void> {
    setPage(0)
    setQueryLoading(true)
    try {
      const response: AxiosResponse<QueryResponse> = await databaseApi.queryChat(
        databaseId, {
          chatId: chatHistory[activeChatIndexRedux].id,
          // @ts-ignore
          query: naturalLanguageQuery.current.value
        },
        pageSize)

      // @ts-ignore
      naturalLanguageQuery.current.value = ''
      setQueryResult(response.data)

      // update chat name if it's still the autogenerated name
      if (chat?.name == NEW_CHAT_NAME) {
        const updatedName: string = response.data.chatQueryWithResponse.query.length < CHAT_NAME_MAX_LENGTH
          ? response.data.chatQueryWithResponse.query
          : response.data.chatQueryWithResponse.query.substring(0, CHAT_NAME_MAX_LENGTH)

        dispatch(addMessageAndChangeName({
          message: response.data.chatQueryWithResponse,
          name: updatedName
        }))
        // @ts-ignore
        dispatch(renameChat({
          index: activeChatIndexRedux,
          name: updatedName
        }))
      } else {
        dispatch(addMessage(response.data.chatQueryWithResponse))
      }
    } catch (error: unknown) {
      console.log(error) // TODO: handle
    } finally {
      setQueryLoading(false)
    }
  }

  /**
   * Load result when page is changed.
   * @param page new page number (first page index is 0)
   * @param pageSize number of items in the page
   */
  async function onPageChange(page: number, pageSize: number): Promise<void> {
    setPageSize(pageSize)
    setPage(page)

    setPageLoading(true)
    try {
      const response: AxiosResponse<QueryResponse> =
        await databaseApi.loadChatResult(databaseId, chat?.id || '', page, pageSize)

      setQueryResult(response.data)
    } catch (error: unknown) {
      console.log(error) // TODO: handles
    } finally {
      setPageLoading(false)
    }
  }

  /**
   * Fetch result of query language query.
   * @param chatId identifier
   */
  async function loadQueryLanguageQuery(chatId: string): Promise<void> {
    setQueryLoading(true)
    try {
      const response: AxiosResponse<QueryResponse> =
        await databaseApi.loadChatResult(databaseId, chatId || '', 0, pageSize)

      setPage(0)
      setQueryResult(response.data)
    } catch (error: unknown) {
      console.log(error) // TODO: handles
    } finally {
      setQueryLoading(false)
    }
  }

  /**
   * Load query result of a chat - load it's content and query response.
   * @param chatId chat id
   */
  async function loadChatResult(chatId: string): Promise<void> {
    const result = await dispatch(fetchChat(chatId)) // TODO: move to chat history
    // @ts-expect-error
    if (result.payload.messages.length > 0) {
      await loadQueryLanguageQuery(chatId)
    } else {
      setQueryResult(null)
    }
  }

  return (
    <div
      role="tabpanel"
      hidden={tab != CHAT_TAB}
      className={styles.chatTab}
    >
      <div className={styles.chatTabContainer}>
        <ChatHistory
          loadChatResult={loadChatResult}
          loadChatHistoryAndChatAndResult={loadChatHistoryAndChatAndResult}
          databaseId={databaseId}
          setQueryResult={setQueryResult}
        />

        <div className={styles.chatWithInput}>

          <ChatView />

          <div className={styles.chatInputContainer}>
            <TextField
              id="query"
              label="Query"
              variant="standard"
              inputRef={naturalLanguageQuery}
              fullWidth
            />

            <LoadingButton
              loading={queryLoading}
              variant="contained"
              endIcon={<SendRoundedIcon />}
              onClick={queryChat}
            >
              Query
            </LoadingButton>
          </div>
        </div>
      </div>

      <Result
        queryResponse={queryResult}
        editQueryInConsole={editQueryInConsole}
        showEditInConsoleButton={true}
        page={page}
        pageSize={pageSize}
        setPageSize={setPageSize}
        totalCount={queryResult?.totalCount || 1}
        onPageChange={onPageChange}
        loading={queryLoading || pageLoading}
      />

    </div>
  )
}