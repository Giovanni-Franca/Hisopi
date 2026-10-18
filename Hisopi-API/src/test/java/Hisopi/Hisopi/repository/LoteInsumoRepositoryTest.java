package Hisopi.Hisopi.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.hibernate.Hibernate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import Hisopi.Hisopi.Enum.TipoEspaco;
import Hisopi.Hisopi.infra.exception.ErrorResponseWriter;
import Hisopi.Hisopi.model.Espaco;
import Hisopi.Hisopi.model.Insumo;
import Hisopi.Hisopi.model.LoteInsumo;
import jakarta.persistence.EntityManager;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class LoteInsumoRepositoryTest {

    @MockitoBean
    private ErrorResponseWriter errorResponseWriter;

    @Autowired
    private LoteInsumoRepository loteRepository;

    @Autowired
    private InsumoRepository insumoRepository;

    @Autowired
    private EspacoRepository espacoRepository;

    @Autowired
    private EntityManager entityManager;

    private Espaco espaco;
    private Espaco outroEspaco;
    private Insumo farinha;
    private Insumo acucar;
    private Insumo insumoOutroEspaco;

    private final LocalDate hoje = LocalDate.of(2026, 10, 11);

    @BeforeEach
    void setUp() {
        espaco = criarEspaco("Cozinha", TipoEspaco.ORGANIZACAO);
        outroEspaco = criarEspaco("Casa", TipoEspaco.PESSOAL);

        farinha = criarInsumo("Farinha", espaco);
        acucar = criarInsumo("Açúcar", espaco);
        insumoOutroEspaco = criarInsumo("Arroz", outroEspaco);
    }

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

    private LoteInsumo criarLote(Insumo insumo, double quantidadeAtual, LocalDate validade) {
        LoteInsumo l = new LoteInsumo();
        l.setInsumo(insumo);
        l.setQuantidadeInicial(10.0);
        l.setQuantidadeAtual(quantidadeAtual);
        l.setDataEntrada(hoje.minusDays(30));
        l.setDataValidade(validade);
        return l;
    }

    private LoteInsumo salvarLote(Insumo insumo, double quantidadeAtual, LocalDate validade) {
        return loteRepository.save(criarLote(insumo, quantidadeAtual, validade));
    }

    @Test
    void deveSalvarLoteEGerarIdEVersion() {
        LoteInsumo salvo = loteRepository.saveAndFlush(criarLote(farinha, 10.0, hoje.plusDays(10)));

        assertThat(salvo.getId()).isNotNull();
        assertThat(salvo.getVersion()).isNotNull();
        assertThat(salvo.getInsumo().getId()).isEqualTo(farinha.getId());
    }

    @Test
    void devePersistirTodosOsCampos() {
        LoteInsumo lote = criarLote(farinha, 8.0, hoje.plusDays(15));
        lote.setFornecedor("Fornecedor X");

        LoteInsumo salvo = loteRepository.saveAndFlush(lote);
        entityManager.clear();

        LoteInsumo r = loteRepository.findById(salvo.getId()).orElseThrow();
        assertThat(r.getQuantidadeInicial()).isEqualTo(10.0);
        assertThat(r.getQuantidadeAtual()).isEqualTo(8.0);
        assertThat(r.getDataEntrada()).isEqualTo(hoje.minusDays(30));
        assertThat(r.getDataValidade()).isEqualTo(hoje.plusDays(15));
        assertThat(r.getFornecedor()).isEqualTo("Fornecedor X");
    }

    @Test
    void devePersistirFornecedorNulo() {
        LoteInsumo salvo = loteRepository.saveAndFlush(criarLote(farinha, 10.0, hoje.plusDays(10)));
        entityManager.clear();

        LoteInsumo r = loteRepository.findById(salvo.getId()).orElseThrow();
        assertThat(r.getFornecedor()).isNull();
    }

    @Test
    void deveIncrementarVersionAoAtualizar() {
        LoteInsumo salvo = loteRepository.saveAndFlush(criarLote(farinha, 10.0, hoje.plusDays(10)));
        Long versionInicial = salvo.getVersion();

        salvo.setQuantidadeAtual(7.0);
        loteRepository.saveAndFlush(salvo);
        entityManager.clear();

        LoteInsumo atualizado = loteRepository.findById(salvo.getId()).orElseThrow();
        assertThat(atualizado.getQuantidadeAtual()).isEqualTo(7.0);
        assertThat(atualizado.getVersion()).isGreaterThan(versionInicial);
    }

    @Test
    void deveLancarExcecaoDeLockOtimistaEmAtualizacaoConcorrente() {
        LoteInsumo salvo = loteRepository.saveAndFlush(criarLote(farinha, 10.0, hoje.plusDays(10)));
        Long id = salvo.getId();
        entityManager.clear();

        LoteInsumo copiaA = loteRepository.findById(id).orElseThrow();
        entityManager.detach(copiaA);
        LoteInsumo copiaB = loteRepository.findById(id).orElseThrow();
        entityManager.detach(copiaB);

        copiaA.setQuantidadeAtual(5.0);
        loteRepository.saveAndFlush(copiaA);

        copiaB.setQuantidadeAtual(3.0);

        assertThatThrownBy(() -> loteRepository.saveAndFlush(copiaB))
                .isInstanceOf(ObjectOptimisticLockingFailureException.class);
    }

    @Test
    void deveDeletarLote() {
        LoteInsumo salvo = salvarLote(farinha, 10.0, hoje.plusDays(10));

        loteRepository.deleteById(salvo.getId());

        assertThat(loteRepository.findById(salvo.getId())).isEmpty();
    }

    @Test
    void deveBuscarLotesVencidosOuVencendoOrdenadosPorValidade() {
        salvarLote(farinha, 5.0, hoje.plusDays(3));   // vencendo
        salvarLote(acucar, 5.0, hoje.minusDays(2));   // vencido
        salvarLote(farinha, 5.0, hoje.plusDays(30));  // fora do limite

        List<LoteInsumo> resultado = loteRepository
                .buscarVencidosOuVencendo(espaco.getId(), hoje.plusDays(7));

        assertThat(resultado).extracting(LoteInsumo::getDataValidade)
                .containsExactly(hoje.minusDays(2), hoje.plusDays(3));
    }

    @Test
    void deveIncluirLoteComValidadeIgualAoLimite() {
        LocalDate limite = hoje.plusDays(7);
        salvarLote(farinha, 5.0, limite);

        List<LoteInsumo> resultado = loteRepository.buscarVencidosOuVencendo(espaco.getId(), limite);

        assertThat(resultado).hasSize(1);
    }

    @Test
    void naoDeveBuscarVencidosComQuantidadeAtualZerada() {
        salvarLote(farinha, 0.0, hoje.minusDays(1));

        List<LoteInsumo> resultado = loteRepository
                .buscarVencidosOuVencendo(espaco.getId(), hoje.plusDays(7));

        assertThat(resultado).isEmpty();
    }

    @Test
    void naoDeveBuscarVencidosDeOutroEspaco() {
        salvarLote(insumoOutroEspaco, 5.0, hoje.minusDays(1));
        salvarLote(farinha, 5.0, hoje.minusDays(1));

        List<LoteInsumo> resultado = loteRepository
                .buscarVencidosOuVencendo(espaco.getId(), hoje.plusDays(7));

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getInsumo().getId()).isEqualTo(farinha.getId());
    }

    @Test
    void deveCarregarInsumoJuntoComJoinFetch() {
        salvarLote(farinha, 5.0, hoje.minusDays(1));
        entityManager.flush();
        entityManager.clear();

        List<LoteInsumo> resultado = loteRepository
                .buscarVencidosOuVencendo(espaco.getId(), hoje.plusDays(7));

        assertThat(resultado).hasSize(1);
        assertThat(Hibernate.isInitialized(resultado.get(0).getInsumo())).isTrue();
        assertThat(resultado.get(0).getInsumo().getNome()).isEqualTo("Farinha");
    }

    @Test
    void deveListarLotesDoInsumoEmOrdemFefo() {
        salvarLote(farinha, 5.0, hoje.plusDays(20));
        salvarLote(farinha, 5.0, hoje.plusDays(5));
        salvarLote(farinha, 5.0, hoje.plusDays(10));

        List<LoteInsumo> resultado = loteRepository
                .findByInsumoIdAndQuantidadeAtualGreaterThanOrderByDataValidadeAsc(farinha.getId(), 0.0);

        assertThat(resultado).extracting(LoteInsumo::getDataValidade)
                .containsExactly(hoje.plusDays(5), hoje.plusDays(10), hoje.plusDays(20));
    }

    @Test
    void naoDeveListarLotesZeradosNemDeOutroInsumoNoFefo() {
        salvarLote(farinha, 0.0, hoje.plusDays(5));
        salvarLote(farinha, 3.0, hoje.plusDays(10));
        salvarLote(acucar, 4.0, hoje.plusDays(2));

        List<LoteInsumo> resultado = loteRepository
                .findByInsumoIdAndQuantidadeAtualGreaterThanOrderByDataValidadeAsc(farinha.getId(), 0.0);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getQuantidadeAtual()).isEqualTo(3.0);
    }

    @Test
    void deveRetornarListaVaziaNoFefoParaInsumoInexistente() {
        List<LoteInsumo> resultado = loteRepository
                .findByInsumoIdAndQuantidadeAtualGreaterThanOrderByDataValidadeAsc(999L, 0.0);

        assertThat(resultado).isEmpty();
    }

    @Test
    void deveBuscarLotesDentroDoIntervaloDeValidade() {
        salvarLote(farinha, 5.0, hoje.plusDays(5));
        salvarLote(acucar, 5.0, hoje.plusDays(10));
        salvarLote(farinha, 5.0, hoje.plusDays(40)); // fora

        List<LoteInsumo> resultado = loteRepository
                .findByInsumoEspacoIdAndDataValidadeBetweenAndQuantidadeAtualGreaterThan(
                        espaco.getId(), hoje, hoje.plusDays(15), 0.0);

        assertThat(resultado).hasSize(2);
    }

    @Test
    void deveIncluirLimitesDoIntervaloNoBetween() {
        salvarLote(farinha, 5.0, hoje);
        salvarLote(farinha, 5.0, hoje.plusDays(15));

        List<LoteInsumo> resultado = loteRepository
                .findByInsumoEspacoIdAndDataValidadeBetweenAndQuantidadeAtualGreaterThan(
                        espaco.getId(), hoje, hoje.plusDays(15), 0.0);

        assertThat(resultado).hasSize(2);
    }

    @Test
    void naoDeveBuscarNoIntervaloLotesZeradosOuDeOutroEspaco() {
        salvarLote(farinha, 0.0, hoje.plusDays(5));
        salvarLote(insumoOutroEspaco, 5.0, hoje.plusDays(5));

        List<LoteInsumo> resultado = loteRepository
                .findByInsumoEspacoIdAndDataValidadeBetweenAndQuantidadeAtualGreaterThan(
                        espaco.getId(), hoje, hoje.plusDays(15), 0.0);

        assertThat(resultado).isEmpty();
    }

    @Test
    void deveBuscarLotePorIdEEspaco() {
        LoteInsumo salvo = salvarLote(farinha, 5.0, hoje.plusDays(5));

        Optional<LoteInsumo> resultado = loteRepository.findByIdAndInsumoEspacoId(salvo.getId(), espaco.getId());

        assertThat(resultado).isPresent();
    }

    @Test
    void naoDeveBuscarLoteDeOutroEspaco() {
        LoteInsumo salvo = salvarLote(farinha, 5.0, hoje.plusDays(5));

        Optional<LoteInsumo> resultado = loteRepository.findByIdAndInsumoEspacoId(salvo.getId(), outroEspaco.getId());

        assertThat(resultado).isEmpty();
    }

    @Test
    void naoDeveSalvarLoteSemInsumo() {
        LoteInsumo lote = criarLote(null, 5.0, hoje.plusDays(5));

        assertThatThrownBy(() -> loteRepository.saveAndFlush(lote))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void naoDeveSalvarLoteSemQuantidadeInicial() {
        LoteInsumo lote = criarLote(farinha, 5.0, hoje.plusDays(5));
        lote.setQuantidadeInicial(null);

        assertThatThrownBy(() -> loteRepository.saveAndFlush(lote))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void naoDeveSalvarLoteSemQuantidadeAtual() {
        LoteInsumo lote = criarLote(farinha, 5.0, hoje.plusDays(5));
        lote.setQuantidadeAtual(null);

        assertThatThrownBy(() -> loteRepository.saveAndFlush(lote))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void naoDeveSalvarLoteSemDataEntrada() {
        LoteInsumo lote = criarLote(farinha, 5.0, hoje.plusDays(5));
        lote.setDataEntrada(null);

        assertThatThrownBy(() -> loteRepository.saveAndFlush(lote))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void naoDeveSalvarLoteSemDataValidade() {
        LoteInsumo lote = criarLote(farinha, 5.0, hoje.plusDays(5));
        lote.setDataValidade(null);

        assertThatThrownBy(() -> loteRepository.saveAndFlush(lote))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}