import React, { useEffect, useState } from 'react'
import { View, Text, FlatList, TouchableOpacity, Alert, StyleSheet } from 'react-native'
import { Trash2, QrCode } from 'lucide-react-native'
import { useLangStore } from '../../store/langStore'
import { getScanHistory, clearScanHistory } from '../../hooks/useScanner'
import { scanCodeLabels } from '../../utils/codeParser'
import { timeAgo } from '../../utils/formatters'
import { t } from '../../i18n'
import { Colors, Spacing, Radius, Shadow } from '../../constants/theme'
import type { ScanHistoryItem } from '../../types'

export function ScanHistoryScreen() {
  const { lang } = useLangStore()
  const [history, setHistory] = useState<ScanHistoryItem[]>([])

  useEffect(() => {
    getScanHistory().then(setHistory)
  }, [])

  function handleClear() {
    Alert.alert(t(lang, 'clearHistory'), 'Remove all scan history?', [
      { text: t(lang, 'cancel'), style: 'cancel' },
      {
        text: t(lang, 'clearHistory'), style: 'destructive', onPress: async () => {
          await clearScanHistory()
          setHistory([])
        }
      },
    ])
  }

  return (
    <View style={styles.container}>
      {history.length > 0 && (
        <TouchableOpacity style={styles.clearBtn} onPress={handleClear}>
          <Trash2 size={14} color={Colors.error} />
          <Text style={styles.clearText}>{t(lang, 'clearHistory')}</Text>
        </TouchableOpacity>
      )}
      <FlatList
        data={history}
        keyExtractor={i => i.id}
        contentContainerStyle={styles.list}
        ListEmptyComponent={<Text style={styles.empty}>{t(lang, 'noScanHistory')}</Text>}
        renderItem={({ item }) => (
          <View style={styles.item}>
            <View style={styles.iconWrap}>
              <QrCode size={16} color={Colors.primary} />
            </View>
            <View style={{ flex: 1 }}>
              <Text style={styles.code} numberOfLines={1}>{item.rawCode}</Text>
              <Text style={styles.meta}>{scanCodeLabels[item.type]} · {timeAgo(item.timestamp)}</Text>
            </View>
          </View>
        )}
      />
    </View>
  )
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: Colors.background },
  clearBtn: { flexDirection: 'row', alignItems: 'center', gap: 6, alignSelf: 'flex-end', margin: Spacing.lg, padding: Spacing.sm },
  clearText: { fontSize: 13, color: Colors.error, fontWeight: '600' },
  list: { padding: Spacing.lg, gap: Spacing.sm },
  item: { flexDirection: 'row', alignItems: 'center', gap: Spacing.md, backgroundColor: Colors.card, borderRadius: Radius.lg, padding: Spacing.md, ...Shadow.sm },
  iconWrap: { width: 36, height: 36, borderRadius: Radius.sm, backgroundColor: Colors.primaryBg, justifyContent: 'center', alignItems: 'center' },
  code: { fontSize: 13, fontWeight: '500', color: Colors.dark },
  meta: { fontSize: 11, color: Colors.textMuted, marginTop: 2 },
  empty: { textAlign: 'center', color: Colors.textMuted, marginTop: 80, fontSize: 14 },
})
