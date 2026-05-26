import React from 'react'
import { createNativeStackNavigator } from '@react-navigation/native-stack'
import { SearchScreen } from '../screens/search/SearchScreen'
import { CitizenDetailScreen } from '../screens/search/CitizenDetailScreen'
import { Colors } from '../constants/theme'
import type { SearchStackParamList } from '../types'

const Stack = createNativeStackNavigator<SearchStackParamList>()

const headerStyle = {
  headerStyle: { backgroundColor: Colors.navy },
  headerTintColor: '#fff',
  headerTitleStyle: { color: '#fff', fontWeight: '600' as const },
  headerShadowVisible: false,
}

export function SearchStack() {
  return (
    <Stack.Navigator screenOptions={headerStyle}>
      <Stack.Screen name="Search" component={SearchScreen} options={{ headerShown: false }} />
      <Stack.Screen name="CitizenDetail" component={CitizenDetailScreen} options={{ title: 'Citizen Details' }} />
    </Stack.Navigator>
  )
}
