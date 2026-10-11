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
import Hisopi.Hisopi.Enum.TipoReceita;
import Hisopi.Hisopi.infra.exception.ErrorResponseWriter;
import Hisopi.Hisopi.model.Espaco;
import Hisopi.Hisopi.model.Insumo;
import Hisopi.Hisopi.model.Receita;
import Hisopi.Hisopi.model.ReceitaInsumo;
import jakarta.persistence.EntityManager;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ReceitaInsumoRepositoryTest {

    @MockitoBean
    private ErrorResponseWriter errorResponseWriter;

    @Autowired
    private ReceitaInsumoRepository receitaInsumoRepository;

    @Autowired
    private ReceitaRepository receitaRepository;

    @Autowired
    private InsumoRepository insumoRepository;

    @Autowired
    private EspacoRepository espacoRepository;

    @Autowired
    private EntityManager entityManager;

    private Espaco espaco;
    private Receita bolo;
    private Receita pao;
    private Insumo farinha;
    private Insumo acucar;

    @BeforeEach
    void setUp() {
        espaco = criarEspaco("Cozinha", TipoEspaco.ORGANIZACAO);

        bolo = criarReceita("Bolo");
        pao = criarReceita("Pão");

        farinha = criarInsumo("Farinha");
        acucar = criarInsumo("Açúcar");
    }

    private Espaco criarEspaco(String nome, TipoEspaco tipo) {
        Espaco e = new Espaco();
        e.setNome(nome);
        e.setTipo(tipo);
        return espacoRepository.save(e);
    }

    private Receita criarReceita(String nome) {
        Receita r = new Receita();
        r.setEspaco(espaco);
        r.setNome(nome);
        r.setTipo(TipoReceita.PRODUTO_VENDA);
        return receitaRepository.save(r);
    }

    private Insumo criarInsumo(String nome) {
        Insumo i = new Insumo();
        i.setEspaco(espaco);
        i.setNome(nome);
        i.setUnidadeMedida("kg");
        i.setEstoqueMinimo(5.0);
        return insumoRepository.save(i);
    }

    private ReceitaInsumo criarItem(Receita receita, Insumo insumo, double quantidade) {
        ReceitaInsumo ri = new ReceitaInsumo();
        ri.setReceita(receita);
        ri.setInsumo(insumo);
        ri.setQuantidadePorUnidade(quantidade);
        return ri;
    }

    private ReceitaInsumo salvarItem(Receita receita, Insumo insumo, double quantidade) {
        return receitaInsumoRepository.save(criarItem(receita, insumo, quantidade));
    }

    @Test
    void deveSalvarItemEGerarId() {
        ReceitaInsumo salvo = receitaInsumoRepository.save(criarItem(bolo, farinha, 0.5));

        assertThat(salvo.getId()).isNotNull();
        assertThat(salvo.getReceita().getId()).isEqualTo(bolo.getId());
        assertThat(salvo.getInsumo().getId()).isEqualTo(farinha.getId());
    }

    @Test
    void devePersistirQuantidadePorUnidade() {
        ReceitaInsumo salvo = receitaInsumoRepository.saveAndFlush(criarItem(bolo, farinha, 0.25));
        entityManager.clear();

        ReceitaInsumo r = receitaInsumoRepository.findById(salvo.getId()).orElseThrow();

        assertThat(r.getQuantidadePorUnidade()).isEqualTo(0.25);
    }

    @Test
    void deveAtualizarQuantidadePorUnidade() {
        ReceitaInsumo salvo = receitaInsumoRepository.saveAndFlush(criarItem(bolo, farinha, 0.5));

        salvo.setQuantidadePorUnidade(1.5);
        receitaInsumoRepository.saveAndFlush(salvo);
        entityManager.clear();

        ReceitaInsumo r = receitaInsumoRepository.findById(salvo.getId()).orElseThrow();
        assertThat(r.getQuantidadePorUnidade()).isEqualTo(1.5);
    }

    @Test
    void deveDeletarItem() {
        ReceitaInsumo salvo = salvarItem(bolo, farinha, 0.5);

        receitaInsumoRepository.deleteById(salvo.getId());

        assertThat(receitaInsumoRepository.findById(salvo.getId())).isEmpty();
    }

    @Test
    void deveListarApenasItensDaReceita() {
        salvarItem(bolo, farinha, 0.5);
        salvarItem(bolo, acucar, 0.2);
        salvarItem(pao, farinha, 1.0);

        List<ReceitaInsumo> resultado = receitaInsumoRepository.findByReceitaId(bolo.getId());

        assertThat(resultado).extracting(ri -> ri.getInsumo().getId())
                .containsExactlyInAnyOrder(farinha.getId(), acucar.getId());
    }

    @Test
    void deveRetornarListaVaziaParaReceitaSemItens() {
        assertThat(receitaInsumoRepository.findByReceitaId(bolo.getId())).isEmpty();
    }

    @Test
    void deveRetornarListaVaziaParaReceitaInexistente() {
        assertThat(receitaInsumoRepository.findByReceitaId(999L)).isEmpty();
    }

    @Test
    void deveBuscarItemPorIdEReceita() {
        ReceitaInsumo salvo = salvarItem(bolo, farinha, 0.5);

        Optional<ReceitaInsumo> resultado = receitaInsumoRepository
                .findByIdAndReceitaId(salvo.getId(), bolo.getId());

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getQuantidadePorUnidade()).isEqualTo(0.5);
    }

    @Test
    void naoDeveBuscarItemDeOutraReceita() {
        ReceitaInsumo salvo = salvarItem(bolo, farinha, 0.5);

        Optional<ReceitaInsumo> resultado = receitaInsumoRepository
                .findByIdAndReceitaId(salvo.getId(), pao.getId());

        assertThat(resultado).isEmpty();
    }

    @Test
    void deveRetornarVazioParaIdInexistente() {
        assertThat(receitaInsumoRepository.findByIdAndReceitaId(999L, bolo.getId())).isEmpty();
    }

    @Test
    void deveRetornarTrueQuandoInsumoJaEstaNaReceita() {
        salvarItem(bolo, farinha, 0.5);

        assertThat(receitaInsumoRepository
                .existsByReceitaIdAndInsumoId(bolo.getId(), farinha.getId())).isTrue();
    }

    @Test
    void deveRetornarFalseParaOutraReceitaOuOutroInsumo() {
        salvarItem(bolo, farinha, 0.5);

        assertThat(receitaInsumoRepository
                .existsByReceitaIdAndInsumoId(pao.getId(), farinha.getId())).isFalse();
        assertThat(receitaInsumoRepository
                .existsByReceitaIdAndInsumoId(bolo.getId(), acucar.getId())).isFalse();
    }

    @Test
    void naoDeveSalvarItemSemReceita() {
        ReceitaInsumo ri = criarItem(null, farinha, 0.5);

        assertThatThrownBy(() -> receitaInsumoRepository.saveAndFlush(ri))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void naoDeveSalvarItemSemInsumo() {
        ReceitaInsumo ri = criarItem(bolo, null, 0.5);

        assertThatThrownBy(() -> receitaInsumoRepository.saveAndFlush(ri))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void naoDeveSalvarItemSemQuantidadePorUnidade() {
        ReceitaInsumo ri = criarItem(bolo, farinha, 0.5);
        ri.setQuantidadePorUnidade(null);

        assertThatThrownBy(() -> receitaInsumoRepository.saveAndFlush(ri))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}