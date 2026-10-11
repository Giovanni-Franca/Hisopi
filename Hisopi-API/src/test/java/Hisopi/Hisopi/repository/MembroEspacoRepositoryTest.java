package Hisopi.Hisopi.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.hibernate.Hibernate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import Hisopi.Hisopi.Enum.PapelMembro;
import Hisopi.Hisopi.Enum.TipoEspaco;
import Hisopi.Hisopi.Enum.UserRole;
import Hisopi.Hisopi.infra.exception.ErrorResponseWriter;
import Hisopi.Hisopi.model.Espaco;
import Hisopi.Hisopi.model.MembroEspaco;
import Hisopi.Hisopi.model.Usuario;
import jakarta.persistence.EntityManager;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class MembroEspacoRepositoryTest {

    @MockitoBean
    private ErrorResponseWriter errorResponseWriter;

    @Autowired
    private MembroEspacoRepository membroRepository;

    @Autowired
    private EspacoRepository espacoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EntityManager entityManager;

    private Espaco espaco;
    private Espaco outroEspaco;
    private Usuario maria;
    private Usuario joao;

    private final PapelMembro papel = PapelMembro.OPERADOR;
    
    @BeforeEach
    void setUp() {
        espaco = criarEspaco("Cozinha", TipoEspaco.ORGANIZACAO);
        outroEspaco = criarEspaco("Casa", TipoEspaco.PESSOAL);

        maria = criarUsuario("Maria", "maria@email.com");
        joao = criarUsuario("João", "joao@email.com");
    }

    private Espaco criarEspaco(String nome, TipoEspaco tipo) {
        Espaco e = new Espaco();
        e.setNome(nome);
        e.setTipo(tipo);
        return espacoRepository.save(e);
    }

    private Usuario criarUsuario(String nome, String email) {
        Usuario u = new Usuario(nome, email, "senha123", UserRole.USER);
        return usuarioRepository.save(u);
    }

    private MembroEspaco criarMembro(Espaco espaco, Usuario usuario, PapelMembro papel) {
        MembroEspaco m = new MembroEspaco();
        m.setEspaco(espaco);
        m.setUsuario(usuario);
        m.setPapel(papel);
        return m;
    }

    private MembroEspaco salvarMembro(Espaco espaco, Usuario usuario) {
        return membroRepository.save(criarMembro(espaco, usuario, papel));
    }

    @Test
    void deveSalvarMembroEGerarId() {
        MembroEspaco salvo = membroRepository.save(criarMembro(espaco, maria, papel));

        assertThat(salvo.getId()).isNotNull();
        assertThat(salvo.getEspaco().getId()).isEqualTo(espaco.getId());
        assertThat(salvo.getUsuario().getId()).isEqualTo(maria.getId());
        assertThat(salvo.getPapel()).isEqualTo(papel);
    }

    @Test
    void devePreencherEntradaEmAutomaticamente() {
        LocalDateTime antes = LocalDateTime.now().minusSeconds(1);

        MembroEspaco salvo = membroRepository.save(criarMembro(espaco, maria, papel));

        assertThat(salvo.getEntradaEm()).isNotNull();
        assertThat(salvo.getEntradaEm()).isAfter(antes);
    }

    @ParameterizedTest
    @EnumSource(PapelMembro.class)
    void devePersistirERecuperarTodosOsPapeis(PapelMembro papelTestado) {
        MembroEspaco salvo = membroRepository.saveAndFlush(criarMembro(espaco, maria, papelTestado));
        entityManager.clear();

        MembroEspaco recuperado = membroRepository.findById(salvo.getId()).orElseThrow();

        assertThat(recuperado.getPapel()).isEqualTo(papelTestado);
    }

    @Test
    void devePersistirPapelComoNomeDoEnumNoBanco() {
        MembroEspaco salvo = membroRepository.saveAndFlush(criarMembro(espaco, maria, papel));
        entityManager.clear();

        String papelNoBanco = (String) entityManager
                .createNativeQuery("SELECT papel FROM membros_espaco WHERE id = :id")
                .setParameter("id", salvo.getId())
                .getSingleResult();

        assertThat(papelNoBanco).isEqualTo(papel.name());
    }

    @Test
    void deveAtualizarMembro() {
        MembroEspaco salvo = membroRepository.saveAndFlush(criarMembro(espaco, maria, PapelMembro.OPERADOR));

        salvo.setPapel(PapelMembro.GERENTE);
        membroRepository.saveAndFlush(salvo);
        entityManager.clear();

        MembroEspaco atualizado = membroRepository.findById(salvo.getId()).orElseThrow();
        assertThat(atualizado.getPapel()).isEqualTo(PapelMembro.GERENTE);
    }

    @Test
    void deveDeletarMembro() {
        MembroEspaco salvo = salvarMembro(espaco, maria);

        membroRepository.deleteById(salvo.getId());

        assertThat(membroRepository.findById(salvo.getId())).isEmpty();
    }

    @Test
    void deveListarMembrosDoEspaco() {
        salvarMembro(espaco, maria);
        salvarMembro(espaco, joao);
        salvarMembro(outroEspaco, maria);

        List<MembroEspaco> resultado = membroRepository.findByEspacoId(espaco.getId());

        assertThat(resultado).extracting(m -> m.getUsuario().getId())
                .containsExactlyInAnyOrder(maria.getId(), joao.getId());
    }

    @Test
    void deveCarregarUsuarioJuntoComEntityGraphNaListagemPorEspaco() {
        salvarMembro(espaco, maria);
        entityManager.flush();
        entityManager.clear();

        List<MembroEspaco> resultado = membroRepository.findByEspacoId(espaco.getId());

        assertThat(resultado).hasSize(1);
        assertThat(Hibernate.isInitialized(resultado.get(0).getUsuario())).isTrue();
    }

    @Test
    void deveRetornarListaVaziaParaEspacoSemMembros() {
        assertThat(membroRepository.findByEspacoId(espaco.getId())).isEmpty();
    }

    @Test
    void deveRetornarListaVaziaParaEspacoInexistente() {
        assertThat(membroRepository.findByEspacoId(999L)).isEmpty();
    }

    @Test
    void deveBuscarMembroPorEspacoEUsuario() {
        MembroEspaco salvo = salvarMembro(espaco, maria);

        Optional<MembroEspaco> resultado = membroRepository
                .findByEspacoIdAndUsuarioId(espaco.getId(), maria.getId());

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getId()).isEqualTo(salvo.getId());
    }

    @Test
    void naoDeveBuscarMembroDeOutroEspaco() {
        salvarMembro(espaco, maria);

        Optional<MembroEspaco> resultado = membroRepository
                .findByEspacoIdAndUsuarioId(outroEspaco.getId(), maria.getId());

        assertThat(resultado).isEmpty();
    }

    @Test
    void naoDeveBuscarMembroDeOutroUsuario() {
        salvarMembro(espaco, maria);

        Optional<MembroEspaco> resultado = membroRepository
                .findByEspacoIdAndUsuarioId(espaco.getId(), joao.getId());

        assertThat(resultado).isEmpty();
    }

    @Test
    void deveRetornarTrueQuandoUsuarioEhMembro() {
        salvarMembro(espaco, maria);

        assertThat(membroRepository.existsByEspacoIdAndUsuarioId(espaco.getId(), maria.getId())).isTrue();
    }

    @Test
    void deveRetornarFalseQuandoUsuarioNaoEhMembroDoEspaco() {
        salvarMembro(espaco, maria);

        assertThat(membroRepository.existsByEspacoIdAndUsuarioId(outroEspaco.getId(), maria.getId())).isFalse();
        assertThat(membroRepository.existsByEspacoIdAndUsuarioId(espaco.getId(), joao.getId())).isFalse();
    }

    @Test
    void deveListarEspacosDoUsuario() {
        salvarMembro(espaco, maria);
        salvarMembro(outroEspaco, maria);
        salvarMembro(espaco, joao);

        List<MembroEspaco> resultado = membroRepository.findByUsuarioId(maria.getId());

        assertThat(resultado).extracting(m -> m.getEspaco().getId())
                .containsExactlyInAnyOrder(espaco.getId(), outroEspaco.getId());
    }

    @Test
    void deveCarregarEspacoJuntoComEntityGraphNaListagemPorUsuario() {
        salvarMembro(espaco, maria);
        entityManager.flush();
        entityManager.clear();

        List<MembroEspaco> resultado = membroRepository.findByUsuarioId(maria.getId());

        assertThat(resultado).hasSize(1);
        assertThat(Hibernate.isInitialized(resultado.get(0).getEspaco())).isTrue();
        assertThat(resultado.get(0).getEspaco().getNome()).isEqualTo("Cozinha");
    }

    @Test
    void deveRetornarListaVaziaParaUsuarioSemEspacos() {
        assertThat(membroRepository.findByUsuarioId(maria.getId())).isEmpty();
    }

    // ---------- Constraint única (espaco_id, usuario_id) ----------

    @Test
    void naoDevePermitirMesmoUsuarioDuasVezesNoMesmoEspaco() {
        membroRepository.saveAndFlush(criarMembro(espaco, maria, papel));

        MembroEspaco duplicado = criarMembro(espaco, maria, papel);

        assertThatThrownBy(() -> membroRepository.saveAndFlush(duplicado))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void devePermitirMesmoUsuarioEmEspacosDiferentes() {
        membroRepository.saveAndFlush(criarMembro(espaco, maria, papel));
        membroRepository.saveAndFlush(criarMembro(outroEspaco, maria, papel));

        assertThat(membroRepository.findByUsuarioId(maria.getId())).hasSize(2);
    }

    @Test
    void devePermitirUsuariosDiferentesNoMesmoEspaco() {
        membroRepository.saveAndFlush(criarMembro(espaco, maria, papel));
        membroRepository.saveAndFlush(criarMembro(espaco, joao, papel));

        assertThat(membroRepository.findByEspacoId(espaco.getId())).hasSize(2);
    }

    @Test
    void naoDeveSalvarMembroSemEspaco() {
        MembroEspaco membro = criarMembro(null, maria, papel);

        assertThatThrownBy(() -> membroRepository.saveAndFlush(membro))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void naoDeveSalvarMembroSemUsuario() {
        MembroEspaco membro = criarMembro(espaco, null, papel);

        assertThatThrownBy(() -> membroRepository.saveAndFlush(membro))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void naoDeveSalvarMembroSemPapel() {
        MembroEspaco membro = criarMembro(espaco, maria, null);

        assertThatThrownBy(() -> membroRepository.saveAndFlush(membro))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}