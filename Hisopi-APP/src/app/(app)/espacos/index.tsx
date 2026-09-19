import { router } from 'expo-router'
import { useCallback, useEffect, useState } from 'react'
import {
  ActivityIndicator,
  FlatList,
  Pressable,
  StyleSheet,
  Text,
  View,
} from 'react-native'
import { useFocusEffect } from 'expo-router'

import { useAuth } from '@/src/context/AuthContext'
import { useResponsive } from '@/src/hooks/useResponsive'
import { colors } from '@/src/theme/colors'
import {
  listarEspacosDoUsuario,
  type EspacoComPapel,
} from '@/src/services/espacoService'

const PAPEL_LABEL: Record<string, string> = {
  DONO: 'Dono',
  ADMIN: 'Admin',
  GERENTE: 'Gerente',
  OPERADOR: 'Operador',
}

export default function EspacosScreen() {
  const { usuario } = useAuth()
  const { isDesktop } = useResponsive()

  const [espacos, setEspacos] = useState<EspacoComPapel[]>([])
  const [loading, setLoading] = useState(true)

  const load = useCallback(async () => {
    if (!usuario) return

    try {
      const data = await listarEspacosDoUsuario()
      setEspacos(data)
    } catch (error) {
      console.log('Erro ao carregar espaços:', error)
    } finally {
      setLoading(false)
    }
  }, [usuario])

  // Recarrega sempre que a tela ganha foco — importante para refletir
  // um espaço recém-criado ao voltar de /espacos/novo.
  useFocusEffect(
    useCallback(() => {
      load()
    }, [load])
  )

  if (loading) {
    return (
      <View style={styles.center}>
        <ActivityIndicator size="large" color={colors.primary} />
      </View>
    )
  }

  return (
    <View style={styles.screen}>
      <View style={[styles.header, isDesktop && styles.headerDesktop]}>
        <View>
          <Text style={styles.title}>Seus espaços</Text>
          <Text style={styles.subtitle}>
            Escolha um espaço para gerenciar, ou crie um novo.
          </Text>
        </View>

        <Pressable
          style={styles.newButton}
          onPress={() => router.push('/espacos/novo')}
        >
          <Text style={styles.newButtonText}>+ Novo espaço</Text>
        </Pressable>
      </View>

      {espacos.length === 0 ? (
        <View style={styles.empty}>
          <View style={styles.emptyIcon}>
            <Text style={styles.emptyIconText}>+</Text>
          </View>
          <Text style={styles.emptyTitle}>Nenhum espaço ainda</Text>
          <Text style={styles.emptyText}>
            Crie um espaço pessoal para controlar sua despensa, ou um
            espaço de organização para o seu negócio.
          </Text>
          <Pressable
            style={styles.emptyButton}
            onPress={() => router.push('/espacos/novo')}
          >
            <Text style={styles.emptyButtonText}>Criar meu primeiro espaço</Text>
          </Pressable>
        </View>
      ) : (
        <FlatList
          data={espacos}
          key={isDesktop ? 'desktop' : 'mobile'}
          keyExtractor={(item) => String(item.id)}
          numColumns={isDesktop ? 3 : 1}
          columnWrapperStyle={isDesktop ? styles.row : undefined}
          contentContainerStyle={[
            styles.list,
            isDesktop && styles.listDesktop,
          ]}
          renderItem={({ item }) => (
            <Pressable
              style={[styles.card, isDesktop && styles.cardDesktop]}
              onPress={() => router.push(`/${item.id}` as any)}
            >
              <View
                style={[
                  styles.cardIcon,
                  item.tipo === 'ORGANIZACAO' && styles.cardIconOrg,
                ]}
              >
                <Text style={styles.cardIconText}>
                  {item.tipo === 'ORGANIZACAO' ? '🏢' : '🏠'}
                </Text>
              </View>

              <Text style={styles.cardNome} numberOfLines={1}>
                {item.nome}
              </Text>

              <Text style={styles.cardTipo}>
                {item.tipo === 'ORGANIZACAO' ? 'Organização' : 'Pessoal'}
              </Text>

              <View style={styles.papelBadge}>
                <Text style={styles.papelBadgeText}>
                  {PAPEL_LABEL[item.papel] ?? item.papel}
                </Text>
              </View>
            </Pressable>
          )}
        />
      )}
    </View>
  )
}

const styles = StyleSheet.create({
  screen: {
    flex: 1,
    backgroundColor: colors.background,
    padding: 20,
  },

  center: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: colors.background,
  },

  header: {
    marginBottom: 24,
  },

  headerDesktop: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'flex-end',
    maxWidth: 1000,
    width: '100%',
    alignSelf: 'center',
  },

  title: {
    fontSize: 26,
    fontWeight: '800',
    color: colors.text,
  },

  subtitle: {
    fontSize: 14,
    color: colors.textMuted,
    marginTop: 4,
  },

  newButton: {
    backgroundColor: colors.primary,
    paddingVertical: 12,
    paddingHorizontal: 18,
    borderRadius: 12,
    marginTop: 16,
    alignSelf: 'flex-start',
  },

  newButtonText: {
    color: '#fff',
    fontWeight: '700',
    fontSize: 14,
  },

  list: {
    paddingBottom: 24,
  },

  listDesktop: {
    maxWidth: 1000,
    width: '100%',
    alignSelf: 'center',
  },

  row: {
    gap: 16,
    justifyContent: 'flex-start',
  },

  card: {
    backgroundColor: colors.surface,
    borderRadius: 18,
    padding: 20,
    borderWidth: 1,
    borderColor: colors.border,
    marginBottom: 16,
  },

  cardDesktop: {
    flex: 1,
    maxWidth: '32%',
  },

  cardIcon: {
    width: 52,
    height: 52,
    borderRadius: 14,
    backgroundColor: colors.accentSoft,
    alignItems: 'center',
    justifyContent: 'center',
    marginBottom: 14,
  },

  cardIconOrg: {
    backgroundColor: '#EDE3EF',
  },

  cardIconText: {
    fontSize: 24,
  },

  cardNome: {
    fontSize: 17,
    fontWeight: '700',
    color: colors.text,
  },

  cardTipo: {
    fontSize: 13,
    color: colors.textMuted,
    marginTop: 2,
    marginBottom: 12,
  },

  papelBadge: {
    alignSelf: 'flex-start',
    backgroundColor: colors.background,
    borderWidth: 1,
    borderColor: colors.border,
    paddingHorizontal: 10,
    paddingVertical: 4,
    borderRadius: 999,
  },

  papelBadgeText: {
    fontSize: 11,
    fontWeight: '700',
    color: colors.secondary,
  },

  empty: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    paddingHorizontal: 32,
  },

  emptyIcon: {
    width: 64,
    height: 64,
    borderRadius: 32,
    backgroundColor: colors.accentSoft,
    alignItems: 'center',
    justifyContent: 'center',
    marginBottom: 20,
  },

  emptyIconText: {
    fontSize: 32,
    color: colors.primary,
    fontWeight: '800',
  },

  emptyTitle: {
    fontSize: 18,
    fontWeight: '800',
    color: colors.text,
    marginBottom: 8,
  },

  emptyText: {
    fontSize: 14,
    color: colors.textMuted,
    textAlign: 'center',
    lineHeight: 21,
    marginBottom: 24,
  },

  emptyButton: {
    backgroundColor: colors.primary,
    paddingVertical: 14,
    paddingHorizontal: 24,
    borderRadius: 12,
  },

  emptyButtonText: {
    color: '#fff',
    fontWeight: '700',
    fontSize: 15,
  },
})