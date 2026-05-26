import React from 'react'
import { View, StyleSheet } from 'react-native'
import { createBottomTabNavigator } from '@react-navigation/bottom-tabs'
import { useQuery } from '@tanstack/react-query'
import { Home, LayoutGrid, FileText, Bell, User } from 'lucide-react-native'
import { DashboardScreen } from '../screens/DashboardScreen'
import { DocumentsScreen } from '../screens/DocumentsScreen'
import { NotificationsScreen } from '../screens/NotificationsScreen'
import { ServicesStack } from './ServicesStack'
import { ProfileStack } from './ProfileStack'
import { getUnreadCount } from '../api/notifications'
import { useLangStore } from '../store/langStore'
import { t } from '../i18n'
import { Colors } from '../constants/theme'
import type { MainTabParamList } from '../types'

const Tab = createBottomTabNavigator<MainTabParamList>()

function UnreadDot() {
  return <View style={styles.dot} />
}

export function MainTabs() {
  const { lang } = useLangStore()
  const { data: unreadCount } = useQuery({
    queryKey: ['unread-count'],
    queryFn: getUnreadCount,
    retry: false,
    refetchInterval: 60_000,
  })

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
        name="ServicesTab"
        component={ServicesStack}
        options={{
          title: t(lang, 'services'),
          tabBarIcon: ({ color, size }) => <LayoutGrid size={size} color={color} />,
        }}
      />
      <Tab.Screen
        name="DocumentsTab"
        component={DocumentsScreen}
        options={{
          title: t(lang, 'documents'),
          tabBarIcon: ({ color, size }) => <FileText size={size} color={color} />,
        }}
      />
      <Tab.Screen
        name="NotificationsTab"
        component={NotificationsScreen}
        options={{
          title: t(lang, 'notifications'),
          tabBarIcon: ({ color, size }) => (
            <View>
              <Bell size={size} color={color} />
              {unreadCount != null && unreadCount > 0 && <UnreadDot />}
            </View>
          ),
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
    backgroundColor: Colors.card,
    borderTopColor: Colors.border,
    borderTopWidth: StyleSheet.hairlineWidth,
    paddingBottom: 4,
    height: 56,
  },
  tabLabel: { fontSize: 10, fontWeight: '500' },
  dot: {
    position: 'absolute',
    top: -2,
    right: -4,
    width: 8,
    height: 8,
    borderRadius: 4,
    backgroundColor: Colors.error,
    borderWidth: 1.5,
    borderColor: Colors.card,
  },
})
