import React from 'react'
import { StyleSheet } from 'react-native'
import { createBottomTabNavigator } from '@react-navigation/bottom-tabs'
import { Home, QrCode, Search, Activity, User } from 'lucide-react-native'
import { DashboardScreen } from '../screens/home/DashboardScreen'
import { ScanStack } from './ScanStack'
import { SearchStack } from './SearchStack'
import { ActionsStack } from './ActionsStack'
import { ProfileStack } from './ProfileStack'
import { useLangStore } from '../store/langStore'
import { useRole } from '../hooks/useRole'
import { t } from '../i18n'
import { Colors } from '../constants/theme'
import type { MainTabParamList } from '../types'

const Tab = createBottomTabNavigator<MainTabParamList>()

export function MainTabs() {
  const { lang } = useLangStore()
  const { colors } = useRole()

  return (
    <Tab.Navigator
      screenOptions={{
        headerShown: false,
        tabBarActiveTintColor: Colors.primary,
        tabBarInactiveTintColor: Colors.textMuted,
        tabBarStyle: styles.tabBar,
        tabBarLabelStyle: styles.tabLabel,
      }}
    >
      <Tab.Screen
        name="HomeTab"
        component={DashboardScreen}
        options={{
          title: t(lang, 'home'),
          tabBarIcon: ({ color, size }) => <Home size={size} color={color} />,
        }}
      />
      <Tab.Screen
        name="ScanTab"
        component={ScanStack}
        options={{
          title: t(lang, 'scan'),
          tabBarIcon: ({ color, size }) => <QrCode size={size} color={color} />,
        }}
      />
      <Tab.Screen
        name="SearchTab"
        component={SearchStack}
        options={{
          title: t(lang, 'search'),
          tabBarIcon: ({ color, size }) => <Search size={size} color={color} />,
        }}
      />
      <Tab.Screen
        name="ActionsTab"
        component={ActionsStack}
        options={{
          title: t(lang, 'actions'),
          tabBarIcon: ({ color, size }) => <Activity size={size} color={color} />,
        }}
      />
      <Tab.Screen
        name="ProfileTab"
        component={ProfileStack}
        options={{
          title: t(lang, 'profile'),
          tabBarIcon: ({ color, size }) => <User size={size} color={color} />,
        }}
      />
    </Tab.Navigator>
  )
}

const styles = StyleSheet.create({
  tabBar: {
    backgroundColor: Colors.navy,
    borderTopColor: Colors.navyLight,
    borderTopWidth: StyleSheet.hairlineWidth,
    paddingBottom: 4,
    height: 56,
  },
  tabLabel: { fontSize: 10, fontWeight: '600' },
})
