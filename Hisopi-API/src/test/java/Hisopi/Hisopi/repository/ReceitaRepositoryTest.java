package Hisopi.Hisopi.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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

import Hisopi.Hisopi.Enum.TipoEspaco;
import Hisopi.Hisopi.Enum.TipoReceita;
import Hisopi.Hisopi.infra.exception.ErrorResponseWriter;
import Hisopi.Hisopi.model.Espaco;
import Hisopi.Hisopi.model.Receita;
import jakarta.persistence.EntityManager;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ReceitaRepositoryTest {

    @MockitoBean
    private ErrorResponseWriter errorResponseWriter;

    @Autowired
    private ReceitaRepository receitaRepository;

    @Autowired
    private EspacoRepository espacoRepository;

    @Autowired
    private EntityManager entityManager;

    private Espaco espaco;
    private Espaco outroEspaco;

    @BeforeEach
    void setUp() {
        espaco = criarEspaco("Cozinha", TipoEspaco.ORGANIZACAO);
        outroEspaco = criarEspaco("Casa", TipoEspaco.PESSOAL);
    }

    private Espaco criarEspaco(String nome, TipoEspaco tipo) {
        Espaco e = new Espaco();
        e.setNome(nome);
        e.setTipo(tipo);
        return espacoRepository.save(e);
    }

    private Receita criarReceita(String nome, Espaco espaco, TipoReceita tipo) {
        Receita r = new Receita();
        r.setEspaco(espaco);
        r.setNome(nome);
        r.setTipo(tipo);
        return r;
    }

    private Receita salvarReceita(String nome, Espaco espaco, TipoReceita tipo) {
        return receitaRepository.save(criarReceita(nome, espaco, tipo));
    }

    @Test
    void deveSalvarReceitaEGerarId() {
        Receita salva = receitaRepository.save(criarReceita("Bolo", espaco, TipoReceita.PRODUTO_VENDA));

        assertThat(salva.getId()).isNotNull();
        assertThat(salva.getNome()).isEqualTo("Bolo");
        assertThat(salva.getEspaco().getId()).isEqualTo(espaco.getId());
        assertThat(salva.getTipo()).isEqualTo(TipoReceita.PRODUTO_VENDA);
    }

    @Test
    void devePreencherCriadoEmAutomaticamente() {
        LocalDateTime antes = LocalDateTime.now().minusSeconds(1);

        Receita salva = receitaRepository.save(criarReceita("Bolo", espaco, TipoReceita.PRODUTO_VENDA));

        assertThat(salva.getCriadoEm()).isNotNull();
        assertThat(salva.getCriadoEm()).isAfter(antes);
    }

    @Test
    void devePersistirModoPreparoLongo() {
        String textoLongo = "Misture os ingredientes. ".repeat(400); // ~10 mil caracteres
        Receita receita = criarReceita("Bolo", espaco, TipoReceita.PRODUTO_VENDA);
        receita.setModoPreparo(textoLongo);

        Receita salva = receitaRepository.saveAndFlush(receita);
        entityManager.clear();

        Receita r = receitaRepository.findById(salva.getId()).orElseThrow();
        assertThat(r.getModoPreparo()).isEqualTo(textoLongo);
    }

    @Test
    void devePersistirModoPreparoNulo() {
        Receita salva = receitaRepository.saveAndFlush(criarReceita("Bolo", espaco, TipoReceita.PRODUTO_VENDA));
        entityManager.clear();

        Receita r = receitaRepository.findById(salva.getId()).orElseThrow();
        assertThat(r.getModoPreparo()).isNull();
    }

    @ParameterizedTest
    @EnumSource(TipoReceita.class)
    void devePersistirERecuperarTodosOsTipos(TipoReceita tipo) {
        Receita salva = receitaRepository.saveAndFlush(criarReceita("Receita " + tipo, espaco, tipo));
        entityManager.clear();

        Receita r = receitaRepository.findById(salva.getId()).orElseThrow();

        assertThat(r.getTipo()).isEqualTo(tipo);
    }

    @Test
    void devePersistirTipoComoNomeDoEnumNoBanco() {
        Receita salva = receitaRepository.saveAndFlush(
                criarReceita("Sugestão", espaco, TipoReceita.SUGESTAO_CONSUMO));
        entityManager.clear();

        String tipoNoBanco = (String) entityManager
                .createNativeQuery("SELECT tipo FROM receitas WHERE id = :id")
                .setParameter("id", salva.getId())
                .getSingleResult();

        assertThat(tipoNoBanco).isEqualTo("SUGESTAO_CONSUMO");
    }

    @Test
    void devePermitirReceitaSemEspaco() {
        Receita salva = receitaRepository.saveAndFlush(
                criarReceita("Receita Global", null, TipoReceita.SUGESTAO_CONSUMO));
        entityManager.clear();

        Receita r = receitaRepository.findById(salva.getId()).orElseThrow();
        assertThat(r.getEspaco()).isNull();
    }

    @Test
    void deveAtualizarReceita() {
        Receita salva = receitaRepository.saveAndFlush(criarReceita("Bolo", espaco, TipoReceita.PRODUTO_VENDA));

        salva.setNome("Bolo de Cenoura");
        salva.setTipo(TipoReceita.SUGESTAO_CONSUMO);
        salva.setModoPreparo("Asse por 40 minutos");
        receitaRepository.saveAndFlush(salva);
        entityManager.clear();

        Receita r = receitaRepository.findById(salva.getId()).orElseThrow();
        assertThat(r.getNome()).isEqualTo("Bolo de Cenoura");
        assertThat(r.getTipo()).isEqualTo(TipoReceita.SUGESTAO_CONSUMO);
        assertThat(r.getModoPreparo()).isEqualTo("Asse por 40 minutos");
    }

    @Test
    void deveDeletarReceita() {
        Receita salva = salvarReceita("Bolo", espaco, TipoReceita.PRODUTO_VENDA);

        receitaRepository.deleteById(salva.getId());

        assertThat(receitaRepository.findById(salva.getId())).isEmpty();
    }

    @Test
    void deveListarApenasReceitasDoEspaco() {
        salvarReceita("Bolo", espaco, TipoReceita.PRODUTO_VENDA);
        salvarReceita("Pão", espaco, TipoReceita.SUGESTAO_CONSUMO);
        salvarReceita("Sopa", outroEspaco, TipoReceita.PRODUTO_VENDA);

        List<Receita> resultado = receitaRepository.findByEspacoId(espaco.getId());

        assertThat(resultado).extracting(Receita::getNome)
                .containsExactlyInAnyOrder("Bolo", "Pão");
    }

    @Test
    void naoDeveListarReceitasSemEspacoNaBuscaPorEspaco() {
        salvarReceita("Global", null, TipoReceita.SUGESTAO_CONSUMO);
        salvarReceita("Bolo", espaco, TipoReceita.PRODUTO_VENDA);

        List<Receita> resultado = receitaRepository.findByEspacoId(espaco.getId());

        assertThat(resultado).extracting(Receita::getNome).containsExactly("Bolo");
    }

    @Test
    void deveRetornarListaVaziaParaEspacoSemReceitas() {
        assertThat(receitaRepository.findByEspacoId(espaco.getId())).isEmpty();
    }

    @Test
    void deveRetornarListaVaziaParaEspacoInexistente() {
        assertThat(receitaRepository.findByEspacoId(999L)).isEmpty();
    }

    @Test
    void deveListarReceitasPorTipoDeTodosOsEspacos() {
        salvarReceita("Bolo", espaco, TipoReceita.PRODUTO_VENDA);
        salvarReceita("Sopa", outroEspaco, TipoReceita.PRODUTO_VENDA);
        salvarReceita("Pão", espaco, TipoReceita.SUGESTAO_CONSUMO);

        List<Receita> resultado = receitaRepository.findByTipo(TipoReceita.PRODUTO_VENDA);

        assertThat(resultado).extracting(Receita::getNome)
                .containsExactlyInAnyOrder("Bolo", "Sopa");
    }

    @Test
    void deveIncluirReceitasSemEspacoNaBuscaPorTipo() {
        salvarReceita("Global", null, TipoReceita.SUGESTAO_CONSUMO);
        salvarReceita("Pão", espaco, TipoReceita.SUGESTAO_CONSUMO);

        List<Receita> resultado = receitaRepository.findByTipo(TipoReceita.SUGESTAO_CONSUMO);

        assertThat(resultado).extracting(Receita::getNome)
                .containsExactlyInAnyOrder("Global", "Pão");
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoHaReceitasDoTipo() {
        salvarReceita("Bolo", espaco, TipoReceita.PRODUTO_VENDA);

        assertThat(receitaRepository.findByTipo(TipoReceita.SUGESTAO_CONSUMO)).isEmpty();
    }

    @Test
    void deveBuscarReceitaPorIdEEspaco() {
        Receita salva = salvarReceita("Bolo", espaco, TipoReceita.PRODUTO_VENDA);

        Optional<Receita> resultado = receitaRepository.findByIdAndEspacoId(salva.getId(), espaco.getId());

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getNome()).isEqualTo("Bolo");
    }

    @Test
    void naoDeveBuscarReceitaDeOutroEspaco() {
        Receita salva = salvarReceita("Bolo", espaco, TipoReceita.PRODUTO_VENDA);

        Optional<Receita> resultado = receitaRepository
                .findByIdAndEspacoId(salva.getId(), outroEspaco.getId());

        assertThat(resultado).isEmpty();
    }

    @Test
    void naoDeveBuscarReceitaSemEspacoPorIdEEspaco() {
        Receita global = salvarReceita("Global", null, TipoReceita.SUGESTAO_CONSUMO);

        Optional<Receita> resultado = receitaRepository
                .findByIdAndEspacoId(global.getId(), espaco.getId());

        assertThat(resultado).isEmpty();
    }

    @Test
    void deveRetornarVazioParaIdInexistente() {
        assertThat(receitaRepository.findByIdAndEspacoId(999L, espaco.getId())).isEmpty();
    }

    @Test
    void deveFiltrarPorEspacoETipo() {
        salvarReceita("Bolo", espaco, TipoReceita.PRODUTO_VENDA);
        salvarReceita("Torta", espaco, TipoReceita.PRODUTO_VENDA);
        salvarReceita("Pão", espaco, TipoReceita.SUGESTAO_CONSUMO);
        salvarReceita("Sopa", outroEspaco, TipoReceita.PRODUTO_VENDA);

        List<Receita> resultado = receitaRepository
                .findByEspacoIdAndTipo(espaco.getId(), TipoReceita.PRODUTO_VENDA);

        assertThat(resultado).extracting(Receita::getNome)
                .containsExactlyInAnyOrder("Bolo", "Torta");
    }

    @Test
    void deveRetornarListaVaziaQuandoEspacoNaoTemReceitasDoTipo() {
        salvarReceita("Bolo", espaco, TipoReceita.PRODUTO_VENDA);

        assertThat(receitaRepository
                .findByEspacoIdAndTipo(espaco.getId(), TipoReceita.SUGESTAO_CONSUMO)).isEmpty();
    }

    @Test
    void naoDeveSalvarReceitaSemNome() {
        Receita r = criarReceita(null, espaco, TipoReceita.PRODUTO_VENDA);

        assertThatThrownBy(() -> receitaRepository.saveAndFlush(r))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void naoDeveSalvarReceitaSemTipo() {
        Receita r = criarReceita("Bolo", espaco, null);

        assertThatThrownBy(() -> receitaRepository.saveAndFlush(r))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}