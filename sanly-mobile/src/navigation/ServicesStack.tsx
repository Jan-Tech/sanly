import React from 'react'
import { createNativeStackNavigator } from '@react-navigation/native-stack'
import { ServicesScreen } from '../screens/ServicesScreen'
import { MedicalScreen } from '../screens/MedicalScreen'
import { LicenseScreen } from '../screens/LicenseScreen'
import { VehicleScreen } from '../screens/VehicleScreen'
import { TaxScreen } from '../screens/TaxScreen'
import { BusinessScreen } from '../screens/BusinessScreen'
import { PropertyScreen } from '../screens/PropertyScreen'
import { EducationScreen } from '../screens/EducationScreen'
import { SocialScreen } from '../screens/SocialScreen'
import { PensionScreen } from '../screens/PensionScreen'
import { CustomsScreen } from '../screens/CustomsScreen'
import { CourtScreen } from '../screens/CourtScreen'
import { AppointmentsScreen } from '../screens/AppointmentsScreen'
import { BankingScreen } from '../screens/BankingScreen'
import { SignaturesScreen } from '../screens/SignaturesScreen'
import { Colors } from '../constants/theme'
import type { ServicesStackParamList } from '../types'

const Stack = createNativeStackNavigator<ServicesStackParamList>()

const headerStyle = {
  headerStyle: { backgroundColor: Colors.card },
  headerTintColor: Colors.primary,
  headerTitleStyle: { color: Colors.dark, fontWeight: '600' as const },
  headerShadowVisible: false,
}

export function ServicesStack() {
  return (
    <Stack.Navigator screenOptions={headerStyle}>
      <Stack.Screen name="ServicesList" component={ServicesScreen} options={{ headerShown: false }} />
      <Stack.Screen name="Medical" component={MedicalScreen} options={{ title: 'Medical Records' }} />
      <Stack.Screen name="License" component={LicenseScreen} options={{ title: 'Driving License' }} />
      <Stack.Screen name="Vehicle" component={VehicleScreen} options={{ title: 'Vehicles' }} />
      <Stack.Screen name="Tax" component={TaxScreen} options={{ title: 'Tax Records' }} />
      <Stack.Screen name="Business" component={BusinessScreen} options={{ title: 'Business Registry' }} />
      <Stack.Screen name="Property" component={PropertyScreen} options={{ title: 'Land & Property' }} />
      <Stack.Screen name="Education" component={EducationScreen} options={{ title: 'Education' }} />
      <Stack.Screen name="Social" component={SocialScreen} options={{ title: 'Social Benefits' }} />
      <Stack.Screen name="Pension" component={PensionScreen} options={{ title: 'Pension' }} />
      <Stack.Screen name="Customs" component={CustomsScreen} options={{ title: 'Customs' }} />
      <Stack.Screen name="Court" component={CourtScreen} options={{ title: 'Court & Fines' }} />
      <Stack.Screen name="Appointments" component={AppointmentsScreen} options={{ title: 'Appointments' }} />
      <Stack.Screen name="Banking" component={BankingScreen} options={{ title: 'Banking Consents' }} />
      <Stack.Screen name="Signatures" component={SignaturesScreen} options={{ title: 'Digital Signatures' }} />
    </Stack.Navigator>
  )
}
