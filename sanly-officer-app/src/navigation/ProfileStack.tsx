import React from 'react'
import { createNativeStackNavigator } from '@react-navigation/native-stack'
import { ProfileScreen } from '../screens/profile/ProfileScreen'
import { SettingsScreen } from '../screens/profile/SettingsScreen'
import { Colors } from '../constants/theme'
import type { ProfileStackParamList } from '../types'

const Stack = createNativeStackNavigator<ProfileStackParamList>()

const headerStyle = {
  headerStyle: { backgroundColor: Colors.navy },
  headerTintColor: '#fff',
  headerTitleStyle: { color: '#fff', fontWeight: '600' as const },
  headerShadowVisible: false,
}

export function ProfileStack() {
  return (
    <Stack.Navigator screenOptions={headerStyle}>
      <Stack.Screen name="Profile" component={ProfileScreen} options={{ headerShown: false }} />
      <Stack.Screen name="Settings" component={SettingsScreen} options={{ title: 'Settings' }} />
    </Stack.Navigator>
  )
}
