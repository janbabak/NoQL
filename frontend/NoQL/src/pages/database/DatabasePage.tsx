import { SecondaryNavbarItem } from '../../components/secondaryNavigation/SecondaryNavbar.types.ts'
import { SecondaryNavbar } from '../../components/secondaryNavigation/SecondaryNavbar.tsx'
import QuestionAnswerRoundedIcon from '@mui/icons-material/QuestionAnswerRounded';
import BackupTableRoundedIcon from '@mui/icons-material/BackupTableRounded';
import SettingsRoundedIcon from '@mui/icons-material/SettingsRounded';
import { DatabaseSettings } from './settings/DatabaseSettings.tsx'
import { DatabaseStructure } from './structure/DatabaseStructure.tsx'
import { QueryDatabase } from './query/QueryDatabase.tsx'

export function DatabasePage() {

  const subpages: SecondaryNavbarItem[] = [
    {
      label: "Query",
      component: <QueryDatabase />,
      buttonIcon: <QuestionAnswerRoundedIcon />,
    },
    {
      label: "Structure",
      component: <DatabaseStructure />,
      buttonIcon: <BackupTableRoundedIcon />,
    },
    {
      label: "Settings",
      component: <DatabaseSettings />,
      buttonIcon: <SettingsRoundedIcon />
    }
  ]

  return <SecondaryNavbar subpages={subpages} />
}