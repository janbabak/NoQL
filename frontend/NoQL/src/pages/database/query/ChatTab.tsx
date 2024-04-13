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
import chatApi from '../../../services/api/chatApi.ts'
import { AxiosResponse } from 'axios'

interface ChatTabProps {
  databaseId: string,
  tab: number,
  editQueryInConsole: (query: string) => void,
}

export function ChatTab({ databaseId, tab, editQueryInConsole }: ChatTabProps) {

  const NEW_CHAT_NAME: string = 'New chat'
  const CHAT_NAME_MAX_LENGTH: number = 32

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
    chat,
    setChat
  ] = useState<Chat | null>(null)

  const [
    chatLoading,
    setChatLoading
  ] = useState<boolean>(false)

  const [
    chatHistory,
    setChatHistory
  ] = useState<ChatHistoryItem[]>([])

  const [
    chatHistoryLoading,
    setChatHistoryLoading
  ] = useState<boolean>(false)

  const [
    page,
    setPage
  ] = useState<number>(0)

  const [
    pageSize,
    setPageSize
  ] = useState<number>(10)

  const [
    activeChatIndex,
    setActiveChatIndex
  ] = useState<number>(0)

  const [
    createNewChatLoading,
    setCreateNewChatLoading
  ] = useState<boolean>(false)

  const naturalLanguageQuery: React.MutableRefObject<string> = useRef<string>('')

  /**
   * Creates new chat
   */
  async function createNewChat(): Promise<AxiosResponse<Chat>> {
    setCreateNewChatLoading(true)
    try {
      const response: AxiosResponse<Chat> = await chatApi.createNewChat(databaseId)
      setCreateNewChatLoading(false)

      // insert newly created chat at the fist place
      setChatHistory([
        { id: response.data.id, name: response.data.name },
        ...chatHistory
      ])
      // open newly created chat
      setChat(response.data)
      setActiveChatIndex(0)
      setQueryResult(null)
      return response
    } catch (error: unknown) {
      console.log(error) // TODO: handle
      return Promise.reject()
    } finally {
      setCreateNewChatLoading(false)
    }
  }

  /**
   * Delete chat and refresh chat history
   * @param chatId identifier
   */
  async function deleteChat(chatId: string): Promise<void> {
    await chatApi.deleteChat(chatId)
    await loadChatsHistory()
  }

  async function renameChat(chatId: string, newName: string): Promise<void> {
    await chatApi.renameChat(chatId, newName)
    await loadChatsHistory()
  }

  // TODO: fix multiple calls
  /**
   * Load chat history, then active chat content, then query the chat for the result.
   * Creates new chat if there isn't any.
   */
  useEffect((): void => {
    loadChatsHistory()
      .then(async (response: AxiosResponse<ChatHistoryItem[]> | undefined) => {
        if (response && response.data.length > 0) {
          return loadChat(response.data[0].id)
        } else {
          return createNewChat()
        }
      }).then((response: AxiosResponse<Chat> | undefined): void => {
        // if there are some messages in the chat execute the query response from the last message
        if (response && response.data.messages.length > 0) {
          void loadQueryLanguageQuery(response.data.messages[response.data.messages.length - 1].response)
        }
    })
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  /**
   * Load one chat - its messages.
   * @param id chat it
   */
  async function loadChat(id: string): Promise<AxiosResponse<Chat>> {
    setChatLoading(true)
    try {
      const response: AxiosResponse<Chat> = await chatApi.getById(id)
      setChat(response.data)
      return response
    } catch (error: unknown) {
      console.log(error) // TODO: handle
      return Promise.reject()
    } finally {
      setChatLoading(false)
    }
  }

  /**
   * Load history of chats.
   */
  async function loadChatsHistory(): Promise<AxiosResponse<ChatHistoryItem[]>> {
    setChatHistoryLoading(true)
    try {
      const response: AxiosResponse<ChatHistoryItem[]> = await databaseApi.getChatHistoryByDatabaseId(databaseId)
      setChatHistory(response.data)
      return response
    } catch (error: unknown) {
      console.log(error) // TODO: handle
      return Promise.reject(error)
    } finally {
      setChatHistoryLoading(false)
    }
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
          chatId: chatHistory[activeChatIndex].id,
          // eslint-disable-next-line @typescript-eslint/ban-ts-comment
          // @ts-ignore
          query: naturalLanguageQuery.current.value
        },
        pageSize)

      // eslint-disable-next-line @typescript-eslint/ban-ts-comment
      // @ts-ignore
      naturalLanguageQuery.current.value = ''
      setQueryResult(response.data)

      // update chat name if it's still the autogenerated name
      if (chat?.name == NEW_CHAT_NAME) {
        const updatedName: string = response.data.chatQueryWithResponse.query.length < CHAT_NAME_MAX_LENGTH
          ? response.data.chatQueryWithResponse.query
          : response.data.chatQueryWithResponse.query.substring(0, CHAT_NAME_MAX_LENGTH)

        setChat({
          ...chat,
          messages: [...chat?.messages || [], response.data.chatQueryWithResponse],
          name: updatedName
        })
        const newChatHistory: ChatHistoryItem[] = chatHistory
        newChatHistory[0].name = updatedName
        setChatHistory(newChatHistory)
      } else {
        setChat({
          ...chat,
          messages: [...chat?.messages || [], response.data.chatQueryWithResponse]
        } as Chat)
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
      const response: AxiosResponse<QueryResponse> = await databaseApi.queryQueryLanguageQuery(
        databaseId,
        chat?.messages[chat?.messages.length - 1].response || '',
        page,
        pageSize)

      setQueryResult(response.data)
    } catch (error: unknown) {
      console.log(error) // TODO: handles
    } finally {
      setPageLoading(false)
    }
  }

  /**
   * Fetch result of query language query.
   * @param query in query language
   */
  async function loadQueryLanguageQuery(query: string): Promise<void> {
    setQueryLoading(true)
    try {
      const response: AxiosResponse<QueryResponse> = await databaseApi.queryQueryLanguageQuery(
        databaseId, query, 0, pageSize)
      setPage(0)
      setQueryResult(response.data)
    } catch (error: unknown) {
      console.log(error) // TODO: handles
    } finally {
      setQueryLoading(false)
    }
  }

  /**
   * Open chat - load it's content and query response.
   * @param id chat id
   * @param index index in the chat history
   */
  async function openChat(id: string, index: number): Promise<void> {
    const response: AxiosResponse<Chat> = await loadChat(id)
    setActiveChatIndex(index)

    // if chat contains some messages, execute them and load the result
    if (response.data.messages.length > 0) {
      await loadQueryLanguageQuery(response.data.messages[response.data.messages.length - 1].response)
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
          chatHistory={chatHistory}
          chatHistoryLoading={chatHistoryLoading}
          createChat={createNewChat}
          createChatLoading={createNewChatLoading}
          openChat={openChat}
          reallyDeleteChat={deleteChat}
          renameChat={renameChat}
          activeChatIndex={activeChatIndex}
        />

        <div className={styles.chatWithInput}>

          <ChatView chat={chat} chatLoading={chatLoading} />

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