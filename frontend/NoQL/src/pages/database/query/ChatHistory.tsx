import styles from './Query.module.css'
import { Chat, ChatHistoryItem } from '../../../types/Chat.ts'
import AddRoundedIcon from '@mui/icons-material/AddRounded'
import { AxiosResponse } from 'axios'
import { LoadingButton } from '@mui/lab'

interface ChatHistoryProps {
  chatHistory: ChatHistoryItem[],
  chatHistoryLoading: boolean,
  createChat: () => Promise<AxiosResponse<Chat>>,
  createChatLoading: boolean
  openChat: (id: string, index: number) => void,
  activeChatIndex: number,
}

export function ChatHistory(
  {
    chatHistory,
    chatHistoryLoading,
    createChat,
    createChatLoading,
    openChat,
    activeChatIndex
  }: ChatHistoryProps) {

  const CreateNewChatButton =
    <LoadingButton
      onClick={createChat}
      startIcon={<AddRoundedIcon />}
      variant="outlined"
      loading={createChatLoading}
      disabled={createChatLoading}
      fullWidth
    >
      New chat
    </LoadingButton>

  return (
    <div className={styles.chatHistory}>
      {CreateNewChatButton}

      <div className={styles.chatHistoryList}>
        {chatHistoryLoading && <div>Loading</div>}
        {!chatHistoryLoading && <div>
          {
            chatHistory.map((chat: ChatHistoryItem, index: number) => {
              return (
                <div
                  onClick={() => openChat(chat.id, index)}
                  key={chat.id}
                  className={index == activeChatIndex ? styles.chatHistoryItemActive : styles.chatHistoryItem}
                >
                  {chat.name}
                </div>
              )
            })
          }
        </div>
        }
      </div>
    </div>
  )
}