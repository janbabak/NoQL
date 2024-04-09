import styles from './Query.module.css'
import { Button } from '@mui/material'
import { useState } from 'react'
import { ChatHistoryItem } from '../../../types/Chat.ts'
import AddRoundedIcon from '@mui/icons-material/AddRounded'
import chatApi from '../../../services/api/chatApi.ts'
import { AxiosResponse } from 'axios'

interface ChatHistoryProps {
  chatHistory: ChatHistoryItem[],
  chatHistoryLoading: boolean,
  refreshChatHistory: () => Promise<AxiosResponse<ChatHistoryItem[]>>
  databaseId: string,
  openChat: (id: string, index: number) => void,
  activeChatIndex: number,
}

export function ChatHistory(
  {
    chatHistory,
    chatHistoryLoading,
    refreshChatHistory,
    databaseId,
    openChat,
    activeChatIndex,
  }: ChatHistoryProps) {

  const [
    createNewChatLoading,
    setCreateNewChatLoading
  ] = useState<boolean>(false)

  async function createNewChat(): Promise<void> {
    setCreateNewChatLoading(true)
    try {
      await chatApi.createNewChat(databaseId)
      await refreshChatHistory() // TODO: is that necessary
      openChat(chatHistory[0].id, 0)
    } catch (error: unknown) {
      console.log(error) // TODO: handle
    } finally {
      setCreateNewChatLoading(false)
    }
  }

  const CreateNewChatButton =
    <Button
      onClick={createNewChat}
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
      { createNewChatLoading && <span>TODO: create loding button</span>}

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