import SyntaxHighlighter from 'react-syntax-highlighter'
import { vs2015 as theme } from 'react-syntax-highlighter/dist/cjs/styles/hljs'
import React, { useEffect, useRef } from 'react'
import styles from './Query.module.css'
import { ChatFromApi, MessageWithResponse } from '../../../types/Chat.ts'

interface ChatViewProps {
  chat: ChatFromApi | null,
  chatLoading: boolean,
}

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

export function ChatView({ chat, chatLoading }: ChatViewProps) {

  const chatWindowRef: React.RefObject<HTMLDivElement> = useRef<HTMLDivElement>(null)

  const scrollChatToTheBottom = (): void => {
    if (chatWindowRef.current) {
      chatWindowRef.current.scrollTop = chatWindowRef.current.scrollHeight
    }
  }

  useEffect((): void => {
    scrollChatToTheBottom()
  }, [chat?.messages])

  return (
    <div ref={chatWindowRef} className={styles.chatWindow}>
      {
        chat?.messages.map((message: MessageWithResponse) => {
          return (
            <div key={message.id}>
              <UsersQuery query={message.message} />
              <ModelsResponse response={message.response} />
            </div>
          )
        })
      }
    </div>
  )
}