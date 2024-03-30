import { SecondaryNavbarItem } from '../../components/secondaryNavigation/SecondaryNavbar.types.ts'
import { SecondaryNavbar } from '../../components/secondaryNavigation/SecondaryNavbar.tsx'
import QuestionAnswerRoundedIcon from '@mui/icons-material/QuestionAnswerRounded'
import BackupTableRoundedIcon from '@mui/icons-material/BackupTableRounded'
import SettingsRoundedIcon from '@mui/icons-material/SettingsRounded'
import { DatabaseSettings } from './settings/DatabaseSettings.tsx'
import { DatabaseStructureSubpage } from './structure/DatabaseStructureSubpage.tsx'
import { QueryDatabase } from './query/QueryDatabase.tsx'
import { useEffect, useState } from 'react'
import { Database } from '../../types/Database.ts'
import databaseApi from '../../services/api/databaseApi.ts'
import { useParams } from 'react-router'

export function DatabasePage() {

  const { id } = useParams<string>()

  const [
    database,
    setDatabase
  ] = useState<Database | null>(null)

  const [
    databaseLoading,
    setDatabaseLoading
  ] = useState<boolean>(false)

  useEffect((): void => {
    void loadDatabase()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  // load database from API
  async function loadDatabase(): Promise<void> {
    setDatabaseLoading(true)
    try {
      const response = await databaseApi.getById(id || '')
      setDatabase(response.data)
    } catch (error: unknown) {
      console.log(error) // TODO: handle
    } finally {
      setDatabaseLoading(false)
    }
  }

  const subpages: SecondaryNavbarItem[] = [
    {
      label: 'Query',
      buttonIcon: <QuestionAnswerRoundedIcon />,
      component:
        <QueryDatabase
          databaseId={id || ''}
          database={database}
          databaseLoading={databaseLoading}
        />
    },
    {
      label: 'Structure',
      buttonIcon: <BackupTableRoundedIcon />,
      component:
        <DatabaseStructureSubpage
          databaseId={id || ''}
          database={database}
          databaseLoading={databaseLoading}
        />
    },
    {
      label: 'Settings',
      buttonIcon: <SettingsRoundedIcon />,
      component: <DatabaseSettings />
    }
  ]

  return <SecondaryNavbar subpages={subpages} />
}