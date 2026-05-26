import React from 'react'
import { View, Text, ScrollView, StyleSheet } from 'react-native'
import {
  Stethoscope, Car, Truck, Receipt, Briefcase, Home, GraduationCap,
  HeartHandshake, PiggyBank, Package, Scale, CalendarDays, FileSignature, Building2,
} from 'lucide-react-native'
import { useNavigation } from '@react-navigation/native'
import type { NativeStackNavigationProp } from '@react-navigation/native-stack'
import { useLangStore } from '../store/langStore'
import { t } from '../i18n'
import { ServiceGridItem } from '../components/ServiceGridItem'
import { Colors, Spacing } from '../constants/theme'
import type { ServicesStackParamList } from '../types'

type NavProp = NativeStackNavigationProp<ServicesStackParamList, 'Services'>

interface ServiceDef {
  id: keyof ServicesStackParamList
  icon: React.ReactNode
  labelKey: keyof ReturnType<typeof t> extends never ? string : any
  color: string
}

export function ServicesScreen() {
  const navigation = useNavigation<NavProp>()
  const { lang } = useLangStore()

  const services = [
    { id: 'Medical', icon: <Stethoscope size={24} color={Colors.purple} />, label: t(lang, 'medical'), color: Colors.purpleBg },
    { id: 'License', icon: <Car size={24} color={Colors.primary} />, label: t(lang, 'license'), color: Colors.primaryBg },
    { id: 'Vehicle', icon: <Truck size={24} color={Colors.orange} />, label: t(lang, 'vehicles'), color: Colors.orangeBg },
    { id: 'Tax', icon: <Receipt size={24} color="#059669" />, label: t(lang, 'tax'), color: '#D1FAE5' },
    { id: 'Business', icon: <Briefcase size={24} color="#F59E0B" />, label: t(lang, 'business'), color: Colors.warningBg },
    { id: 'Property', icon: <Home size={24} color="#10B981" />, label: t(lang, 'property'), color: Colors.successBg },
    { id: 'Education', icon: <GraduationCap size={24} color={Colors.info} />, label: t(lang, 'education'), color: Colors.infoBg },
    { id: 'Social', icon: <HeartHandshake size={24} color="#EC4899" />, label: t(lang, 'benefits'), color: '#FCE7F3' },
    { id: 'Pension', icon: <PiggyBank size={24} color="#0EA5E9" />, label: t(lang, 'pension'), color: '#E0F2FE' },
    { id: 'Customs', icon: <Package size={24} color="#78716C" />, label: t(lang, 'customs'), color: '#F5F5F4' },
    { id: 'Court', icon: <Scale size={24} color={Colors.error} />, label: t(lang, 'court'), color: Colors.errorBg },
    { id: 'Appointments', icon: <CalendarDays size={24} color={Colors.primary} />, label: t(lang, 'appointments'), color: Colors.primaryBg },
    { id: 'Signatures', icon: <FileSignature size={24} color="#6366F1" />, label: t(lang, 'signatures'), color: '#EEF2FF' },
    { id: 'Banking', icon: <Building2 size={24} color="#DC2626" />, label: t(lang, 'banking'), color: Colors.errorBg },
  ] as const

  return (
    <ScrollView style={styles.scroll} contentContainerStyle={styles.content}>
      <Text style={styles.subtitle}>
        {lang === 'en' ? 'All government services in one place'
          : lang === 'tk' ? 'Ähli döwlet hyzmatlary bir ýerde'
          : 'Все государственные услуги в одном месте'}
      </Text>
      <View style={styles.grid}>
        {services.map(svc => (
          <ServiceGridItem
            key={svc.id}
            icon={svc.icon}
            label={svc.label}
            color={svc.color}
            onPress={() => navigation.navigate(svc.id as keyof ServicesStackParamList)}
          />
        ))}
      </View>
    </ScrollView>
  )
}

const styles = StyleSheet.create({
  scroll: { flex: 1, backgroundColor: Colors.background },
  content: { padding: Spacing.lg, paddingBottom: 32 },
  subtitle: { fontSize: 13, color: Colors.textSecondary, marginBottom: Spacing.xl },
  grid: { flexDirection: 'row', flexWrap: 'wrap' },
})
