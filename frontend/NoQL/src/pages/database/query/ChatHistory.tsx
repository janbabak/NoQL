import styles from './Query.module.css'
import { Button } from '@mui/material'
import { useState } from 'react'
import { ChatDto } from '../../../types/Chat.ts'
import AddRoundedIcon from '@mui/icons-material/AddRounded'
import chatApi from '../../../services/api/chatApi.ts'
import { AxiosResponse } from 'axios'

interface ChatHistoryProps {
  chatHistory: ChatDto[],
  chatHistoryLoading: boolean,
  refreshChatHistory: () => Promise<AxiosResponse<ChatDto[]>>
  databaseId: string,
  openChat: (id: string) => void
}

export function ChatHistory(
  {
    chatHistory,
    chatHistoryLoading,
    refreshChatHistory,
    databaseId,
    openChat
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

      {chatHistoryLoading && <div>Loading</div>}
      {!chatHistoryLoading && <div>
        {
          chatHistory.map((chat: ChatDto) => {
            return (
              <div
                onClick={() => openChat(chat.id)}
                key={chat.id}
                className={styles.chatHistoryItem}
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