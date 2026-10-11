package Hisopi.Hisopi.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import Hisopi.Hisopi.Enum.TipoEspaco;
import Hisopi.Hisopi.infra.exception.ErrorResponseWriter;
import Hisopi.Hisopi.model.Espaco;
import Hisopi.Hisopi.model.Insumo;
import jakarta.persistence.EntityManager;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class InsumoRepositoryTest {

    @MockitoBean
    private ErrorResponseWriter errorResponseWriter;

    @Autowired
    private InsumoRepository insumoRepository;

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

    private Insumo criarInsumo(String nome, Espaco espaco, boolean ativo) {
        Insumo i = new Insumo();
        i.setEspaco(espaco);
        i.setNome(nome);
        i.setUnidadeMedida("kg");
        i.setEstoqueMinimo(5.0);
        i.setAtivo(ativo);
        return i;
    }

    @Test
    void deveSalvarInsumoEGerarId() {
        Insumo salvo = insumoRepository.save(criarInsumo("Farinha", espaco, true));

        assertThat(salvo.getId()).isNotNull();
        assertThat(salvo.getNome()).isEqualTo("Farinha");
        assertThat(salvo.getEspaco().getId()).isEqualTo(espaco.getId());
    }

    @Test
    void deveAplicarValoresPadrao() {
        Insumo salvo = insumoRepository.saveAndFlush(criarInsumo("Açúcar", espaco, true));
        entityManager.clear();

        Insumo recuperado = insumoRepository.findById(salvo.getId()).orElseThrow();

        assertThat(recuperado.getEstoqueAtual()).isEqualTo(0.0);
        assertThat(recuperado.getAtivo()).isTrue();
        assertThat(recuperado.getCriadoEm()).isNotNull();
    }

    @Test
    void devePersistirCamposOpcionaisNulos() {
        Insumo salvo = insumoRepository.saveAndFlush(criarInsumo("Sal", espaco, true));
        entityManager.clear();

        Insumo recuperado = insumoRepository.findById(salvo.getId()).orElseThrow();

        assertThat(recuperado.getCategoria()).isNull();
        assertThat(recuperado.getCustoUnitario()).isNull();
    }

    @Test
    void devePersistirTodosOsCampos() {
        Insumo insumo = criarInsumo("Leite", espaco, true);
        insumo.setCategoria("Laticínios");
        insumo.setUnidadeMedida("l");
        insumo.setEstoqueAtual(12.5);
        insumo.setEstoqueMinimo(3.0);
        insumo.setCustoUnitario(4.99);

        Insumo salvo = insumoRepository.saveAndFlush(insumo);
        entityManager.clear();

        Insumo r = insumoRepository.findById(salvo.getId()).orElseThrow();
        assertThat(r.getCategoria()).isEqualTo("Laticínios");
        assertThat(r.getUnidadeMedida()).isEqualTo("l");
        assertThat(r.getEstoqueAtual()).isEqualTo(12.5);
        assertThat(r.getEstoqueMinimo()).isEqualTo(3.0);
        assertThat(r.getCustoUnitario()).isEqualTo(4.99);
    }

    @Test
    void deveAtualizarInsumo() {
        Insumo salvo = insumoRepository.saveAndFlush(criarInsumo("Farinha", espaco, true));

        salvo.setNome("Farinha Integral");
        salvo.setEstoqueAtual(20.0);
        insumoRepository.saveAndFlush(salvo);
        entityManager.clear();

        Insumo atualizado = insumoRepository.findById(salvo.getId()).orElseThrow();
        assertThat(atualizado.getNome()).isEqualTo("Farinha Integral");
        assertThat(atualizado.getEstoqueAtual()).isEqualTo(20.0);
    }

    @Test
    void deveDeletarInsumo() {
        Insumo salvo = insumoRepository.save(criarInsumo("Farinha", espaco, true));

        insumoRepository.deleteById(salvo.getId());

        assertThat(insumoRepository.findById(salvo.getId())).isEmpty();
    }

    @Test
    void deveListarApenasInsumosAtivosDoEspaco() {
        insumoRepository.save(criarInsumo("Farinha", espaco, true));
        insumoRepository.save(criarInsumo("Açúcar", espaco, true));
        insumoRepository.save(criarInsumo("Inativo", espaco, false));
        insumoRepository.save(criarInsumo("Outro Espaço", outroEspaco, true));

        List<Insumo> resultado = insumoRepository.findByEspacoIdAndAtivoTrue(espaco.getId());

        assertThat(resultado).extracting(Insumo::getNome)
                .containsExactlyInAnyOrder("Farinha", "Açúcar");
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoHaInsumosAtivos() {
        insumoRepository.save(criarInsumo("Inativo", espaco, false));

        List<Insumo> resultado = insumoRepository.findByEspacoIdAndAtivoTrue(espaco.getId());

        assertThat(resultado).isEmpty();
    }

    @Test
    void deveRetornarListaVaziaParaEspacoInexistente() {
        List<Insumo> resultado = insumoRepository.findByEspacoIdAndAtivoTrue(999L);

        assertThat(resultado).isEmpty();
    }

    @Test
    void deveBuscarPorIdEEspaco() {
        Insumo salvo = insumoRepository.save(criarInsumo("Farinha", espaco, true));

        Optional<Insumo> resultado = insumoRepository.findByIdAndEspacoId(salvo.getId(), espaco.getId());

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getNome()).isEqualTo("Farinha");
    }

    @Test
    void naoDeveBuscarInsumoDeOutroEspaco() {
        Insumo salvo = insumoRepository.save(criarInsumo("Farinha", espaco, true));

        Optional<Insumo> resultado = insumoRepository.findByIdAndEspacoId(salvo.getId(), outroEspaco.getId());

        assertThat(resultado).isEmpty();
    }

    @Test
    void deveEncontrarInsumoInativoPorIdEEspaco() {
        Insumo salvo = insumoRepository.save(criarInsumo("Inativo", espaco, false));

        Optional<Insumo> resultado = insumoRepository.findByIdAndEspacoId(salvo.getId(), espaco.getId());

        assertThat(resultado).isPresent();
    }

    @Test
    void naoDeveSalvarInsumoSemEspaco() {
        Insumo insumo = criarInsumo("Farinha", null, true);

        assertThatThrownBy(() -> insumoRepository.saveAndFlush(insumo))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void naoDeveSalvarInsumoSemNome() {
        Insumo insumo = criarInsumo(null, espaco, true);

        assertThatThrownBy(() -> insumoRepository.saveAndFlush(insumo))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void naoDeveSalvarInsumoSemUnidadeMedida() {
        Insumo insumo = criarInsumo("Farinha", espaco, true);
        insumo.setUnidadeMedida(null);

        assertThatThrownBy(() -> insumoRepository.saveAndFlush(insumo))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void naoDeveSalvarInsumoSemEstoqueMinimo() {
        Insumo insumo = criarInsumo("Farinha", espaco, true);
        insumo.setEstoqueMinimo(null);

        assertThatThrownBy(() -> insumoRepository.saveAndFlush(insumo))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void naoDeveSalvarInsumoSemEstoqueAtual() {
        Insumo insumo = criarInsumo("Farinha", espaco, true);
        insumo.setEstoqueAtual(null);

        assertThatThrownBy(() -> insumoRepository.saveAndFlush(insumo))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void naoDeveSalvarInsumoSemAtivo() {
        Insumo insumo = criarInsumo("Farinha", espaco, true);
        insumo.setAtivo(null);

        assertThatThrownBy(() -> insumoRepository.saveAndFlush(insumo))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}