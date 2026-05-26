import React, { useState, useEffect } from 'react'
import { View, Text, TouchableOpacity, StyleSheet, Alert, ActivityIndicator } from 'react-native'
import { CameraView, useCameraPermissions } from 'expo-camera'
import { useNavigation } from '@react-navigation/native'
import type { NativeStackNavigationProp } from '@react-navigation/native-stack'
import { History, X, Zap } from 'lucide-react-native'
import { useLangStore } from '../../store/langStore'
import { useScanner } from '../../hooks/useScanner'
import { parseCode } from '../../utils/codeParser'
import { getCitizen } from '../../api/registry'
import { verifyLicense } from '../../api/dmv'
import { verifyDiploma } from '../../api/education'
import { verifyProperty } from '../../api/land'
import { getAppointmentByCode } from '../../api/appointments'
import { getFineByCode } from '../../api/court'
import { t } from '../../i18n'
import { Colors, Spacing, Radius } from '../../constants/theme'
import type { ScanStackParamList } from '../../types'

type NavProp = NativeStackNavigationProp<ScanStackParamList>

async function fetchForCode(type: string, value: string) {
  switch (type) {
    case 'NIN':         return getCitizen(value)
    case 'LICENSE':     return verifyLicense(value)
    case 'DIPLOMA':     return verifyDiploma(value)
    case 'CADASTRAL':   return verifyProperty(value)
    case 'APPOINTMENT': return getAppointmentByCode(value)
    case 'FINE':        return getFineByCode(value)
    default: return null
  }
}

export function ScannerScreen() {
  const navigation = useNavigation<NavProp>()
  const { lang } = useLangStore()
  const [permission, requestPermission] = useCameraPermissions()
  const { scanned, processBarcode, resetScanner } = useScanner()
  const [loading, setLoading] = useState(false)

  async function handleBarcode({ data }: { data: string }) {
    const parsed = await processBarcode(data)
    if (!parsed) return

    setLoading(true)
    try {
      const result = await fetchForCode(parsed.type, parsed.value)
      navigation.navigate('ScanResult', { code: parsed.rawCode, type: parsed.type, data: result })
    } catch (e: any) {
      const errMsg = e?.response?.status === 404 ? 'Record not found for this code.' : 'Failed to fetch record. Check connectivity.'
      Alert.alert('Scan Error', errMsg, [{ text: 'OK', onPress: resetScanner }])
    } finally {
      setLoading(false)
    }
  }

  if (!permission) return <View style={styles.center}><ActivityIndicator color={Colors.primary} /></View>

  if (!permission.granted) {
    return (
      <View style={styles.center}>
        <Text style={styles.permText}>{t(lang, 'scanPermissionDenied')}</Text>
        <TouchableOpacity style={styles.permBtn} onPress={requestPermission}>
          <Text style={styles.permBtnText}>{t(lang, 'grantPermission')}</Text>
        </TouchableOpacity>
      </View>
    )
  }

  return (
    <View style={{ flex: 1, backgroundColor: '#000' }}>
      <CameraView
        style={StyleSheet.absoluteFill}
        barcodeScannerSettings={{ barcodeTypes: ['qr', 'code128', 'code39', 'pdf417', 'datamatrix'] }}
        onBarcodeScanned={scanned ? undefined : handleBarcode}
      />

      {/* Dark overlay with cutout */}
      <View style={styles.overlay}>
        <View style={styles.topOverlay} />
        <View style={styles.middleRow}>
          <View style={styles.sideOverlay} />
          <View style={styles.scanFrame}>
            {/* Corner marks */}
            <View style={[styles.corner, styles.topLeft]} />
            <View style={[styles.corner, styles.topRight]} />
            <View style={[styles.corner, styles.bottomLeft]} />
            <View style={[styles.corner, styles.bottomRight]} />
            {loading && (
              <View style={styles.scanLoading}>
                <ActivityIndicator size="large" color="#fff" />
                <Text style={styles.scanLoadingText}>Fetching record...</Text>
              </View>
            )}
          </View>
          <View style={styles.sideOverlay} />
        </View>
        <View style={styles.bottomOverlay}>
          <Text style={styles.hint}>{t(lang, 'scanCode')}</Text>
          <Text style={styles.hintSub}>QR · Code128 · Code39 · PDF417</Text>

          <View style={styles.btnRow}>
            <TouchableOpacity
              style={styles.btn}
              onPress={() => navigation.navigate('ScanHistory')}
            >
              <History size={20} color="#fff" />
              <Text style={styles.btnText}>{t(lang, 'scanHistory')}</Text>
            </TouchableOpacity>

            {scanned && (
              <TouchableOpacity style={[styles.btn, styles.resetBtn]} onPress={resetScanner}>
                <Zap size={20} color="#fff" />
                <Text style={styles.btnText}>{t(lang, 'scanAgain')}</Text>
              </TouchableOpacity>
            )}
          </View>
        </View>
      </View>
    </View>
  )
}

const FRAME = 260
const CORNER = 24
const THICK = 3

const styles = StyleSheet.create({
  center: { flex: 1, justifyContent: 'center', alignItems: 'center', backgroundColor: Colors.background, padding: Spacing.xl },
  permText: { fontSize: 15, color: Colors.textSecondary, textAlign: 'center', marginBottom: Spacing.lg },
  permBtn: { backgroundColor: Colors.primary, borderRadius: Radius.md, paddingHorizontal: Spacing.xl, paddingVertical: Spacing.md },
  permBtnText: { color: '#fff', fontWeight: '600' },
  overlay: { ...StyleSheet.absoluteFillObject },
  topOverlay: { flex: 1, backgroundColor: 'rgba(0,0,0,0.65)' },
  middleRow: { flexDirection: 'row', height: FRAME },
  sideOverlay: { flex: 1, backgroundColor: 'rgba(0,0,0,0.65)' },
  scanFrame: { width: FRAME, height: FRAME, justifyContent: 'center', alignItems: 'center' },
  bottomOverlay: { flex: 1.2, backgroundColor: 'rgba(0,0,0,0.65)', alignItems: 'center', paddingTop: Spacing.xl },
  corner: { position: 'absolute', width: CORNER, height: CORNER, borderColor: Colors.primary },
  topLeft:    { top: 0, left: 0,  borderTopWidth: THICK, borderLeftWidth: THICK },
  topRight:   { top: 0, right: 0, borderTopWidth: THICK, borderRightWidth: THICK },
  bottomLeft: { bottom: 0, left: 0,  borderBottomWidth: THICK, borderLeftWidth: THICK },
  bottomRight:{ bottom: 0, right: 0, borderBottomWidth: THICK, borderRightWidth: THICK },
  scanLoading: { alignItems: 'center', gap: Spacing.sm },
  scanLoadingText: { color: '#fff', fontSize: 13 },
  hint: { color: '#fff', fontSize: 16, fontWeight: '600', textAlign: 'center' },
  hintSub: { color: 'rgba(255,255,255,0.5)', fontSize: 12, marginTop: 4 },
  btnRow: { flexDirection: 'row', gap: Spacing.md, marginTop: Spacing.xl },
  btn: { flexDirection: 'row', alignItems: 'center', gap: Spacing.sm, backgroundColor: 'rgba(255,255,255,0.15)', borderRadius: Radius.full, paddingHorizontal: Spacing.lg, paddingVertical: Spacing.sm },
  resetBtn: { backgroundColor: Colors.primary + 'AA' },
  btnText: { color: '#fff', fontWeight: '600', fontSize: 13 },
})
