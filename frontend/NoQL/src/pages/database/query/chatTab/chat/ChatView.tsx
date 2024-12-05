import { Chat, ChatResponse } from '../../../../../types/Chat.ts'
import { useSelector } from 'react-redux'
import { RootState } from '../../../../../state/store.ts'
import styles from './Chat.module.css'
import { SkeletonStack } from '../../../../../components/loaders/SkeletonStack.tsx'
import { ChatItem } from './ChatItem.tsx'
import React, { useEffect, useRef } from 'react'

/**
 * Chat - list of messages with results.
 */
export function ChatView() {

  const chat: Chat | null = useSelector((state: RootState) => {
    return state.chatReducer.chatNew
  })

  const chatLoading: boolean = useSelector((state: RootState) => {
    return state.chatReducer.loading
  })

  const chatWindowRef: React.RefObject<HTMLDivElement> = useRef<HTMLDivElement>(null)

  // scroll chat messages to the bottom where's the latest message
  const scrollChatToTheBottom = (): void => {
    if (chatWindowRef.current) {
      chatWindowRef.current.scrollTop = chatWindowRef.current.scrollHeight
    }
  }

  useEffect((): void => {
    scrollChatToTheBottom()
  }, [chat?.messages])

  const ChatLoadingElement =
    <div className={styles.chatWindowLoading}>
      <SkeletonStack height={50} />
    </div>

  return (
    <>
      {chatLoading
        ? ChatLoadingElement
        : <div ref={chatWindowRef} className={styles.chatWindow}>
          {chat?.messages.map((message: ChatResponse) => {
            return <ChatItem message={message} key={message.messageId} />
          })}</div>}
    </>
  )
}