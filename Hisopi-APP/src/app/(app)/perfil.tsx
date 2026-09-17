import { router } from 'expo-router'
import { Pressable, StyleSheet, Text, View } from 'react-native'

import { useAuth } from '@/src/context/AuthContext'
import { useResponsive } from '@/src/hooks/useResponsive'
import { colors } from '@/src/theme/colors'

export default function PerfilScreen() {
  const { usuario, logout } = useAuth()
  const { isDesktop } = useResponsive()

  async function handleLogout() {
    await logout()
    router.replace('/login')
  }

  const inicial = usuario?.nome?.charAt(0)?.toUpperCase() ?? '?'
  const roleLabel = usuario?.role === 'ADMIN' ? 'Administrador' : 'Usuário'

  return (
    <View style={styles.screen}>
      <View style={[styles.card, isDesktop && styles.cardDesktop]}>
        <View style={styles.avatar}>
          <Text style={styles.avatarText}>{inicial}</Text>
        </View>

        <Text style={styles.nome}>{usuario?.nome ?? 'Usuário'}</Text>
        <Text style={styles.email}>{usuario?.email}</Text>

        <View style={styles.roleBadge}>
          <Text style={styles.roleBadgeText}>{roleLabel}</Text>
        </View>

        <View style={styles.infoSection}>
          <InfoRow label="Nome" value={usuario?.nome ?? '—'} />
          <InfoRow label="E-mail" value={usuario?.email ?? '—'} />
          <InfoRow label="Tipo de conta" value={roleLabel} />
        </View>

        <Pressable style={styles.logoutButton} onPress={handleLogout}>
          <Text style={styles.logoutButtonText}>Sair da conta</Text>
        </Pressable>
      </View>
    </View>
  )
}

function InfoRow({ label, value }: { label: string; value: string }) {
  return (
    <View style={styles.infoRow}>
      <Text style={styles.infoLabel}>{label}</Text>
      <Text style={styles.infoValue}>{value}</Text>
    </View>
  )
}

const styles = StyleSheet.create({
  screen: {
    flex: 1,
    backgroundColor: colors.background,
    alignItems: 'center',
    padding: 24,
  },

  card: {
    width: '100%',
    maxWidth: 480,
    backgroundColor: colors.surface,
    borderRadius: 20,
    padding: 28,
    alignItems: 'center',
    marginTop: 24,
  },

  cardDesktop: {
    marginTop: 64,
    elevation: 3,
    shadowColor: '#000',
    shadowOpacity: 0.06,
    shadowRadius: 16,
    shadowOffset: { width: 0, height: 8 },
  },

  avatar: {
    width: 84,
    height: 84,
    borderRadius: 42,
    backgroundColor: colors.primary,
    alignItems: 'center',
    justifyContent: 'center',
    marginBottom: 16,
  },

  avatarText: {
    fontSize: 32,
    fontWeight: '800',
    color: '#fff',
  },

  nome: {
    fontSize: 22,
    fontWeight: '800',
    color: colors.text,
  },

  email: {
    fontSize: 14,
    color: colors.textMuted,
    marginTop: 4,
  },

  roleBadge: {
    backgroundColor: colors.accentSoft,
    paddingHorizontal: 14,
    paddingVertical: 6,
    borderRadius: 999,
    marginTop: 14,
  },

  roleBadgeText: {
    fontSize: 12,
    fontWeight: '700',
    color: colors.primary,
  },

  infoSection: {
    width: '100%',
    marginTop: 28,
    gap: 14,
  },

  infoRow: {
    borderBottomWidth: 1,
    borderBottomColor: colors.border,
    paddingBottom: 12,
  },

  infoLabel: {
    fontSize: 12,
    fontWeight: '700',
    color: colors.textMuted,
    marginBottom: 4,
    textTransform: 'uppercase',
    letterSpacing: 0.5,
  },

  infoValue: {
    fontSize: 15,
    color: colors.text,
  },

  logoutButton: {
    width: '100%',
    borderWidth: 1,
    borderColor: colors.danger,
    paddingVertical: 14,
    borderRadius: 12,
    alignItems: 'center',
    marginTop: 28,
  },

  logoutButtonText: {
    color: colors.danger,
    fontSize: 15,
    fontWeight: '700',
  },
})