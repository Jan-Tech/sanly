import React, { useState } from 'react'
import { View, TextInput, FlatList, Text, StyleSheet, TouchableOpacity } from 'react-native'
import { useNavigation } from '@react-navigation/native'
import type { NativeStackNavigationProp } from '@react-navigation/native-stack'
import { useQuery } from '@tanstack/react-query'
import { Search } from 'lucide-react-native'
import { useLangStore } from '../../store/langStore'
import { searchCitizens, getCitizen } from '../../api/registry'
import { CitizenCard } from '../../components/CitizenCard'
import { LoadingView } from '../../components/LoadingView'
import { t } from '../../i18n'
import { Colors, Spacing, Radius, Shadow } from '../../constants/theme'
import type { SearchStackParamList } from '../../types'

type NavProp = NativeStackNavigationProp<SearchStackParamList>

export function SearchScreen() {
  const navigation = useNavigation<NavProp>()
  const { lang } = useLangStore()
  const [query, setQuery] = useState('')
  const [submitted, setSubmitted] = useState('')

  const { data, isLoading, isFetching } = useQuery({
    queryKey: ['citizen-search', submitted],
    queryFn: () => {
      // If it's a 10-digit NIN, do direct lookup
      if (/^\d{10}$/.test(submitted.trim())) {
        return getCitizen(submitted.trim()).then(c => [c])
      }
      // Strip TM-NIN- prefix if present
      const nin = submitted.replace(/^TM-NIN-/i, '').trim()
      if (/^\d{10}$/.test(nin)) {
        return getCitizen(nin).then(c => [c])
      }
      return searchCitizens(submitted.trim())
    },
    enabled: submitted.length >= 3,
    retry: false,
  })

  function handleSubmit() {
    setSubmitted(query)
  }

  return (
    <View style={styles.container}>
      {/* Search bar */}
      <View style={styles.searchBar}>
        <View style={styles.inputWrap}>
          <Search size={18} color={Colors.textMuted} style={{ marginRight: Spacing.sm }} />
          <TextInput
            style={styles.input}
            value={query}
            onChangeText={setQuery}
            placeholder={t(lang, 'searchPlaceholder')}
            placeholderTextColor={Colors.textMuted}
            returnKeyType="search"
            onSubmitEditing={handleSubmit}
            autoCapitalize="none"
            autoCorrect={false}
          />
          {query.length > 0 && (
            <TouchableOpacity onPress={() => { setQuery(''); setSubmitted('') }}>
              <Text style={styles.clearText}>✕</Text>
            </TouchableOpacity>
          )}
        </View>
        <TouchableOpacity style={styles.searchBtn} onPress={handleSubmit}>
          <Text style={styles.searchBtnText}>{t(lang, 'search')}</Text>
        </TouchableOpacity>
      </View>

      {!submitted && (
        <View style={styles.hint}>
          <Text style={styles.hintText}>{t(lang, 'searchHint')}</Text>
        </View>
      )}

      {(isLoading || isFetching) && submitted && <LoadingView />}

      {!isLoading && submitted && (
        <FlatList
          data={data ?? []}
          keyExtractor={c => c.nationalId}
          contentContainerStyle={styles.list}
          ListEmptyComponent={<Text style={styles.empty}>{t(lang, 'noResults')}</Text>}
          renderItem={({ item }) => (
            <CitizenCard
              citizen={item}
              onPress={() => navigation.navigate('CitizenDetail', { nationalId: item.nationalId })}
            />
          )}
        />
      )}
    </View>
  )
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: Colors.background },
  searchBar: { flexDirection: 'row', gap: Spacing.sm, padding: Spacing.lg, backgroundColor: Colors.navy },
  inputWrap: { flex: 1, flexDirection: 'row', alignItems: 'center', backgroundColor: 'rgba(255,255,255,0.1)', borderRadius: Radius.md, paddingHorizontal: Spacing.md, height: 44 },
  input: { flex: 1, color: '#fff', fontSize: 14 },
  clearText: { color: 'rgba(255,255,255,0.5)', fontSize: 16, paddingLeft: Spacing.sm },
  searchBtn: { backgroundColor: Colors.primary, borderRadius: Radius.md, paddingHorizontal: Spacing.lg, justifyContent: 'center' },
  searchBtnText: { color: '#fff', fontWeight: '700', fontSize: 14 },
  hint: { padding: Spacing.xl, alignItems: 'center' },
  hintText: { fontSize: 13, color: Colors.textMuted, textAlign: 'center' },
  list: { padding: Spacing.lg, gap: Spacing.sm },
  empty: { textAlign: 'center', color: Colors.textMuted, marginTop: 48, fontSize: 14 },
})
