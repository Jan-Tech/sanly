import React from 'react'
import { createNativeStackNavigator } from '@react-navigation/native-stack'
import { ProfileScreen } from '../screens/ProfileScreen'
import { IdentityScreen } from '../screens/IdentityScreen'
import { AccessLogScreen } from '../screens/AccessLogScreen'
import { SessionsScreen } from '../screens/SessionsScreen'
import { SettingsScreen } from '../screens/SettingsScreen'
import { Colors } from '../constants/theme'
import type { ProfileStackParamList } from '../types'

const Stack = createNativeStackNavigator<ProfileStackParamList>()

const headerStyle = {
  headerStyle: { backgroundColor: Colors.card },
  headerTintColor: Colors.primary,
  headerTitleStyle: { color: Colors.dark, fontWeight: '600' as const },
  headerShadowVisible: false,
}

export function ProfileStack() {
  return (
    <Stack.Navigator screenOptions={headerStyle}>
      <Stack.Screen name="Profile" component={ProfileScreen} options={{ headerShown: false }} />
      <Stack.Screen name="Identity" component={IdentityScreen} options={{ title: 'Identity' }} />
      <Stack.Screen name="AccessLog" component={AccessLogScreen} options={{ title: 'Access Log' }} />
      <Stack.Screen name="Sessions" component={SessionsScreen} options={{ title: 'Active Sessions' }} />
      <Stack.Screen name="Settings" component={SettingsScreen} options={{ title: 'Settings' }} />
    </Stack.Navigator>
  )
}
