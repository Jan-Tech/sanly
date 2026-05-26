import React from 'react'
import { createNativeStackNavigator } from '@react-navigation/native-stack'
import { useRole } from '../hooks/useRole'
import { PoliceActionsScreen } from '../screens/actions/PoliceActionsScreen'
import { DmvActionsScreen } from '../screens/actions/DmvActionsScreen'
import { MedicalActionsScreen } from '../screens/actions/MedicalActionsScreen'
import { CustomsActionsScreen } from '../screens/actions/CustomsActionsScreen'
import { CivilActionsScreen } from '../screens/actions/CivilActionsScreen'
import { CourtActionsScreen } from '../screens/actions/CourtActionsScreen'
import { EducationActionsScreen } from '../screens/actions/EducationActionsScreen'
import { LandActionsScreen } from '../screens/actions/LandActionsScreen'
import { TaxActionsScreen } from '../screens/actions/TaxActionsScreen'
import { SocialActionsScreen } from '../screens/actions/SocialActionsScreen'
import { AppointmentsActionsScreen } from '../screens/actions/AppointmentsActionsScreen'
import { Colors } from '../constants/theme'
import type { ActionsStackParamList, OfficerRole } from '../types'

const Stack = createNativeStackNavigator<ActionsStackParamList>()

function RoleActionsHome() {
  const { role } = useRole()
  const screens: Record<OfficerRole, React.ComponentType<any>> = {
    ROLE_POLICE:    PoliceActionsScreen,
    ROLE_DMV:       DmvActionsScreen,
    ROLE_MEDICAL:   MedicalActionsScreen,
    ROLE_CUSTOMS:   CustomsActionsScreen,
    ROLE_CIVIL:     CivilActionsScreen,
    ROLE_COURT:     CourtActionsScreen,
    ROLE_EDUCATION: EducationActionsScreen,
    ROLE_LAND:      LandActionsScreen,
    ROLE_TAX:       TaxActionsScreen,
    ROLE_SOCIAL:    SocialActionsScreen,
    ROLE_ADMIN:     AppointmentsActionsScreen,
  }
  const Component = screens[role] ?? AppointmentsActionsScreen
  return <Component />
}

const headerStyle = {
  headerStyle: { backgroundColor: Colors.navy },
  headerTintColor: '#fff',
  headerTitleStyle: { color: '#fff', fontWeight: '600' as const },
  headerShadowVisible: false,
}

export function ActionsStack() {
  return (
    <Stack.Navigator screenOptions={headerStyle}>
      <Stack.Screen name="RoleActionsHome" component={RoleActionsHome} options={{ headerShown: false }} />
    </Stack.Navigator>
  )
}
