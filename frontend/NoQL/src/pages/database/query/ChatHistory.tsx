import styles from './Query.module.css'
import { Button } from '@mui/material'
import { useEffect, useState } from 'react'
import { ChatDto } from '../../../types/Chat.ts'
import databaseApi from '../../../services/api/databaseApi.ts'
import AddRoundedIcon from '@mui/icons-material/AddRounded';

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

  useEffect((): void => {
    void loadChats()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  return (
    <div className={styles.chatHistory}>
      <Button
        startIcon={<AddRoundedIcon />}
        variant="outlined"
        fullWidth
        sx={{marginBottom: '1rem'}}
      >
        New chat
      </Button>
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