import styles from './Query.module.css'
import { Button } from '@mui/material'
import { Chat, ChatHistoryItem } from '../../../types/Chat.ts'
import AddRoundedIcon from '@mui/icons-material/AddRounded'
import { AxiosResponse } from 'axios'

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
    // refreshChatHistory,
    createChat,
    createChatLoading,
    openChat,
    activeChatIndex,
  }: ChatHistoryProps) {

  const CreateNewChatButton =
    <Button
      onClick={createChat}
      startIcon={<AddRoundedIcon />}
      variant="outlined"
      fullWidth
      sx={{ marginBottom: '1rem' }}
    >
      New chat
    </Button>

  return (
    <div className={styles.chatHistory}>
      {CreateNewChatButton}
      { createChatLoading && <span>TODO: create loding button</span>}

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
  )
}