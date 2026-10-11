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
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import Hisopi.Hisopi.Enum.TipoEspaco;
import Hisopi.Hisopi.infra.exception.ErrorResponseWriter;
import Hisopi.Hisopi.model.Espaco;
import jakarta.persistence.EntityManager;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class EspacoRepositoryTest {
	@MockitoBean
    private ErrorResponseWriter errorResponseWriter;
	@Autowired
    private EspacoRepository espacoRepository;

    @Autowired
    private EntityManager entityManager;

    private Espaco espaco;

    @BeforeEach
    void setUp() {
        espaco = criarEspaco("Meu Espaço", TipoEspaco.PESSOAL);
    }

    private Espaco criarEspaco(String nome, TipoEspaco tipo) {
        Espaco e = new Espaco();
        e.setNome(nome);
        e.setTipo(tipo);
        return e;
    }

    @Test
    void deveSalvarEspacoEGerarId() {
        Espaco salvo = espacoRepository.save(espaco);

        assertThat(salvo.getId()).isNotNull();
        assertThat(salvo.getNome()).isEqualTo("Meu Espaço");
        assertThat(salvo.getTipo()).isEqualTo(TipoEspaco.PESSOAL);
    }

    @Test
    void devePreencherCriadoEmAutomaticamente() {
        LocalDateTime antes = LocalDateTime.now().minusSeconds(1);

        Espaco salvo = espacoRepository.save(espaco);

        assertThat(salvo.getCriadoEm()).isNotNull();
        assertThat(salvo.getCriadoEm()).isAfter(antes);
    }

    @Test
    void deveBuscarPorId() {
        Espaco salvo = espacoRepository.save(espaco);

        Optional<Espaco> resultado = espacoRepository.findById(salvo.getId());

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getNome()).isEqualTo("Meu Espaço");
    }

    @Test
    void deveRetornarVazioQuandoIdNaoExiste() {
        Optional<Espaco> resultado = espacoRepository.findById(999L);

        assertThat(resultado).isEmpty();
    }

    @Test
    void deveListarTodos() {
        espacoRepository.save(espaco);
        espacoRepository.save(criarEspaco("Minha Empresa", TipoEspaco.ORGANIZACAO));

        List<Espaco> todos = espacoRepository.findAll();

        assertThat(todos).hasSize(2);
        assertThat(todos).extracting(Espaco::getTipo)
                .containsExactlyInAnyOrder(TipoEspaco.PESSOAL, TipoEspaco.ORGANIZACAO);
    }

    @Test
    void deveAtualizarEspaco() {
        Espaco salvo = espacoRepository.saveAndFlush(espaco);

        salvo.setNome("Nome Atualizado");
        salvo.setTipo(TipoEspaco.ORGANIZACAO);
        espacoRepository.saveAndFlush(salvo);
        entityManager.clear();

        Espaco atualizado = espacoRepository.findById(salvo.getId()).orElseThrow();
        assertThat(atualizado.getNome()).isEqualTo("Nome Atualizado");
        assertThat(atualizado.getTipo()).isEqualTo(TipoEspaco.ORGANIZACAO);
    }

    @Test
    void deveDeletarEspaco() {
        Espaco salvo = espacoRepository.save(espaco);

        espacoRepository.deleteById(salvo.getId());

        assertThat(espacoRepository.findById(salvo.getId())).isEmpty();
        assertThat(espacoRepository.count()).isZero();
    }

    @ParameterizedTest
    @EnumSource(TipoEspaco.class)
    void devePersistirERecuperarTodosOsTiposDeEspaco(TipoEspaco tipo) {
        Espaco salvo = espacoRepository.saveAndFlush(criarEspaco("Espaço " + tipo, tipo));
        entityManager.clear();

        Espaco recuperado = espacoRepository.findById(salvo.getId()).orElseThrow();

        assertThat(recuperado.getTipo()).isEqualTo(tipo);
    }

    @Test
    void devePersistirTipoComoNomeDoEnumNoBanco() {
        Espaco salvo = espacoRepository.saveAndFlush(criarEspaco("Empresa", TipoEspaco.ORGANIZACAO));
        entityManager.clear();

        String tipoNoBanco = (String) entityManager
                .createNativeQuery("SELECT tipo FROM espacos WHERE id = :id")
                .setParameter("id", salvo.getId())
                .getSingleResult();

        assertThat(tipoNoBanco).isEqualTo("ORGANIZACAO");
    }

    @Test
    void naoDeveSalvarEspacoSemNome() {
        espaco.setNome(null);

        assertThatThrownBy(() -> espacoRepository.saveAndFlush(espaco))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void naoDeveSalvarEspacoSemTipo() {
        espaco.setTipo(null);

        assertThatThrownBy(() -> espacoRepository.saveAndFlush(espaco))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
