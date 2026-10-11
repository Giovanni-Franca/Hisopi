package Hisopi.Hisopi.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

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
import static org.assertj.core.api.Assertions.*;

import Hisopi.Hisopi.Enum.TipoEspaco;
import Hisopi.Hisopi.Enum.TipoMovimentacao;
import Hisopi.Hisopi.Enum.UserRole;
import Hisopi.Hisopi.infra.exception.ErrorResponseWriter;
import Hisopi.Hisopi.model.Espaco;
import Hisopi.Hisopi.model.Insumo;
import Hisopi.Hisopi.model.LoteInsumo;
import Hisopi.Hisopi.model.MovimentacaoEstoque;
import Hisopi.Hisopi.model.Usuario;
import jakarta.persistence.EntityManager;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class MovimentacaoEstoqueRepositoryTest {

    @MockitoBean
    private ErrorResponseWriter errorResponseWriter;

    @Autowired
    private MovimentacaoEstoqueRepository movimentacaoRepository;

    @Autowired
    private InsumoRepository insumoRepository;

    @Autowired
    private LoteInsumoRepository loteRepository;

    @Autowired
    private EspacoRepository espacoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EntityManager entityManager;

    private Espaco espaco;
    private Espaco outroEspaco;
    private Insumo farinha;
    private Insumo acucar;
    private Insumo insumoOutroEspaco;
    private Usuario responsavel;

    private final LocalDateTime base = LocalDateTime.of(2026, 10, 11, 12, 0);

    @BeforeEach
    void setUp() {
        espaco = criarEspaco("Cozinha", TipoEspaco.ORGANIZACAO);
        outroEspaco = criarEspaco("Casa", TipoEspaco.PESSOAL);

        farinha = criarInsumo("Farinha", espaco);
        acucar = criarInsumo("Açúcar", espaco);
        insumoOutroEspaco = criarInsumo("Arroz", outroEspaco);

        responsavel = usuarioRepository.save(new Usuario("Maria", "maria@email.com", "senha123", UserRole.ADMIN));
    }

    // ---------- helpers ----------

    private Espaco criarEspaco(String nome, TipoEspaco tipo) {
        Espaco e = new Espaco();
        e.setNome(nome);
        e.setTipo(tipo);
        return espacoRepository.save(e);
    }

    private Insumo criarInsumo(String nome, Espaco espaco) {
        Insumo i = new Insumo();
        i.setEspaco(espaco);
        i.setNome(nome);
        i.setUnidadeMedida("kg");
        i.setEstoqueMinimo(5.0);
        return insumoRepository.save(i);
    }

    private LoteInsumo criarLote(Insumo insumo) {
        LoteInsumo l = new LoteInsumo();
        l.setInsumo(insumo);
        l.setQuantidadeInicial(10.0);
        l.setQuantidadeAtual(10.0);
        l.setDataEntrada(base.toLocalDate().minusDays(10));
        l.setDataValidade(base.toLocalDate().plusDays(30));
        return loteRepository.save(l);
    }

    private MovimentacaoEstoque criarMov(Insumo insumo, TipoMovimentacao tipo, LocalDateTime data) {
        MovimentacaoEstoque m = new MovimentacaoEstoque();
        m.setInsumo(insumo);
        m.setTipo(tipo);
        m.setQuantidade(2.0);
        m.setDataMovimentacao(data);
        return m;
    }

    private MovimentacaoEstoque salvarMov(Insumo insumo, TipoMovimentacao tipo, LocalDateTime data) {
        return movimentacaoRepository.save(criarMov(insumo, tipo, data));
    }

    // ---------- CRUD básico ----------

    @Test
    void deveSalvarMovimentacaoEGerarId() {
        MovimentacaoEstoque salva = movimentacaoRepository.save(criarMov(farinha, TipoMovimentacao.ENTRADA, base));

        assertThat(salva.getId()).isNotNull();
        assertThat(salva.getInsumo().getId()).isEqualTo(farinha.getId());
        assertThat(salva.getTipo()).isEqualTo(TipoMovimentacao.ENTRADA);
    }

    @Test
    void devePreencherDataMovimentacaoPorPadrao() {
        LocalDateTime antes = LocalDateTime.now().minusSeconds(1);

        MovimentacaoEstoque m = new MovimentacaoEstoque();
        m.setInsumo(farinha);
        m.setTipo(TipoMovimentacao.ENTRADA);
        m.setQuantidade(1.0);

        MovimentacaoEstoque salva = movimentacaoRepository.save(m);

        assertThat(salva.getDataMovimentacao()).isNotNull();
        assertThat(salva.getDataMovimentacao()).isAfter(antes);
    }

    @Test
    void devePersistirTodosOsCampos() {
        LoteInsumo lote = criarLote(farinha);
        MovimentacaoEstoque m = criarMov(farinha, TipoMovimentacao.PERDA_VALIDADE, base);
        m.setLote(lote);
        m.setMotivo("Produto vencido");
        m.setUsuarioResponsavel(responsavel);
        m.setQuantidade(3.5);

        MovimentacaoEstoque salva = movimentacaoRepository.saveAndFlush(m);
        entityManager.clear();

        MovimentacaoEstoque r = movimentacaoRepository.findById(salva.getId()).orElseThrow();
        assertThat(r.getLote().getId()).isEqualTo(lote.getId());
        assertThat(r.getMotivo()).isEqualTo("Produto vencido");
        assertThat(r.getUsuarioResponsavel().getId()).isEqualTo(responsavel.getId());
        assertThat(r.getQuantidade()).isEqualTo(3.5);
        assertThat(r.getDataMovimentacao()).isEqualTo(base);
    }

    @Test
    void devePersistirCamposOpcionaisNulos() {
        MovimentacaoEstoque salva = movimentacaoRepository.saveAndFlush(
                criarMov(farinha, TipoMovimentacao.ENTRADA, base));
        entityManager.clear();

        MovimentacaoEstoque r = movimentacaoRepository.findById(salva.getId()).orElseThrow();
        assertThat(r.getLote()).isNull();
        assertThat(r.getMotivo()).isNull();
        assertThat(r.getUsuarioResponsavel()).isNull();
    }

    @ParameterizedTest
    @EnumSource(TipoMovimentacao.class)
    void devePersistirERecuperarTodosOsTipos(TipoMovimentacao tipo) {
        MovimentacaoEstoque salva = movimentacaoRepository.saveAndFlush(criarMov(farinha, tipo, base));
        entityManager.clear();

        MovimentacaoEstoque r = movimentacaoRepository.findById(salva.getId()).orElseThrow();

        assertThat(r.getTipo()).isEqualTo(tipo);
    }

    @Test
    void devePersistirTipoComoNomeDoEnumNoBanco() {
        MovimentacaoEstoque salva = movimentacaoRepository.saveAndFlush(
                criarMov(farinha, TipoMovimentacao.SAIDA_USO, base));
        entityManager.clear();

        String tipoNoBanco = (String) entityManager
                .createNativeQuery("SELECT tipo FROM movimentacoes_estoque WHERE id = :id")
                .setParameter("id", salva.getId())
                .getSingleResult();

        // grava o name() do enum, não o valor interno ("saida_uso")
        assertThat(tipoNoBanco).isEqualTo("SAIDA_USO");
    }

    @Test
    void deveAtualizarMovimentacao() {
        MovimentacaoEstoque salva = movimentacaoRepository.saveAndFlush(
                criarMov(farinha, TipoMovimentacao.AJUSTE, base));

        salva.setQuantidade(9.0);
        salva.setMotivo("Contagem física");
        movimentacaoRepository.saveAndFlush(salva);
        entityManager.clear();

        MovimentacaoEstoque r = movimentacaoRepository.findById(salva.getId()).orElseThrow();
        assertThat(r.getQuantidade()).isEqualTo(9.0);
        assertThat(r.getMotivo()).isEqualTo("Contagem física");
    }

    @Test
    void deveDeletarMovimentacao() {
        MovimentacaoEstoque salva = salvarMov(farinha, TipoMovimentacao.ENTRADA, base);

        movimentacaoRepository.deleteById(salva.getId());

        assertThat(movimentacaoRepository.findById(salva.getId())).isEmpty();
    }

    // ---------- findByInsumoEspacoIdAndTipoAndDataMovimentacaoBetween ----------

    @Test
    void deveFiltrarPorTipoNoIntervalo() {
        salvarMov(farinha, TipoMovimentacao.ENTRADA, base);
        salvarMov(acucar, TipoMovimentacao.ENTRADA, base.plusHours(1));
        salvarMov(farinha, TipoMovimentacao.SAIDA_USO, base);

        List<MovimentacaoEstoque> resultado = movimentacaoRepository
                .findByInsumoEspacoIdAndTipoAndDataMovimentacaoBetween(
                        espaco.getId(), TipoMovimentacao.ENTRADA, base.minusDays(1), base.plusDays(1));

        assertThat(resultado).hasSize(2);
        assertThat(resultado).allMatch(m -> m.getTipo() == TipoMovimentacao.ENTRADA);
    }

    @Test
    void naoDeveRetornarMovimentacoesDeOutroEspacoNoBetween() {
        salvarMov(insumoOutroEspaco, TipoMovimentacao.ENTRADA, base);
        salvarMov(farinha, TipoMovimentacao.ENTRADA, base);

        List<MovimentacaoEstoque> resultado = movimentacaoRepository
                .findByInsumoEspacoIdAndTipoAndDataMovimentacaoBetween(
                        espaco.getId(), TipoMovimentacao.ENTRADA, base.minusDays(1), base.plusDays(1));

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getInsumo().getId()).isEqualTo(farinha.getId());
    }

    @Test
    void deveIncluirLimitesDoIntervaloNoBetween() {
        LocalDateTime inicio = base;
        LocalDateTime fim = base.plusDays(1);
        salvarMov(farinha, TipoMovimentacao.ENTRADA, inicio);
        salvarMov(farinha, TipoMovimentacao.ENTRADA, fim);

        List<MovimentacaoEstoque> resultado = movimentacaoRepository
                .findByInsumoEspacoIdAndTipoAndDataMovimentacaoBetween(
                        espaco.getId(), TipoMovimentacao.ENTRADA, inicio, fim);

        assertThat(resultado).hasSize(2);
    }

    @Test
    void naoDeveRetornarMovimentacoesForaDoIntervaloNoBetween() {
        salvarMov(farinha, TipoMovimentacao.ENTRADA, base.minusDays(5));
        salvarMov(farinha, TipoMovimentacao.ENTRADA, base.plusDays(5));

        List<MovimentacaoEstoque> resultado = movimentacaoRepository
                .findByInsumoEspacoIdAndTipoAndDataMovimentacaoBetween(
                        espaco.getId(), TipoMovimentacao.ENTRADA, base.minusDays(1), base.plusDays(1));

        assertThat(resultado).isEmpty();
    }

    // ---------- findByInsumoIdOrderByDataMovimentacaoDesc ----------

    @Test
    void deveListarMovimentacoesDoInsumoDaMaisRecenteParaAMaisAntiga() {
        salvarMov(farinha, TipoMovimentacao.ENTRADA, base.minusDays(2));
        salvarMov(farinha, TipoMovimentacao.SAIDA_USO, base);
        salvarMov(farinha, TipoMovimentacao.AJUSTE, base.minusDays(1));

        List<MovimentacaoEstoque> resultado = movimentacaoRepository
                .findByInsumoIdOrderByDataMovimentacaoDesc(farinha.getId());

        assertThat(resultado).extracting(MovimentacaoEstoque::getDataMovimentacao)
                .containsExactly(base, base.minusDays(1), base.minusDays(2));
    }

    @Test
    void naoDeveListarMovimentacoesDeOutroInsumo() {
        salvarMov(farinha, TipoMovimentacao.ENTRADA, base);
        salvarMov(acucar, TipoMovimentacao.ENTRADA, base);

        List<MovimentacaoEstoque> resultado = movimentacaoRepository
                .findByInsumoIdOrderByDataMovimentacaoDesc(farinha.getId());

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getInsumo().getId()).isEqualTo(farinha.getId());
    }

    @Test
    void deveCarregarInsumoJuntoComEntityGraphNaListagemPorInsumo() {
        salvarMov(farinha, TipoMovimentacao.ENTRADA, base);
        entityManager.flush();
        entityManager.clear();

        List<MovimentacaoEstoque> resultado = movimentacaoRepository
                .findByInsumoIdOrderByDataMovimentacaoDesc(farinha.getId());

        assertThat(resultado).hasSize(1);
        assertThat(Hibernate.isInitialized(resultado.get(0).getInsumo())).isTrue();
        assertThat(resultado.get(0).getInsumo().getNome()).isEqualTo("Farinha");
    }

    @Test
    void deveRetornarListaVaziaParaInsumoSemMovimentacoes() {
        assertThat(movimentacaoRepository.findByInsumoIdOrderByDataMovimentacaoDesc(farinha.getId())).isEmpty();
    }

    @Test
    void deveRetornarListaVaziaParaInsumoInexistente() {
        assertThat(movimentacaoRepository.findByInsumoIdOrderByDataMovimentacaoDesc(999L)).isEmpty();
    }

    // ---------- buscarPorTipos ----------

    @Test
    void deveBuscarApenasOsTiposInformados() {
        salvarMov(farinha, TipoMovimentacao.PERDA_VALIDADE, base);
        salvarMov(farinha, TipoMovimentacao.PERDA_OUTRO, base);
        salvarMov(farinha, TipoMovimentacao.ENTRADA, base);
        salvarMov(farinha, TipoMovimentacao.DOACAO, base);

        List<MovimentacaoEstoque> resultado = movimentacaoRepository.buscarPorTipos(
                espaco.getId(),
                List.of(TipoMovimentacao.PERDA_VALIDADE, TipoMovimentacao.PERDA_OUTRO),
                base.minusDays(1), base.plusDays(1));

        assertThat(resultado).extracting(MovimentacaoEstoque::getTipo)
                .containsExactlyInAnyOrder(TipoMovimentacao.PERDA_VALIDADE, TipoMovimentacao.PERDA_OUTRO);
    }

    @Test
    void deveAceitarUmUnicoTipoNaColecao() {
        salvarMov(farinha, TipoMovimentacao.DOACAO, base);
        salvarMov(farinha, TipoMovimentacao.ENTRADA, base);

        List<MovimentacaoEstoque> resultado = movimentacaoRepository.buscarPorTipos(
                espaco.getId(), Set.of(TipoMovimentacao.DOACAO), base.minusDays(1), base.plusDays(1));

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getTipo()).isEqualTo(TipoMovimentacao.DOACAO);
    }

    @Test
    void naoDeveBuscarPorTiposMovimentacoesDeOutroEspaco() {
        salvarMov(insumoOutroEspaco, TipoMovimentacao.PERDA_OUTRO, base);
        salvarMov(farinha, TipoMovimentacao.PERDA_OUTRO, base);

        List<MovimentacaoEstoque> resultado = movimentacaoRepository.buscarPorTipos(
                espaco.getId(), List.of(TipoMovimentacao.PERDA_OUTRO), base.minusDays(1), base.plusDays(1));

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getInsumo().getId()).isEqualTo(farinha.getId());
    }

    @Test
    void deveIncluirInicioENaoIncluirFimNoIntervaloSemiaberto() {
        LocalDateTime inicio = base;
        LocalDateTime fimExclusivo = base.plusDays(1);
        salvarMov(farinha, TipoMovimentacao.AJUSTE, inicio);        // incluído (>=)
        salvarMov(farinha, TipoMovimentacao.AJUSTE, fimExclusivo);  // excluído (<)

        List<MovimentacaoEstoque> resultado = movimentacaoRepository.buscarPorTipos(
                espaco.getId(), List.of(TipoMovimentacao.AJUSTE), inicio, fimExclusivo);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getDataMovimentacao()).isEqualTo(inicio);
    }

    @Test
    void deveOrdenarBuscarPorTiposDaMaisRecenteParaAMaisAntiga() {
        salvarMov(farinha, TipoMovimentacao.ENTRADA, base.minusHours(5));
        salvarMov(acucar, TipoMovimentacao.SAIDA_USO, base);
        salvarMov(farinha, TipoMovimentacao.AJUSTE, base.minusHours(2));

        List<MovimentacaoEstoque> resultado = movimentacaoRepository.buscarPorTipos(
                espaco.getId(),
                List.of(TipoMovimentacao.ENTRADA, TipoMovimentacao.SAIDA_USO, TipoMovimentacao.AJUSTE),
                base.minusDays(1), base.plusDays(1));

        assertThat(resultado).extracting(MovimentacaoEstoque::getDataMovimentacao)
                .containsExactly(base, base.minusHours(2), base.minusHours(5));
    }

    @Test
    void deveCarregarInsumoJuntoComJoinFetchNoBuscarPorTipos() {
        salvarMov(farinha, TipoMovimentacao.ENTRADA, base);
        entityManager.flush();
        entityManager.clear();

        List<MovimentacaoEstoque> resultado = movimentacaoRepository.buscarPorTipos(
                espaco.getId(), List.of(TipoMovimentacao.ENTRADA), base.minusDays(1), base.plusDays(1));

        assertThat(resultado).hasSize(1);
        assertThat(Hibernate.isInitialized(resultado.get(0).getInsumo())).isTrue();
        assertThat(resultado.get(0).getInsumo().getNome()).isEqualTo("Farinha");
    }

    // ---------- Restrições not null ----------

    @Test
    void naoDeveSalvarMovimentacaoSemInsumo() {
        MovimentacaoEstoque m = criarMov(null, TipoMovimentacao.ENTRADA, base);

        assertThatThrownBy(() -> movimentacaoRepository.saveAndFlush(m))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void naoDeveSalvarMovimentacaoSemTipo() {
        MovimentacaoEstoque m = criarMov(farinha, null, base);

        assertThatThrownBy(() -> movimentacaoRepository.saveAndFlush(m))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void naoDeveSalvarMovimentacaoSemQuantidade() {
        MovimentacaoEstoque m = criarMov(farinha, TipoMovimentacao.ENTRADA, base);
        m.setQuantidade(null);

        assertThatThrownBy(() -> movimentacaoRepository.saveAndFlush(m))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void naoDeveSalvarMovimentacaoSemDataMovimentacao() {
        MovimentacaoEstoque m = criarMov(farinha, TipoMovimentacao.ENTRADA, null);

        assertThatThrownBy(() -> movimentacaoRepository.saveAndFlush(m))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}