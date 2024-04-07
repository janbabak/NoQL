import styles from './Query.module.css'
import { Button } from '@mui/material'
import { useEffect, useState } from 'react'
import { ChatDto } from '../../../types/Chat.ts'
import databaseApi from '../../../services/api/databaseApi.ts'
import AddRoundedIcon from '@mui/icons-material/AddRounded'
import chatApi from '../../../services/api/chatApi.ts'

interface ChatHistoryProps {
  databaseId: string,
  openChat: (id: string) => void
}

export function ChatHistory({ databaseId, openChat }: ChatHistoryProps) {

  const [
    chats,
    setChats
  ] = useState<ChatDto[]>([])

  const [
    chatsLoading,
    setChatsLoading
  ] = useState<boolean>(false)

  const [
    createNewChatLoading,
    setCreateNewChatLoading
  ] = useState<boolean>(false)

  async function loadChats(): Promise<void> {
    setChatsLoading(true)
    try {
      const response = await databaseApi.getChatsFromDatabase(databaseId)
      setChats(response.data)
    } catch (error: unknown) {
      console.log(error) // TODO: handle
    } finally {
      setChatsLoading(false)
    }
  }

  async function createNewChat(): Promise<void> {
    setCreateNewChatLoading(true)
    try {
      await chatApi.createNewChat(databaseId)
      await loadChats() // TODO: is that necessary
    } catch (error: unknown) {
      console.log(error) // TODO: handle
    } finally {
      setCreateNewChatLoading(false)
    }
  }

  useEffect((): void => {
    void loadChats()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

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

      {chatsLoading && <div>Loading</div>}
      {!chatsLoading && <div>
        {
          chats.map((chat: ChatDto) => {
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