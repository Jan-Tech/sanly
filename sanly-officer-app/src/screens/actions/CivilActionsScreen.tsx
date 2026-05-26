import React, { useState } from 'react'
import { View, Text, ScrollView, TextInput, TouchableOpacity, Modal, StyleSheet, Alert } from 'react-native'
import { useMutation } from '@tanstack/react-query'
import { Baby, Heart, Cross } from 'lucide-react-native'
import { registerBirth, registerMarriage, registerDeath } from '../../api/civil'
import { useOfflineQueueStore } from '../../store/offlineQueueStore'
import { Colors, Spacing, Radius, Shadow } from '../../constants/theme'

type FormType = 'BIRTH' | 'MARRIAGE' | 'DEATH' | null

function Field({ label, value, onChangeText, placeholder, keyboardType }: any) {
  return (
    <View>
      <Text style={styles.label}>{label}</Text>
      <TextInput style={styles.input} value={value} onChangeText={onChangeText} placeholder={placeholder} placeholderTextColor={Colors.textMuted} keyboardType={keyboardType ?? 'default'} />
    </View>
  )
}

export function CivilActionsScreen() {
  const { enqueue } = useOfflineQueueStore()
  const [form, setForm] = useState<FormType>(null)

  // Birth form state
  const [b, setB] = useState({ childNin: '', firstName: '', lastName: '', dob: '', place: '', fatherNin: '', motherNin: '' })
  // Marriage form state
  const [m, setM] = useState({ nin1: '', nin2: '', date: '', place: '' })
  // Death form state
  const [d, setD] = useState({ nin: '', date: '', place: '', cause: '' })

  const birthMutation = useMutation({
    mutationFn: () => registerBirth({ childNationalId: b.childNin, childFirstName: b.firstName, childLastName: b.lastName, dateOfBirth: b.dob, placeOfBirth: b.place, fatherNationalId: b.fatherNin || undefined, motherNationalId: b.motherNin || undefined }),
    onSuccess: () => { Alert.alert('Registered', 'Birth registration successful.'); setForm(null); setB({ childNin: '', firstName: '', lastName: '', dob: '', place: '', fatherNin: '', motherNin: '' }) },
    onError: async (e: any) => {
      if (!e?.response) {
        await enqueue({ type: 'BIRTH_REGISTRATION', endpoint: 'civil/api/v1/births', method: 'POST', payload: { childNationalId: b.childNin, childFirstName: b.firstName, childLastName: b.lastName, dateOfBirth: b.dob, placeOfBirth: b.place }, description: `Birth: ${b.firstName} ${b.lastName}` })
        Alert.alert('Offline', 'Queued for submission.')
        setForm(null)
      } else {
        Alert.alert('Error', e?.response?.data?.message ?? 'Registration failed')
      }
    },
  })

  const marriageMutation = useMutation({
    mutationFn: () => registerMarriage({ spouse1NationalId: m.nin1, spouse2NationalId: m.nin2, marriageDate: m.date, marriagePlace: m.place }),
    onSuccess: () => { Alert.alert('Registered', 'Marriage registration successful.'); setForm(null); setM({ nin1: '', nin2: '', date: '', place: '' }) },
    onError: async (e: any) => {
      if (!e?.response) {
        await enqueue({ type: 'MARRIAGE_REGISTRATION', endpoint: 'civil/api/v1/marriages', method: 'POST', payload: { spouse1NationalId: m.nin1, spouse2NationalId: m.nin2, marriageDate: m.date, marriagePlace: m.place }, description: `Marriage: ${m.nin1} + ${m.nin2}` })
        Alert.alert('Offline', 'Queued for submission.')
        setForm(null)
      } else {
        Alert.alert('Error', e?.response?.data?.message ?? 'Registration failed')
      }
    },
  })

  const deathMutation = useMutation({
    mutationFn: () => registerDeath({ deceasedNationalId: d.nin, dateOfDeath: d.date, placeOfDeath: d.place, causeOfDeath: d.cause || undefined }),
    onSuccess: () => { Alert.alert('Registered', 'Death registration successful.'); setForm(null); setD({ nin: '', date: '', place: '', cause: '' }) },
    onError: (e: any) => Alert.alert('Error', e?.response?.data?.message ?? 'Registration failed'),
  })

  const formButtons = [
    { type: 'BIRTH' as FormType, icon: <Baby size={28} color={Colors.civil} />, label: 'Register Birth', color: Colors.civil, bg: Colors.civilBg },
    { type: 'MARRIAGE' as FormType, icon: <Heart size={28} color={Colors.error} />, label: 'Register Marriage', color: Colors.error, bg: Colors.errorBg },
    { type: 'DEATH' as FormType, icon: <Cross size={28} color={Colors.textSecondary} />, label: 'Register Death', color: Colors.textSecondary, bg: Colors.borderLight },
  ]

  return (
    <ScrollView style={styles.scroll} contentContainerStyle={styles.content}>
      <View style={styles.grid}>
        {formButtons.map(fb => (
          <TouchableOpacity
            key={fb.type}
            style={[styles.formBtn, { borderColor: fb.color, backgroundColor: fb.bg }]}
            onPress={() => setForm(fb.type)}
          >
            {fb.icon}
            <Text style={[styles.formBtnText, { color: fb.color }]}>{fb.label}</Text>
          </TouchableOpacity>
        ))}
      </View>

      <View style={styles.note}>
        <Text style={styles.noteText}>All civil registrations are permanent records. Verify all NINs before submitting. Offline submissions will sync when connectivity is restored.</Text>
      </View>

      {/* Birth Modal */}
      <Modal visible={form === 'BIRTH'} animationType="slide" presentationStyle="pageSheet">
        <View style={styles.modal}>
          <View style={styles.modalHeader}>
            <Text style={styles.modalTitle}>Register Birth</Text>
            <TouchableOpacity onPress={() => setForm(null)}><Text style={styles.modalClose}>Cancel</Text></TouchableOpacity>
          </View>
          <ScrollView contentContainerStyle={styles.modalContent}>
            <Field label="Child's National ID" value={b.childNin} onChangeText={(v: string) => setB(p => ({ ...p, childNin: v }))} placeholder="10-digit NIN" keyboardType="numeric" />
            <Field label="First Name" value={b.firstName} onChangeText={(v: string) => setB(p => ({ ...p, firstName: v }))} placeholder="First name" />
            <Field label="Last Name" value={b.lastName} onChangeText={(v: string) => setB(p => ({ ...p, lastName: v }))} placeholder="Last name" />
            <Field label="Date of Birth (YYYY-MM-DD)" value={b.dob} onChangeText={(v: string) => setB(p => ({ ...p, dob: v }))} placeholder="2024-01-15" />
            <Field label="Place of Birth" value={b.place} onChangeText={(v: string) => setB(p => ({ ...p, place: v }))} placeholder="City, hospital..." />
            <Field label="Father's NIN (optional)" value={b.fatherNin} onChangeText={(v: string) => setB(p => ({ ...p, fatherNin: v }))} placeholder="10-digit NIN" keyboardType="numeric" />
            <Field label="Mother's NIN (optional)" value={b.motherNin} onChangeText={(v: string) => setB(p => ({ ...p, motherNin: v }))} placeholder="10-digit NIN" keyboardType="numeric" />
            <TouchableOpacity style={[styles.submitBtn, { backgroundColor: Colors.civil }, birthMutation.isPending && styles.disabled]} onPress={() => birthMutation.mutate()} disabled={birthMutation.isPending}>
              <Text style={styles.submitText}>{birthMutation.isPending ? 'Registering...' : 'Register Birth'}</Text>
            </TouchableOpacity>
          </ScrollView>
        </View>
      </Modal>

      {/* Marriage Modal */}
      <Modal visible={form === 'MARRIAGE'} animationType="slide" presentationStyle="pageSheet">
        <View style={styles.modal}>
          <View style={styles.modalHeader}>
            <Text style={styles.modalTitle}>Register Marriage</Text>
            <TouchableOpacity onPress={() => setForm(null)}><Text style={styles.modalClose}>Cancel</Text></TouchableOpacity>
          </View>
          <ScrollView contentContainerStyle={styles.modalContent}>
            <Field label="Spouse 1 NIN" value={m.nin1} onChangeText={(v: string) => setM(p => ({ ...p, nin1: v }))} placeholder="10-digit NIN" keyboardType="numeric" />
            <Field label="Spouse 2 NIN" value={m.nin2} onChangeText={(v: string) => setM(p => ({ ...p, nin2: v }))} placeholder="10-digit NIN" keyboardType="numeric" />
            <Field label="Marriage Date (YYYY-MM-DD)" value={m.date} onChangeText={(v: string) => setM(p => ({ ...p, date: v }))} placeholder="2024-06-15" />
            <Field label="Place of Marriage" value={m.place} onChangeText={(v: string) => setM(p => ({ ...p, place: v }))} placeholder="City, registry office..." />
            <TouchableOpacity style={[styles.submitBtn, { backgroundColor: Colors.error }, marriageMutation.isPending && styles.disabled]} onPress={() => marriageMutation.mutate()} disabled={marriageMutation.isPending}>
              <Text style={styles.submitText}>{marriageMutation.isPending ? 'Registering...' : 'Register Marriage'}</Text>
            </TouchableOpacity>
          </ScrollView>
        </View>
      </Modal>

      {/* Death Modal */}
      <Modal visible={form === 'DEATH'} animationType="slide" presentationStyle="pageSheet">
        <View style={styles.modal}>
          <View style={styles.modalHeader}>
            <Text style={styles.modalTitle}>Register Death</Text>
            <TouchableOpacity onPress={() => setForm(null)}><Text style={styles.modalClose}>Cancel</Text></TouchableOpacity>
          </View>
          <ScrollView contentContainerStyle={styles.modalContent}>
            <Field label="Deceased's National ID" value={d.nin} onChangeText={(v: string) => setD(p => ({ ...p, nin: v }))} placeholder="10-digit NIN" keyboardType="numeric" />
            <Field label="Date of Death (YYYY-MM-DD)" value={d.date} onChangeText={(v: string) => setD(p => ({ ...p, date: v }))} placeholder="2024-01-15" />
            <Field label="Place of Death" value={d.place} onChangeText={(v: string) => setD(p => ({ ...p, place: v }))} placeholder="City, hospital..." />
            <Field label="Cause of Death (optional)" value={d.cause} onChangeText={(v: string) => setD(p => ({ ...p, cause: v }))} placeholder="Natural causes, accident..." />
            <TouchableOpacity style={[styles.submitBtn, { backgroundColor: Colors.dark }, deathMutation.isPending && styles.disabled]} onPress={() => deathMutation.mutate()} disabled={deathMutation.isPending}>
              <Text style={styles.submitText}>{deathMutation.isPending ? 'Registering...' : 'Register Death'}</Text>
            </TouchableOpacity>
          </ScrollView>
        </View>
      </Modal>
    </ScrollView>
  )
}

const styles = StyleSheet.create({
  scroll: { flex: 1, backgroundColor: Colors.background },
  content: { padding: Spacing.lg, paddingBottom: 40 },
  grid: { gap: Spacing.md, marginBottom: Spacing.lg },
  formBtn: { borderWidth: 1.5, borderRadius: Radius.lg, padding: Spacing.xl, alignItems: 'center', gap: Spacing.sm, ...Shadow.sm },
  formBtnText: { fontSize: 16, fontWeight: '700' },
  note: { backgroundColor: Colors.infoBg, borderRadius: Radius.md, padding: Spacing.md },
  noteText: { fontSize: 12, color: Colors.info, lineHeight: 18 },
  modal: { flex: 1, backgroundColor: Colors.background },
  modalHeader: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', padding: Spacing.lg, backgroundColor: Colors.navy },
  modalTitle: { fontSize: 17, fontWeight: '700', color: '#fff' },
  modalClose: { color: Colors.primary, fontSize: 15, fontWeight: '600' },
  modalContent: { padding: Spacing.lg, paddingBottom: 40, gap: Spacing.sm },
  label: { fontSize: 12, fontWeight: '600', color: Colors.textSecondary, marginBottom: Spacing.sm, textTransform: 'uppercase', letterSpacing: 0.5, marginTop: Spacing.md },
  input: { backgroundColor: Colors.card, borderRadius: Radius.md, borderWidth: 1, borderColor: Colors.border, padding: Spacing.md, fontSize: 15, color: Colors.dark, ...Shadow.sm },
  submitBtn: { borderRadius: Radius.lg, padding: Spacing.md, alignItems: 'center', marginTop: Spacing.xl },
  disabled: { opacity: 0.5 },
  submitText: { color: '#fff', fontWeight: '700', fontSize: 15 },
})
