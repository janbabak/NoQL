import { ChatNew, ChatResponse } from '../../../types/Chat.ts'
import { useSelector } from 'react-redux'
import { RootState } from '../../../state/store.ts'

export function ChatView2() {

  const chat: ChatNew | null = useSelector((state: RootState) => {
    return state.chatReducer.chatNew
  })

  const chatLoading: boolean = useSelector((state: RootState) => {
    return state.chatReducer.loading
  })

  return (
    <>
      {chatLoading
        ? <div>Loading</div>
        : <div>{chat?.messages.map((message: ChatResponse) => {
          return (
            <div key={message.messageId}>
              <div>{message.nlQuery}</div>
              <div>{message.dbQuery}</div>
            </div>
          )
        })}</div>
      }
    </>
  )
}