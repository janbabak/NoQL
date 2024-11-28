import { ChatNew, ChatResponse } from '../../../../types/Chat.ts'
import { useSelector } from 'react-redux'
import { RootState } from '../../../../state/store.ts'
import styles from '../Query.module.css'
import { SkeletonStack } from '../../../../components/loaders/SkeletonStack.tsx'
import { ChatItem } from './ChatItem.tsx'

export function ChatView2() {

  const chat: ChatNew | null = useSelector((state: RootState) => {
    return state.chatReducer.chatNew
  })

  const chatLoading: boolean = useSelector((state: RootState) => {
    return state.chatReducer.loading
  })

  const ChatLoading =
    <div className={styles.chatWindowLoading}>
      <SkeletonStack height={50} />
    </div>

  return (
    <>
      {chatLoading
        ? ChatLoading
        : <div>{chat?.messages.map((message: ChatResponse) => {
          return <ChatItem message={message} key={message.messageId} />
        })}</div>
      }
    </>
  )
}