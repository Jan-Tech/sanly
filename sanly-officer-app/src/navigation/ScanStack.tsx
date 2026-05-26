import React from 'react'
import { createNativeStackNavigator } from '@react-navigation/native-stack'
import { ScannerScreen } from '../screens/scan/ScannerScreen'
import { ScanHistoryScreen } from '../screens/scan/ScanHistoryScreen'
import { ScanResultScreen } from '../screens/scan/ScanResultScreen'
import { Colors } from '../constants/theme'
import type { ScanStackParamList } from '../types'

const Stack = createNativeStackNavigator<ScanStackParamList>()

const headerStyle = {
  headerStyle: { backgroundColor: Colors.navy },
  headerTintColor: '#fff',
  headerTitleStyle: { color: '#fff', fontWeight: '600' as const },
  headerShadowVisible: false,
}

export function ScanStack() {
  return (
    <Stack.Navigator screenOptions={headerStyle}>
      <Stack.Screen name="Scanner" component={ScannerScreen} options={{ headerShown: false }} />
      <Stack.Screen name="ScanHistory" component={ScanHistoryScreen} options={{ title: 'Scan History' }} />
      <Stack.Screen name="ScanResultDetail" component={ScanResultScreen} options={{ title: 'Scan Result' }} />
    </Stack.Navigator>
  )
}
