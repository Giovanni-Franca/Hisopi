package Hisopi.Hisopi.controller;

import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import Hisopi.Hisopi.Enum.PapelMembro;
import Hisopi.Hisopi.Enum.TipoMovimentacao;
import Hisopi.Hisopi.model.Insumo;
import Hisopi.Hisopi.model.MembroEspaco;
import Hisopi.Hisopi.model.MovimentacaoEstoque;
import Hisopi.Hisopi.model.Usuario;
import Hisopi.Hisopi.repository.InsumoRepository;
import Hisopi.Hisopi.repository.MembroEspacoRepository;
import Hisopi.Hisopi.repository.MovimentacaoEstoqueRepository;

@SpringBootTest
@AutoConfigureMockMvc
public class MovimentacaoControllerTest {

	private static final int STATUS_REGRA_NEGOCIO = 400;

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean private MembroEspacoRepository repMembro;
	@MockitoBean private MovimentacaoEstoqueRepository repMov;
	@MockitoBean private InsumoRepository repInsumo;

	private Usuario logado;

	@BeforeEach
	void prepararUsuarioLogado() {
		logado = mock(Usuario.class);
		when(logado.getId()).thenReturn(1L);
	}

	// =================================================================
	// GET /espacos/{idEspaco}/insumos/{idInsumo}/movimentacoes
	// =================================================================

	@Test
	void listarMovimentacoesRetornaDadosPlanosNaOrdemDoRepositorio() throws Exception {
		logadoComPapel(PapelMembro.OPERADOR);

		Insumo farinha = insumo(5L, "Farinha");
		when(repInsumo.findByIdAndEspacoId(5L, 1L)).thenReturn(Optional.of(farinha));

		MovimentacaoEstoque perda = movimentacao(TipoMovimentacao.PERDA_VALIDADE, 2.5, farinha,
			LocalDateTime.of(2026, 10, 2, 9, 0), "Vencido");
		MovimentacaoEstoque entrada = movimentacao(TipoMovimentacao.ENTRADA, 10.0, farinha,
			LocalDateTime.of(2026, 10, 1, 10, 30), null);
		when(repMov.findByInsumoIdOrderByDataMovimentacaoDesc(5L)).thenReturn(List.of(perda, entrada));

		comoLogado(get("/espacos/1/insumos/5/movimentacoes"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(2))
			.andExpect(jsonPath("$[0].tipo").value("PERDA_VALIDADE"))
			.andExpect(jsonPath("$[0].quantidade").value(2.5))
			.andExpect(jsonPath("$[0].motivo").value("Vencido"))
			.andExpect(jsonPath("$[0].idInsumo").value(5))
			.andExpect(jsonPath("$[0].nomeInsumo").value("Farinha"))
			.andExpect(jsonPath("$[0].dataMovimentacao", startsWith("2026-10-02T09:00")))
			.andExpect(jsonPath("$[1].tipo").value("ENTRADA"))
			.andExpect(jsonPath("$[1].quantidade").value(10.0))
			.andExpect(jsonPath("$[1].dataMovimentacao", startsWith("2026-10-01T10:30")));
	}

	@Test
	void listarMovimentacoesDeInsumoSemHistoricoRetornaListaVazia() throws Exception {
		logadoComPapel(PapelMembro.OPERADOR);
		Insumo farinha = insumo(5L, "Farinha");
		when(repInsumo.findByIdAndEspacoId(5L, 1L)).thenReturn(Optional.of(farinha));
		when(repMov.findByInsumoIdOrderByDataMovimentacaoDesc(5L)).thenReturn(List.of());

		comoLogado(get("/espacos/1/insumos/5/movimentacoes"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$").isEmpty());
	}

	@Test
	void insumoDeOutroEspacoOuInexistenteRetorna404SemConsultarMovimentacoes() throws Exception {
		logadoComPapel(PapelMembro.DONO);
		when(repInsumo.findByIdAndEspacoId(5L, 1L)).thenReturn(Optional.empty());

		comoLogado(get("/espacos/1/insumos/5/movimentacoes"))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.status").value(404))
			.andExpect(jsonPath("$.mensagem").value("Insumo não encontrado"));

		verify(repInsumo).findByIdAndEspacoId(5L, 1L);
		verify(repMov, never()).findByInsumoIdOrderByDataMovimentacaoDesc(anyLong());
	}

	@ParameterizedTest(name = "{0} pode ler o histórico")
	@EnumSource(PapelMembro.class)
	void qualquerPapelPodeListarMovimentacoes(PapelMembro papel) throws Exception {
		logadoComPapel(papel);
		Insumo farinha = insumo(5L, "Farinha");
		when(repInsumo.findByIdAndEspacoId(5L, 1L)).thenReturn(Optional.of(farinha));

		comoLogado(get("/espacos/1/insumos/5/movimentacoes"))
			.andExpect(status().isOk());
	}

	@Test
	void listarMovimentacoesSemSerMembroRetorna403() throws Exception {
		logadoSemSerMembro();

		comoLogado(get("/espacos/1/insumos/5/movimentacoes"))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.status").value(403));

		verify(repInsumo, never()).findByIdAndEspacoId(anyLong(), anyLong());
		verify(repMov, never()).findByInsumoIdOrderByDataMovimentacaoDesc(anyLong());
	}

	@Test
	void idInsumoNaoNumericoRetorna400() throws Exception {
		logadoComPapel(PapelMembro.OPERADOR);

		comoLogado(get("/espacos/1/insumos/abc/movimentacoes"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.status").value(400));
	}

	@Test
	void listarMovimentacoesSemAutenticacaoRetorna401() throws Exception {
		mockMvc.perform(get("/espacos/1/insumos/5/movimentacoes"))
			.andExpect(status().isUnauthorized());
	}

	// =================================================================
	// GET /espacos/{idEspaco}/relatorios/perdas
	// =================================================================

	@Test
	void relatorioSeparaPerdasPorTipoESomaOTotal() throws Exception {
		logadoComPapel(PapelMembro.GERENTE);

		Insumo leite = insumo(8L, "Leite");
		MovimentacaoEstoque validade1 = movimentacao(TipoMovimentacao.PERDA_VALIDADE, 2.0, leite,
			LocalDateTime.of(2026, 10, 3, 8, 0), "Venceu");
		MovimentacaoEstoque outro = movimentacao(TipoMovimentacao.PERDA_OUTRO, 1.5, leite,
			LocalDateTime.of(2026, 10, 4, 8, 0), "Caiu no chão");
		MovimentacaoEstoque validade2 = movimentacao(TipoMovimentacao.PERDA_VALIDADE, 3.0, leite,
			LocalDateTime.of(2026, 10, 5, 8, 0), "Venceu");
		when(repMov.buscarPorTipos(anyLong(), any(), any(), any()))
			.thenReturn(List.of(validade1, outro, validade2));

		comoLogado(relatorio("2026-10-01", "2026-10-07"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.perdasPorValidade.length()").value(2))
			.andExpect(jsonPath("$.perdasPorValidade[0].tipo").value("PERDA_VALIDADE"))
			.andExpect(jsonPath("$.perdasPorValidade[0].nomeInsumo").value("Leite"))
			.andExpect(jsonPath("$.perdasPorOutroMotivo.length()").value(1))
			.andExpect(jsonPath("$.perdasPorOutroMotivo[0].tipo").value("PERDA_OUTRO"))
			.andExpect(jsonPath("$.perdasPorOutroMotivo[0].motivo").value("Caiu no chão"))
			.andExpect(jsonPath("$.quantidadeTotalPerdida").value(6.5));
	}

	@Test
	void relatorioSemPerdasRetornaListasVaziasETotalZero() throws Exception {
		logadoComPapel(PapelMembro.GERENTE);
		when(repMov.buscarPorTipos(anyLong(), any(), any(), any())).thenReturn(List.of());

		comoLogado(relatorio("2026-10-01", "2026-10-07"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.perdasPorValidade").isEmpty())
			.andExpect(jsonPath("$.perdasPorOutroMotivo").isEmpty())
			.andExpect(jsonPath("$.quantidadeTotalPerdida").value(0.0));
	}

	@Test
	void relatorioUsaIntervaloSemiabertoQueCobreOUltimoDia() throws Exception {
		logadoComPapel(PapelMembro.GERENTE);
		when(repMov.buscarPorTipos(anyLong(), any(), any(), any())).thenReturn(List.of());

		comoLogado(relatorio("2026-10-01", "2026-10-07")).andExpect(status().isOk());

		@SuppressWarnings({ "unchecked", "rawtypes" })
		ArgumentCaptor<Collection<TipoMovimentacao>> tipos = (ArgumentCaptor) ArgumentCaptor.forClass(Collection.class);
		ArgumentCaptor<LocalDateTime> de = ArgumentCaptor.forClass(LocalDateTime.class);
		ArgumentCaptor<LocalDateTime> ate = ArgumentCaptor.forClass(LocalDateTime.class);
		verify(repMov).buscarPorTipos(eq(1L), tipos.capture(), de.capture(), ate.capture());

		assertEquals(LocalDateTime.of(2026, 10, 1, 0, 0), de.getValue());
		assertEquals(LocalDateTime.of(2026, 10, 8, 0, 0), ate.getValue());
		assertEquals(Set.of(TipoMovimentacao.PERDA_VALIDADE, TipoMovimentacao.PERDA_OUTRO),
			new HashSet<>(tipos.getValue()));
	}

	@Test
	void relatorioDeUmSoDiaCobreODiaInteiro() throws Exception {
		logadoComPapel(PapelMembro.GERENTE);
		when(repMov.buscarPorTipos(anyLong(), any(), any(), any())).thenReturn(List.of());

		// início == fim é válido e não pode resultar em intervalo vazio
		comoLogado(relatorio("2026-10-05", "2026-10-05")).andExpect(status().isOk());

		ArgumentCaptor<LocalDateTime> de = ArgumentCaptor.forClass(LocalDateTime.class);
		ArgumentCaptor<LocalDateTime> ate = ArgumentCaptor.forClass(LocalDateTime.class);
		verify(repMov).buscarPorTipos(eq(1L), any(), de.capture(), ate.capture());

		assertEquals(LocalDateTime.of(2026, 10, 5, 0, 0), de.getValue());
		assertEquals(LocalDateTime.of(2026, 10, 6, 0, 0), ate.getValue());
	}

	/** O relatório precisa filtrar pelo espaço da URL, senão misturaria perdas de outros espaços. */
	@Test
	void relatorioConsultaPeloEspacoDaUrl() throws Exception {
		logadoComPapel(PapelMembro.GERENTE);
		when(repMov.buscarPorTipos(anyLong(), any(), any(), any())).thenReturn(List.of());

		comoLogado(get("/espacos/2/relatorios/perdas")
				.param("inicio", "2026-10-01").param("fim", "2026-10-07"))
			.andExpect(status().isOk());

		verify(repMov).buscarPorTipos(eq(2L), any(), any(), any());
	}

	@Test
	void relatorioComInicioDepoisDoFimRetornaErroDeRegra() throws Exception {
		logadoComPapel(PapelMembro.GERENTE);

		comoLogado(relatorio("2026-10-10", "2026-10-01"))
			.andExpect(status().is(STATUS_REGRA_NEGOCIO))
			.andExpect(jsonPath("$.mensagem").value("A data inicial não pode ser maior que a final"));

		verify(repMov, never()).buscarPorTipos(anyLong(), any(), any(), any());
	}

	@ParameterizedTest(name = "inicio={0} fim={1} -> 400")
	@CsvSource({
		"abc,        2026-10-07",
		"2026-10-01, xyz",
		"01/10/2026, 07/10/2026",
		"2026-13-45, 2026-10-07"
	})
	void relatorioComDataInvalidaRetorna400(String inicio, String fim) throws Exception {
		logadoComPapel(PapelMembro.GERENTE);

		comoLogado(relatorio(inicio, fim))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.status").value(400))
			.andExpect(jsonPath("$.mensagem").exists());

		verify(repMov, never()).buscarPorTipos(anyLong(), any(), any(), any());
	}

	@ParameterizedTest(name = "query \"{0}\" -> 400")
	@ValueSource(strings = { "?fim=2026-10-07", "?inicio=2026-10-01", "" })
	void relatorioSemParametroObrigatorioRetorna400(String query) throws Exception {
		logadoComPapel(PapelMembro.GERENTE);

		comoLogado(get("/espacos/1/relatorios/perdas" + query))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.status").value(400))
			.andExpect(jsonPath("$.mensagem").exists());

		verify(repMov, never()).buscarPorTipos(anyLong(), any(), any(), any());
	}

	@ParameterizedTest(name = "{0} pode ver o relatório")
	@EnumSource(PapelMembro.class)
	void qualquerPapelPodeVerORelatorio(PapelMembro papel) throws Exception {
		logadoComPapel(papel);
		when(repMov.buscarPorTipos(anyLong(), any(), any(), any())).thenReturn(List.of());

		comoLogado(relatorio("2026-10-01", "2026-10-07"))
			.andExpect(status().isOk());
	}

	@Test
	void relatorioSemSerMembroRetorna403() throws Exception {
		logadoSemSerMembro();

		comoLogado(relatorio("2026-10-01", "2026-10-07"))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.status").value(403));

		verify(repMov, never()).buscarPorTipos(anyLong(), any(), any(), any());
	}

	@Test
	void relatorioSemAutenticacaoRetorna401() throws Exception {
		mockMvc.perform(relatorio("2026-10-01", "2026-10-07"))
			.andExpect(status().isUnauthorized());
	}

	// =================================================================
	// auxiliares
	// =================================================================

	private static MockHttpServletRequestBuilder relatorio(String inicio, String fim) {
		return get("/espacos/1/relatorios/perdas").param("inicio", inicio).param("fim", fim);
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

	private static Insumo insumo(long id, String nome) {
		Insumo i = mock(Insumo.class);
		when(i.getId()).thenReturn(id);
		when(i.getNome()).thenReturn(nome);
		return i;
	}

	private static MovimentacaoEstoque movimentacao(TipoMovimentacao tipo, double quantidade,
			Insumo insumo, LocalDateTime data, String motivo) {
		MovimentacaoEstoque m = new MovimentacaoEstoque();
		m.setInsumo(insumo);
		m.setTipo(tipo);
		m.setQuantidade(quantidade);
		m.setMotivo(motivo);
		m.setDataMovimentacao(data);
		return m;
	}
}