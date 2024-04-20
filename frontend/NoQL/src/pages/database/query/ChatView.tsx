import SyntaxHighlighter from 'react-syntax-highlighter'
import { vs2015 as theme } from 'react-syntax-highlighter/dist/cjs/styles/hljs'
import React, { useEffect, useRef } from 'react'
import styles from './Query.module.css'
import { Chat, ChatQueryWithResponse } from '../../../types/Chat.ts'
import { CircularProgress } from '@mui/material'
import { useSelector } from 'react-redux'
import { RootState } from '../../../state/store.ts'

interface UsersQueryProps {
  query: string
}

interface ModelsResponse {
  response: string
}

function ModelsResponse({ response }: ModelsResponse) {
  return (
    <SyntaxHighlighter
      style={theme}
      language="SQL"
      customStyle={{ borderRadius: '0.25rem', margin: '0.25rem 0 1rem 0' }}
    >
      {response}
    </SyntaxHighlighter>
  )
}

function UsersQuery({ query }: UsersQueryProps) {
  return (
    <>
      <span style={{ fontWeight: 'bold' }}>You: </span>
      {query}
    </>
  )
}

export function ChatView() {

  const chat: Chat | null = useSelector((state: RootState) => {
    return state.chatReducer.chat
  })

  const chatLoading: boolean = useSelector((state: RootState) => {
    return state.chatReducer.loading
  })

  const chatWindowRef: React.RefObject<HTMLDivElement> = useRef<HTMLDivElement>(null)

  const scrollChatToTheBottom = (): void => {
    if (chatWindowRef.current) {
      chatWindowRef.current.scrollTop = chatWindowRef.current.scrollHeight
    }
  }

  useEffect((): void => {
    scrollChatToTheBottom()
  }, [chat?.messages])

  const ChatLoading =
    <div className={styles.chatWindowLoading}>
      <CircularProgress />
    </div>

  return (
    <>
      {chatLoading && ChatLoading}

      {!chatLoading &&
        <div ref={chatWindowRef} className={styles.chatWindow}>
          {
            chat?.messages.length == 0
              ? <div className={styles.startChatting}>Start chatting...</div>
              : chat?.messages.map((message: ChatQueryWithResponse) => {
                return (
                  <div key={message.id}>
                    <UsersQuery query={message.query} />
                    <ModelsResponse response={message.response} />
                  </div>
                )
              })
          }
        </div>
      }
    </>
  )
}