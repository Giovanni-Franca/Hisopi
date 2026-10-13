package Hisopi.Hisopi.controller;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import Hisopi.Hisopi.Enum.PapelMembro;
import Hisopi.Hisopi.Enum.TipoMovimentacao;
import Hisopi.Hisopi.model.Espaco;
import Hisopi.Hisopi.model.Insumo;
import Hisopi.Hisopi.model.LoteInsumo;
import Hisopi.Hisopi.model.MembroEspaco;
import Hisopi.Hisopi.model.MovimentacaoEstoque;
import Hisopi.Hisopi.model.Usuario;
import Hisopi.Hisopi.repository.EspacoRepository;
import Hisopi.Hisopi.repository.InsumoRepository;
import Hisopi.Hisopi.repository.LoteInsumoRepository;
import Hisopi.Hisopi.repository.MembroEspacoRepository;
import Hisopi.Hisopi.repository.MovimentacaoEstoqueRepository;

@SpringBootTest
@AutoConfigureMockMvc
public class InsumoControllerTest {

	private static final int STATUS_REGRA_NEGOCIO = 400;

	private static final String CATEGORIA = "ALIMENTO";
	private static final String UNIDADE = "KG";

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean private MembroEspacoRepository repMembro;
	@MockitoBean private InsumoRepository repInsumo;
	@MockitoBean private LoteInsumoRepository repLote;
	@MockitoBean private MovimentacaoEstoqueRepository repMov;
	@MockitoBean private EspacoRepository repEspaco;

	private Usuario logado;

	@BeforeEach
	void prepararUsuarioLogado() {
		logado = mock(Usuario.class);
		when(logado.getId()).thenReturn(1L);
	}

	// =================================================================
	// POST /espacos/{id}/insumos  (mínimo: GERENTE)
	// =================================================================

	@Test
	void criarInsumoSalvaComEstoqueZeradoEAtivo() throws Exception {
		logadoComPapel(PapelMembro.GERENTE);
		Espaco espaco = new Espaco();
		espaco.setNome("Cozinha");
		when(repEspaco.findById(1L)).thenReturn(Optional.of(espaco));

		String corpo = json("nome", q("Farinha"), "categoria", q(CATEGORIA), "unidadeMedida", q(UNIDADE),
			"estoqueMinimo", "5", "custoUnitario", "2.5", "estoqueAtual", "999", "ativo", "false");

		comoLogado(requisicao("POST", "/espacos/1/insumos", corpo))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.nome").value("Farinha"))
			.andExpect(jsonPath("$.estoqueAtual").value(0.0))
			.andExpect(jsonPath("$.estoqueMinimo").value(5.0))
			.andExpect(jsonPath("$.custoUnitario").value(2.5))
			.andExpect(jsonPath("$.ativo").value(true));

		ArgumentCaptor<Insumo> salvo = ArgumentCaptor.forClass(Insumo.class);
		verify(repInsumo).save(salvo.capture());
		assertEquals("Farinha", salvo.getValue().getNome());
		assertSame(espaco, salvo.getValue().getEspaco());
	}

	@Test
	void criarInsumoEmEspacoInexistenteRetorna404() throws Exception {
		logadoComPapel(PapelMembro.GERENTE);
		when(repEspaco.findById(1L)).thenReturn(Optional.empty());

		comoLogado(requisicao("POST", "/espacos/1/insumos", insumoJson()))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.mensagem").value("Espaço não encontrado"));

		verify(repInsumo, never()).save(any());
	}

	@ParameterizedTest(name = "{0} cria insumo -> HTTP {1}")
	@CsvSource({ "OPERADOR, 403", "GERENTE, 200", "ADMIN, 200", "DONO, 200" })
	void papelMinimoParaCriarInsumoEGerente(PapelMembro papel, int esperado) throws Exception {
		logadoComPapel(papel);
		when(repEspaco.findById(1L)).thenReturn(Optional.of(new Espaco()));

		comoLogado(requisicao("POST", "/espacos/1/insumos", insumoJson()))
			.andExpect(status().is(esperado));
	}

	// =================================================================
	// GET /espacos/{id}/insumos  e  /baixoEstoque
	// =================================================================

	@Test
	void listarInsumosRetornaOsAtivosDoEspaco() throws Exception {
		logadoComPapel(PapelMembro.OPERADOR);
		when(repInsumo.findByEspacoIdAndAtivoTrue(1L)).thenReturn(List.of(
			insumoReal("Farinha", 10.0, 5.0),
			insumoReal("Açúcar", 3.0, 5.0)));

		comoLogado(get("/espacos/1/insumos"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(2))
			.andExpect(jsonPath("$[0].nome").value("Farinha"));

		verify(repInsumo).findByEspacoIdAndAtivoTrue(1L);
	}

	/** Baixo estoque = estoqueAtual <= estoqueMinimo (o limite exato conta). */
	@Test
	void baixoEstoqueIncluiAbaixoEIgualAoMinimoEExcluiAcima() throws Exception {
		logadoComPapel(PapelMembro.OPERADOR);
		when(repInsumo.findByEspacoIdAndAtivoTrue(1L)).thenReturn(List.of(
			insumoReal("Abaixo", 2.0, 5.0),
			insumoReal("NoLimite", 5.0, 5.0),
			insumoReal("Acima", 6.0, 5.0)));

		comoLogado(get("/espacos/1/insumos/baixoEstoque"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[*].nome", contains("Abaixo", "NoLimite")));
	}

	@Test
	void baixoEstoqueSemNenhumInsumoAbaixoRetornaListaVazia() throws Exception {
		logadoComPapel(PapelMembro.OPERADOR);
		when(repInsumo.findByEspacoIdAndAtivoTrue(1L)).thenReturn(List.of(insumoReal("Cheio", 50.0, 5.0)));

		comoLogado(get("/espacos/1/insumos/baixoEstoque"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$").isEmpty());
	}

	// =================================================================
	// GET /espacos/{id}/insumos/{id}
	// =================================================================

	@Test
	void buscarInsumoRetornaOInsumoDoEspaco() throws Exception {
		logadoComPapel(PapelMembro.OPERADOR);
		when(repInsumo.findByIdAndEspacoId(5L, 1L)).thenReturn(Optional.of(insumoReal("Farinha", 10.0, 5.0)));

		comoLogado(get("/espacos/1/insumos/5"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.nome").value("Farinha"))
			.andExpect(jsonPath("$.estoqueAtual").value(10.0));

		verify(repInsumo).findByIdAndEspacoId(5L, 1L); // busca já filtrada pelo espaço (IDOR)
	}

	@Test
	void buscarInsumoInexistenteRetorna404() throws Exception {
		logadoComPapel(PapelMembro.OPERADOR);
		when(repInsumo.findByIdAndEspacoId(5L, 1L)).thenReturn(Optional.empty());

		comoLogado(get("/espacos/1/insumos/5"))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.status").value(404))
			.andExpect(jsonPath("$.mensagem").value("Insumo não encontrado"));
	}

	@Test
	void idInsumoNaoNumericoRetorna400() throws Exception {
		logadoComPapel(PapelMembro.OPERADOR);

		comoLogado(get("/espacos/1/insumos/abc"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.status").value(400));
	}

	// =================================================================
	// PUT /espacos/{id}/insumos/{id}  (mínimo: GERENTE)
	// =================================================================

	@Test
	void editarInsumoAtualizaCamposMasPreservaEstoqueEAtivo() throws Exception {
		logadoComPapel(PapelMembro.GERENTE);
		Insumo existente = insumoReal("Farinha", 7.0, 5.0);
		when(repInsumo.findByIdAndEspacoId(5L, 1L)).thenReturn(Optional.of(existente));

		String corpo = json("nome", q("Farinha Integral"), "categoria", q(CATEGORIA), "unidadeMedida", q(UNIDADE),
			"estoqueMinimo", "8", "custoUnitario", "3.2");

		comoLogado(requisicao("PUT", "/espacos/1/insumos/5", corpo))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.nome").value("Farinha Integral"))
			.andExpect(jsonPath("$.estoqueMinimo").value(8.0))
			.andExpect(jsonPath("$.custoUnitario").value(3.2))
			// editar não mexe no saldo nem reativa/desativa
			.andExpect(jsonPath("$.estoqueAtual").value(7.0))
			.andExpect(jsonPath("$.ativo").value(true));

		verify(repInsumo).save(existente);
	}

	@Test
	void editarInsumoDeOutroEspacoRetorna404SemSalvar() throws Exception {
		logadoComPapel(PapelMembro.GERENTE);
		when(repInsumo.findByIdAndEspacoId(5L, 1L)).thenReturn(Optional.empty());

		comoLogado(requisicao("PUT", "/espacos/1/insumos/5", insumoJson()))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.mensagem").value("Insumo não encontrado"));

		verify(repInsumo, never()).save(any());
	}

	// =================================================================
	// DELETE /espacos/{id}/insumos/{id}  (desativa, não apaga)
	// =================================================================

	@Test
	void desativarInsumoMarcaComoInativoSemApagar() throws Exception {
		logadoComPapel(PapelMembro.GERENTE);
		Insumo insumo = mock(Insumo.class); // resposta é 204, então um mock basta
		when(repInsumo.findByIdAndEspacoId(5L, 1L)).thenReturn(Optional.of(insumo));

		comoLogado(requisicao("DELETE", "/espacos/1/insumos/5", ""))
			.andExpect(status().isNoContent())
			.andExpect(content().string(""));

		verify(insumo).setAtivo(false);
		verify(repInsumo).save(insumo);
		// soft delete: o histórico de lotes e movimentações depende do insumo continuar existindo
		verify(repInsumo, never()).deleteById(any());
		verify(repInsumo, never()).delete(any());
	}

	@Test
	void desativarInsumoDeOutroEspacoRetorna404SemAlterar() throws Exception {
		logadoComPapel(PapelMembro.GERENTE);
		when(repInsumo.findByIdAndEspacoId(5L, 1L)).thenReturn(Optional.empty());

		comoLogado(requisicao("DELETE", "/espacos/1/insumos/5", ""))
			.andExpect(status().isNotFound());

		verify(repInsumo, never()).save(any());
	}

	// =================================================================
	// POST /espacos/{id}/insumos/{id}/lotes  (entrada de estoque)
	// =================================================================

	@Test
	void registrarEntradaCriaLoteSomaNoEstoqueEGravaMovimentacao() throws Exception {
		logadoComPapel(PapelMembro.GERENTE);
		Insumo insumo = insumoReal("Farinha", 5.0, 2.0);
		when(repInsumo.findByIdAndEspacoId(5L, 1L)).thenReturn(Optional.of(insumo));

		comoLogado(requisicao("POST", "/espacos/1/insumos/5/lotes", loteJson()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.quantidadeInicial").value(10.0))
			.andExpect(jsonPath("$.quantidadeAtual").value(10.0))
			.andExpect(jsonPath("$.dataValidade").value("2030-12-31"))
			.andExpect(jsonPath("$.fornecedor").value("Fornecedor X"));

		ArgumentCaptor<LoteInsumo> lote = ArgumentCaptor.forClass(LoteInsumo.class);
		verify(repLote).save(lote.capture());
		assertEquals(10.0, lote.getValue().getQuantidadeInicial(), 0.0001);
		assertEquals(10.0, lote.getValue().getQuantidadeAtual(), 0.0001);
		assertEquals(LocalDate.now(), lote.getValue().getDataEntrada());
		assertSame(insumo, lote.getValue().getInsumo());

		assertEquals(15.0, insumo.getEstoqueAtual(), 0.0001);
		verify(repInsumo).save(insumo);

		ArgumentCaptor<MovimentacaoEstoque> mov = ArgumentCaptor.forClass(MovimentacaoEstoque.class);
		verify(repMov).save(mov.capture());
		assertEquals(TipoMovimentacao.ENTRADA, mov.getValue().getTipo());
		assertEquals(10.0, mov.getValue().getQuantidade(), 0.0001);
		assertSame(insumo, mov.getValue().getInsumo());
		assertSame(lote.getValue(), mov.getValue().getLote());
	}

	@Test
	void registrarEntradaEmInsumoDeOutroEspacoRetorna404SemGravarNada() throws Exception {
		logadoComPapel(PapelMembro.GERENTE);
		when(repInsumo.findByIdAndEspacoId(5L, 1L)).thenReturn(Optional.empty());

		comoLogado(requisicao("POST", "/espacos/1/insumos/5/lotes", loteJson()))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.mensagem").value("Insumo não encontrado"));

		verifyNoInteractions(repLote, repMov);
		verify(repInsumo, never()).save(any());
	}

	// =================================================================
	// GET /espacos/{id}/insumos/{id}/lotes
	// =================================================================

	@Test
	void listarLotesRetornaApenasLotesComSaldo() throws Exception {
		logadoComPapel(PapelMembro.OPERADOR);
		Insumo insumo = insumoReal("Farinha", 10.0, 5.0);
		when(repInsumo.findByIdAndEspacoId(5L, 1L)).thenReturn(Optional.of(insumo));
		when(repLote.findByInsumoIdAndQuantidadeAtualGreaterThanOrderByDataValidadeAsc(5L, 0.0))
			.thenReturn(List.of(loteReal(insumo, 10.0, 4.0)));

		comoLogado(get("/espacos/1/insumos/5/lotes"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(1))
			.andExpect(jsonPath("$[0].quantidadeAtual").value(4.0));

		verify(repLote).findByInsumoIdAndQuantidadeAtualGreaterThanOrderByDataValidadeAsc(5L, 0.0);
	}

	@Test
	void listarLotesDeInsumoDeOutroEspacoRetorna404SemConsultarLotes() throws Exception {
		logadoComPapel(PapelMembro.OPERADOR);
		when(repInsumo.findByIdAndEspacoId(5L, 1L)).thenReturn(Optional.empty());

		comoLogado(get("/espacos/1/insumos/5/lotes"))
			.andExpect(status().isNotFound());

		verifyNoInteractions(repLote);
	}

	// =================================================================
	// GET /espacos/{id}/insumos/vencendo?dias=
	// =================================================================

	@Test
	void vencendoUsaJanelaPadraoDeSeteDias() throws Exception {
	    logadoComPapel(PapelMembro.OPERADOR);

	    comoLogado(get("/espacos/1/insumos/vencendo")).andExpect(status().isOk());

	    ArgumentCaptor<LocalDate> limite = ArgumentCaptor.forClass(LocalDate.class);
	    verify(repLote).buscarVencidosOuVencendo(eq(1L), limite.capture());
	    assertEquals(LocalDate.now().plusDays(7), limite.getValue());
	}

	@ParameterizedTest(name = "dias={0}")
	@CsvSource({ "-5", "0", "1", "30" })
	void vencendoRespeitaOParametroDias(int dias) throws Exception {
	    logadoComPapel(PapelMembro.OPERADOR);

	    comoLogado(get("/espacos/1/insumos/vencendo").param("dias", String.valueOf(dias)))
	        .andExpect(status().isOk());

	    ArgumentCaptor<LocalDate> limite = ArgumentCaptor.forClass(LocalDate.class);
	    verify(repLote).buscarVencidosOuVencendo(eq(1L), limite.capture());
	    assertEquals(LocalDate.now().plusDays(dias), limite.getValue());
	}

	@Test
	void vencendoRetornaOsLotesEncontrados() throws Exception {
		logadoComPapel(PapelMembro.OPERADOR);
		Insumo leite = insumoReal("Leite", 3.0, 1.0);
		when(repLote.buscarVencidosOuVencendo(anyLong(), any()))
	    .thenReturn(List.of(loteReal(leite, 3.0, 3.0)));

		comoLogado(get("/espacos/1/insumos/vencendo"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(1))
			.andExpect(jsonPath("$[0].quantidadeAtual").value(3.0));
	}

	@Test
	void vencendoComDiasNaoNumericoRetorna400() throws Exception {
		logadoComPapel(PapelMembro.OPERADOR);

		comoLogado(get("/espacos/1/insumos/vencendo").param("dias", "abc"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.status").value(400));

		verifyNoInteractions(repLote);
	}


	// =================================================================
	// PUT /espacos/{id}/insumos/lotes/{idLote}/perda  (mínimo: GERENTE)
	// =================================================================

	@Test
	void perdaParcialBaixaDoLoteEDoEstoqueEGravaMovimentacao() throws Exception {
		logadoComPapel(PapelMembro.GERENTE);
		Insumo insumo = insumoReal("Leite", 25.0, 5.0);
		LoteInsumo lote = loteReal(insumo, 10.0, 10.0);
		when(repLote.findByIdAndInsumoEspacoId(9L, 1L)).thenReturn(Optional.of(lote));

		comoLogado(requisicao("PUT", "/espacos/1/insumos/lotes/9/perda",
				json("quantidade", "4", "tipo", q("PERDA_VALIDADE"), "motivo", q("Venceu"))))
			.andExpect(status().isNoContent());

		assertEquals(6.0, lote.getQuantidadeAtual(), 0.0001);
		assertEquals(21.0, insumo.getEstoqueAtual(), 0.0001);
		verify(repLote).save(lote);
		verify(repInsumo).save(insumo);

		ArgumentCaptor<MovimentacaoEstoque> mov = ArgumentCaptor.forClass(MovimentacaoEstoque.class);
		verify(repMov).save(mov.capture());
		assertEquals(TipoMovimentacao.PERDA_VALIDADE, mov.getValue().getTipo());
		assertEquals(4.0, mov.getValue().getQuantidade(), 0.0001);
		assertEquals("Venceu", mov.getValue().getMotivo());
		assertSame(lote, mov.getValue().getLote());
		assertSame(insumo, mov.getValue().getInsumo());
	}

	@Test
	void perdaSemQuantidadeBaixaOLoteInteiro() throws Exception {
		logadoComPapel(PapelMembro.GERENTE);
		Insumo insumo = insumoReal("Leite", 25.0, 5.0);
		LoteInsumo lote = loteReal(insumo, 10.0, 10.0);
		when(repLote.findByIdAndInsumoEspacoId(9L, 1L)).thenReturn(Optional.of(lote));

		comoLogado(requisicao("PUT", "/espacos/1/insumos/lotes/9/perda",
				json("tipo", q("PERDA_OUTRO"), "motivo", q("Caiu no chão"))))
			.andExpect(status().isNoContent());

		assertEquals(0.0, lote.getQuantidadeAtual(), 0.0001);
		assertEquals(15.0, insumo.getEstoqueAtual(), 0.0001);   // 25 - 10

		ArgumentCaptor<MovimentacaoEstoque> mov = ArgumentCaptor.forClass(MovimentacaoEstoque.class);
		verify(repMov).save(mov.capture());
		assertEquals(10.0, mov.getValue().getQuantidade(), 0.0001);
	}

	@Test
	void perdaDeExatamenteOSaldoDoLoteEPermitida() throws Exception {
		logadoComPapel(PapelMembro.GERENTE);
		Insumo insumo = insumoReal("Leite", 25.0, 5.0);
		LoteInsumo lote = loteReal(insumo, 10.0, 10.0);
		when(repLote.findByIdAndInsumoEspacoId(9L, 1L)).thenReturn(Optional.of(lote));

		comoLogado(requisicao("PUT", "/espacos/1/insumos/lotes/9/perda", perdaJson("10")))
			.andExpect(status().isNoContent());

		assertEquals(0.0, lote.getQuantidadeAtual(), 0.0001);
	}

	@Test
	void perdaMaiorQueOSaldoRetornaErroDeRegraSemAlterarNada() throws Exception {
		logadoComPapel(PapelMembro.GERENTE);
		Insumo insumo = insumoReal("Leite", 25.0, 5.0);
		LoteInsumo lote = loteReal(insumo, 10.0, 10.0);
		when(repLote.findByIdAndInsumoEspacoId(9L, 1L)).thenReturn(Optional.of(lote));

		comoLogado(requisicao("PUT", "/espacos/1/insumos/lotes/9/perda", perdaJson("10.5")))
			.andExpect(status().is(STATUS_REGRA_NEGOCIO))
			.andExpect(jsonPath("$.mensagem", startsWith("Quantidade maior que a disponivel")));

		assertEquals(10.0, lote.getQuantidadeAtual(), 0.0001);
		assertEquals(25.0, insumo.getEstoqueAtual(), 0.0001);
		verify(repLote, never()).save(any());
		verify(repInsumo, never()).save(any());
		verifyNoInteractions(repMov);
	}

	@Test
	void perdaTotalDeLoteJaZeradoRetornaErroDeRegra() throws Exception {
		logadoComPapel(PapelMembro.GERENTE);
		Insumo insumo = insumoReal("Leite", 15.0, 5.0);
		LoteInsumo lote = loteReal(insumo, 10.0, 0.0);
		when(repLote.findByIdAndInsumoEspacoId(9L, 1L)).thenReturn(Optional.of(lote));

		comoLogado(requisicao("PUT", "/espacos/1/insumos/lotes/9/perda",
				json("tipo", q("PERDA_VALIDADE"), "motivo", q("Venceu"))))
			.andExpect(status().is(STATUS_REGRA_NEGOCIO))
			.andExpect(jsonPath("$.mensagem").value("A quantidade deve ser maior que zero"));

		verify(repLote, never()).save(any());
		verifyNoInteractions(repMov);
	}

	@Test
	void perdaEmLoteDeOutroEspacoRetorna404SemAlterarNada() throws Exception {
		logadoComPapel(PapelMembro.GERENTE);
		when(repLote.findByIdAndInsumoEspacoId(9L, 1L)).thenReturn(Optional.empty());

		comoLogado(requisicao("PUT", "/espacos/1/insumos/lotes/9/perda", perdaJson("1")))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.mensagem").value("Lote não encontrado"));

		verify(repLote).findByIdAndInsumoEspacoId(9L, 1L);
		verify(repLote, never()).save(any());
		verify(repInsumo, never()).save(any());
		verifyNoInteractions(repMov);
	}

	@Test
	void perdaNaoAceitaTipoEntrada() throws Exception {
		logadoComPapel(PapelMembro.GERENTE);
		Insumo insumo = insumoReal("Leite", 25.0, 5.0);
		LoteInsumo lote = loteReal(insumo, 10.0, 10.0);
		when(repLote.findByIdAndInsumoEspacoId(9L, 1L)).thenReturn(Optional.of(lote));

		comoLogado(requisicao("PUT", "/espacos/1/insumos/lotes/9/perda",
				json("quantidade", "4", "tipo", q("ENTRADA"), "motivo", q("x"))))
			.andExpect(status().is4xxClientError());

		assertEquals(10.0, lote.getQuantidadeAtual(), 0.0001);
		verifyNoInteractions(repMov);
	}

	// =================================================================
	// SEGURANÇA TRANSVERSAL: vale para todos os endpoints
	// =================================================================

	@ParameterizedTest(name = "sem autenticação: {0} -> 401")
	@MethodSource("todosOsEndpoints")
	void semAutenticacaoRetorna401(String nome, String metodo, String url, String corpo) throws Exception {
		mockMvc.perform(requisicao(metodo, url, corpo))
			.andExpect(status().isUnauthorized());

		verifyNoInteractions(repInsumo, repLote, repMov, repEspaco);
	}

	@ParameterizedTest(name = "quem não é membro: {0} -> 403")
	@MethodSource("todosOsEndpoints")
	void quemNaoEMembroRecebe403ENenhumDadoEConsultado(String nome, String metodo, String url, String corpo) throws Exception {
		logadoSemSerMembro();

		comoLogado(requisicao(metodo, url, corpo))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.status").value(403));

		verifyNoInteractions(repInsumo, repLote, repMov, repEspaco);
	}

	@ParameterizedTest(name = "OPERADOR não pode: {0} -> 403")
	@MethodSource("endpointsDeEscrita")
	void operadorNaoPodeEscrever(String nome, String metodo, String url, String corpo) throws Exception {
		logadoComPapel(PapelMembro.OPERADOR);

		comoLogado(requisicao(metodo, url, corpo))
			.andExpect(status().isForbidden());

		verifyNoInteractions(repInsumo, repLote, repMov, repEspaco);
	}

	@ParameterizedTest(name = "OPERADOR pode ler: {0}")
	@MethodSource("endpointsDeLeitura")
	void operadorPodeLer(String nome, String metodo, String url, String corpo) throws Exception {
		logadoComPapel(PapelMembro.OPERADOR);
		when(repInsumo.findByIdAndEspacoId(anyLong(), anyLong())).thenReturn(Optional.of(new Insumo()));

		comoLogado(requisicao(metodo, url, corpo))
			.andExpect(status().isOk());
	}

	static Stream<Arguments> endpointsDeLeitura() {
		return Stream.of(
			Arguments.of("listar insumos", "GET", "/espacos/1/insumos", ""),
			Arguments.of("baixo estoque", "GET", "/espacos/1/insumos/baixoEstoque", ""),
			Arguments.of("buscar insumo", "GET", "/espacos/1/insumos/5", ""),
			Arguments.of("listar lotes", "GET", "/espacos/1/insumos/5/lotes", ""),
			Arguments.of("vencendo", "GET", "/espacos/1/insumos/vencendo", "")
		);
	}

	static Stream<Arguments> endpointsDeEscrita() {
		return Stream.of(
			Arguments.of("criar insumo", "POST", "/espacos/1/insumos", insumoJson()),
			Arguments.of("editar insumo", "PUT", "/espacos/1/insumos/5", insumoJson()),
			Arguments.of("desativar insumo", "DELETE", "/espacos/1/insumos/5", ""),
			Arguments.of("registrar entrada", "POST", "/espacos/1/insumos/5/lotes", loteJson()),
			Arguments.of("registrar perda", "PUT", "/espacos/1/insumos/lotes/9/perda", perdaJson("1"))
		);
	}

	static Stream<Arguments> todosOsEndpoints() {
		return Stream.concat(endpointsDeLeitura(), endpointsDeEscrita());
	}

	// =================================================================
	// auxiliares
	// =================================================================

	private static MockHttpServletRequestBuilder requisicao(String metodo, String url, String corpo) {
		MockHttpServletRequestBuilder builder = request(HttpMethod.valueOf(metodo), url);
		if (!corpo.isEmpty()) {
			builder.contentType(MediaType.APPLICATION_JSON).content(corpo);
		}
		return builder;
	}

	private RequestPostProcessor autenticado() {
		return authentication(new UsernamePasswordAuthenticationToken(logado, null, List.of()));
	}

	private ResultActions comoLogado(MockHttpServletRequestBuilder requisicao) throws Exception {
		return mockMvc.perform(requisicao.with(autenticado()));
	}

	private void logadoComPapel(PapelMembro papel) {
		MembroEspaco membro = new MembroEspaco();
		membro.setPapel(papel);
		when(repMembro.findByEspacoIdAndUsuarioId(anyLong(), anyLong())).thenReturn(Optional.of(membro));
	}

	private void logadoSemSerMembro() {
		when(repMembro.findByEspacoIdAndUsuarioId(anyLong(), anyLong())).thenReturn(Optional.empty());
	}

	private static Insumo insumoReal(String nome, double estoqueAtual, double estoqueMinimo) {
		Insumo i = new Insumo();
		i.setNome(nome);
		i.setEstoqueAtual(estoqueAtual);
		i.setEstoqueMinimo(estoqueMinimo);
		i.setAtivo(true);
		return i;
	}

	private static LoteInsumo loteReal(Insumo insumo, double quantidadeInicial, double quantidadeAtual) {
		LoteInsumo l = new LoteInsumo();
		l.setInsumo(insumo);
		l.setQuantidadeInicial(quantidadeInicial);
		l.setQuantidadeAtual(quantidadeAtual);
		l.setDataEntrada(LocalDate.of(2026, 10, 1));
		l.setDataValidade(LocalDate.of(2026, 12, 31));
		return l;
	}

	private static String insumoJson() {
		return json("nome", q("Farinha"), "categoria", q(CATEGORIA), "unidadeMedida", q(UNIDADE),
			"estoqueMinimo", "5", "custoUnitario", "2.5");
	}

	private static String loteJson() {
		return json("quantidade", "10", "dataValidade", q("2030-12-31"), "fornecedor", q("Fornecedor X"));
	}

	private static String perdaJson(String quantidade) {
		return json("quantidade", quantidade, "tipo", q("PERDA_VALIDADE"), "motivo", q("Quebra"));
	}

	// monta {"chave":valor,...}. Os valores já vêm no formato JSON (use q() para strings)
	private static String json(String... paresChaveValor) {
		StringBuilder sb = new StringBuilder("{");
		for (int i = 0; i < paresChaveValor.length; i += 2) {
			if (i > 0) sb.append(",");
			sb.append("\"").append(paresChaveValor[i]).append("\":").append(paresChaveValor[i + 1]);
		}
		return sb.append("}").toString();
	}

	private static String q(String texto) {
		return "\"" + texto + "\"";
	}
}