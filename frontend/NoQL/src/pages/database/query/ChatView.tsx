import SyntaxHighlighter from 'react-syntax-highlighter'
import { vs2015 as theme } from 'react-syntax-highlighter/dist/cjs/styles/hljs'
import React, { useEffect, useRef } from 'react'
import styles from './Query.module.css'
import { Chat, ChatQueryWithResponse, LLMResult } from '../../../types/Chat.ts'
import { useSelector } from 'react-redux'
import { RootState } from '../../../state/store.ts'
import { LoadersStack } from '../../../components/loaders/LoadersStack.tsx'

interface UsersQueryProps {
  query: string
}

interface ModelsResponse {
  chatQueryResult: LLMResult
}

function ModelsResponse({ chatQueryResult }: ModelsResponse) {
  return (
    chatQueryResult?.databaseQuery != null
      ? <SyntaxHighlighter
        style={theme}
        language="SQL"
        customStyle={{ borderRadius: '0.25rem', margin: '0.25rem 0 1rem 0' }}
      >
        {chatQueryResult.databaseQuery}
      </SyntaxHighlighter>
      : <div></div>
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
      <LoadersStack height={50} />
    </div>

  return (
    <>
      {chatLoading
        ? ChatLoading
        : <div ref={chatWindowRef} className={styles.chatWindow}>
          {
            chat?.messages.length == 0
              ? <div className={styles.startChatting}>Start chatting...</div>
              : chat?.messages.map((message: ChatQueryWithResponse) => {
                return (
                  <div key={message.id}>
                    <UsersQuery query={message.nlQuery} />
                    <ModelsResponse chatQueryResult={message.llmResult} />
                  </div>
                )
              })
          }
        </div>
      }
    </>
  )
}