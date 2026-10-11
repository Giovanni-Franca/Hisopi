package Hisopi.Hisopi.controller;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
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

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import Hisopi.Hisopi.Enum.PapelMembro;
import Hisopi.Hisopi.Enum.TipoReceita;
import Hisopi.Hisopi.model.Espaco;
import Hisopi.Hisopi.model.Insumo;
import Hisopi.Hisopi.model.MembroEspaco;
import Hisopi.Hisopi.model.Receita;
import Hisopi.Hisopi.model.ReceitaInsumo;
import Hisopi.Hisopi.model.Usuario;
import Hisopi.Hisopi.repository.EspacoRepository;
import Hisopi.Hisopi.repository.InsumoRepository;
import Hisopi.Hisopi.repository.MembroEspacoRepository;
import Hisopi.Hisopi.repository.ReceitaInsumoRepository;
import Hisopi.Hisopi.repository.ReceitaRepository;


@SpringBootTest
@AutoConfigureMockMvc
public class ReceitaControllerTest {

	private static final String TIPO_RECEITA = "SUGESTAO_CONSUMO";

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean private MembroEspacoRepository repMembro;
	@MockitoBean private ReceitaRepository repReceita;
	@MockitoBean private ReceitaInsumoRepository repReceitaInsumo;
	@MockitoBean private InsumoRepository repInsumo;
	@MockitoBean private EspacoRepository repEspaco;

	private Usuario logado;

	@BeforeEach
	void prepararUsuarioLogado() {
		logado = mock(Usuario.class);
		when(logado.getId()).thenReturn(1L);
	}

	// =================================================================
	// POST /espacos/{id}/receitas
	// =================================================================

	@Test
	void criarReceitaSalvaVinculadaAoEspacoDaUrl() throws Exception {
		logadoComPapel(PapelMembro.GERENTE);
		Espaco espaco = new Espaco();
		espaco.setNome("Cozinha");
		when(repEspaco.findById(1L)).thenReturn(Optional.of(espaco));

		comoLogado(requisicao("POST", "/espacos/1/receitas", receitaJson()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.nome").value("Bolo de cenoura"));

		ArgumentCaptor<Receita> salva = ArgumentCaptor.forClass(Receita.class);
		verify(repReceita).save(salva.capture());
		assertEquals("Bolo de cenoura", salva.getValue().getNome());
		assertEquals(TipoReceita.SUGESTAO_CONSUMO, salva.getValue().getTipo());
		assertEquals("Misturar e assar", salva.getValue().getModoPreparo());
		assertSame(espaco, salva.getValue().getEspaco());
	}

	@Test
	void criarReceitaEmEspacoInexistenteRetorna404() throws Exception {
		logadoComPapel(PapelMembro.GERENTE);
		when(repEspaco.findById(1L)).thenReturn(Optional.empty());

		comoLogado(requisicao("POST", "/espacos/1/receitas", receitaJson()))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.mensagem").value("Espaço não encontrado"));

		verify(repReceita, never()).save(any());
	}

	@ParameterizedTest(name = "{0} cria receita -> HTTP {1}")
	@CsvSource({ "OPERADOR, 403", "GERENTE, 200", "ADMIN, 200", "DONO, 200" })
	void papelMinimoParaCriarReceitaEGerente(PapelMembro papel, int esperado) throws Exception {
		logadoComPapel(papel);
		when(repEspaco.findById(1L)).thenReturn(Optional.of(new Espaco()));

		comoLogado(requisicao("POST", "/espacos/1/receitas", receitaJson()))
			.andExpect(status().is(esperado));
	}

	// =================================================================
	// GET /espacos/{id}/receitas
	// =================================================================

	@Test
	void listarReceitasRetornaAsDoEspacoDaUrl() throws Exception {
		logadoComPapel(PapelMembro.OPERADOR);
		when(repReceita.findByEspacoId(1L)).thenReturn(List.of(
			receitaReal("Bolo", null), receitaReal("Pão", null)));

		comoLogado(get("/espacos/1/receitas"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(2))
			.andExpect(jsonPath("$[0].nome").value("Bolo"))
			.andExpect(jsonPath("$[1].nome").value("Pão"));

		verify(repReceita).findByEspacoId(1L);
	}

	@Test
	void listarReceitasSemNenhumaRetornaListaVazia() throws Exception {
		logadoComPapel(PapelMembro.OPERADOR);
		when(repReceita.findByEspacoId(1L)).thenReturn(List.of());

		comoLogado(get("/espacos/1/receitas"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$").isEmpty());
	}

	@Test
	void listarReceitasNaoExpoeOEspaco() throws Exception {
		logadoComPapel(PapelMembro.OPERADOR);
		Espaco espaco = new Espaco();
		espaco.setNome("Cozinha");
		when(repReceita.findByEspacoId(1L)).thenReturn(List.of(receitaReal("Bolo", espaco)));

		comoLogado(get("/espacos/1/receitas"))
			.andExpect(status().isOk())
			.andExpect(content().string(not(containsString("\"espaco\""))));
	}

	// =================================================================
	// GET /espacos/{id}/receitas/sugestoes
	// =================================================================

	@Test
	void sugestoesFiltramPeloEspacoEPeloTipoSugestaoDeConsumo() throws Exception {
		logadoComPapel(PapelMembro.OPERADOR);
		Espaco espaco = new Espaco();
		when(repReceita.findByEspacoIdAndTipo(1L, TipoReceita.SUGESTAO_CONSUMO))
			.thenReturn(List.of(receitaReal("Vitamina", espaco)));

		comoLogado(get("/espacos/1/receitas/sugestoes"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(1))
			.andExpect(jsonPath("$[0].nome").value("Vitamina"))
			.andExpect(jsonPath("$[0].tipo").value("SUGESTAO_CONSUMO"))
			.andExpect(jsonPath("$[0].modoPreparo").value("Misturar e assar"))
			.andExpect(content().string(not(containsString("\"espaco\""))));

		verify(repReceita).findByEspacoIdAndTipo(1L, TipoReceita.SUGESTAO_CONSUMO);
	}

	@Test
	void sugestoesConsultamOEspacoDaUrlEMaisNenhum() throws Exception {
		logadoComPapel(PapelMembro.OPERADOR);
		when(repReceita.findByEspacoIdAndTipo(2L, TipoReceita.SUGESTAO_CONSUMO)).thenReturn(List.of());

		comoLogado(get("/espacos/2/receitas/sugestoes")).andExpect(status().isOk());

		verify(repReceita).findByEspacoIdAndTipo(2L, TipoReceita.SUGESTAO_CONSUMO);
		verify(repReceita, never()).findByEspacoIdAndTipo(1L, TipoReceita.SUGESTAO_CONSUMO);
	}

	// =================================================================
	// POST /espacos/{id}/receitas/{id}/insumos  (vincular, mínimo: GERENTE)
	// =================================================================

	@Test
	void vincularInsumoCriaOVinculoERetorna201() throws Exception {
		logadoComPapel(PapelMembro.GERENTE);
		Receita receita = mock(Receita.class);
		Insumo farinha = insumoMock(5L, "Farinha");
		when(repReceita.findByIdAndEspacoId(3L, 1L)).thenReturn(Optional.of(receita));
		when(repInsumo.findByIdAndEspacoId(5L, 1L)).thenReturn(Optional.of(farinha));
		when(repReceitaInsumo.existsByReceitaIdAndInsumoId(3L, 5L)).thenReturn(false);

		comoLogado(requisicao("POST", "/espacos/1/receitas/3/insumos", vinculoJson(5, "0.5")))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.idInsumo").value(5))
			.andExpect(jsonPath("$.nomeInsumo").value("Farinha"))
			.andExpect(jsonPath("$.quantidadePorUnidade").value(0.5));

		ArgumentCaptor<ReceitaInsumo> salvo = ArgumentCaptor.forClass(ReceitaInsumo.class);
		verify(repReceitaInsumo).save(salvo.capture());
		assertSame(receita, salvo.getValue().getReceita());
		assertSame(farinha, salvo.getValue().getInsumo());
		assertEquals(0.5, salvo.getValue().getQuantidadePorUnidade(), 0.0001);
	}

	@Test
	void vincularEmReceitaDeOutroEspacoRetorna404SemConsultarInsumo() throws Exception {
		logadoComPapel(PapelMembro.GERENTE);
		when(repReceita.findByIdAndEspacoId(3L, 1L)).thenReturn(Optional.empty());

		comoLogado(requisicao("POST", "/espacos/1/receitas/3/insumos", vinculoJson(5, "0.5")))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.mensagem").value("Receita não encontrada"));

		verifyNoInteractions(repInsumo);
		verify(repReceitaInsumo, never()).save(any());
	}

	@Test
	void vincularInsumoDeOutroEspacoRetorna404() throws Exception {
		logadoComPapel(PapelMembro.GERENTE);
		Receita receita = mock(Receita.class);
		when(repReceita.findByIdAndEspacoId(3L, 1L)).thenReturn(Optional.of(receita));
		when(repInsumo.findByIdAndEspacoId(5L, 1L)).thenReturn(Optional.empty());

		comoLogado(requisicao("POST", "/espacos/1/receitas/3/insumos", vinculoJson(5, "0.5")))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.mensagem").value("Insumo não encontrado"));

		verify(repInsumo).findByIdAndEspacoId(5L, 1L); // a busca é filtrada pelo espaço da URL
		verify(repReceitaInsumo, never()).save(any());
	}

	@Test
	void vincularInsumoJaPresenteNaFichaRetorna409() throws Exception {
		logadoComPapel(PapelMembro.GERENTE);
		Receita receita = mock(Receita.class);
		Insumo farinha = insumoMock(5L, "Farinha");
		when(repReceita.findByIdAndEspacoId(3L, 1L)).thenReturn(Optional.of(receita));
		when(repInsumo.findByIdAndEspacoId(5L, 1L)).thenReturn(Optional.of(farinha));
		when(repReceitaInsumo.existsByReceitaIdAndInsumoId(3L, 5L)).thenReturn(true);

		comoLogado(requisicao("POST", "/espacos/1/receitas/3/insumos", vinculoJson(5, "0.5")))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.status").value(409))
			.andExpect(jsonPath("$.mensagem").value("Este insumo já está na ficha técnica da receita"));

		verify(repReceitaInsumo, never()).save(any());
	}

	@Test
	void corridaNaConstraintUnicaDoBancoRetorna409() throws Exception {
		logadoComPapel(PapelMembro.GERENTE);
		Receita receita = mock(Receita.class);
		Insumo farinha = insumoMock(5L, "Farinha");
		when(repReceita.findByIdAndEspacoId(3L, 1L)).thenReturn(Optional.of(receita));
		when(repInsumo.findByIdAndEspacoId(5L, 1L)).thenReturn(Optional.of(farinha));
		when(repReceitaInsumo.existsByReceitaIdAndInsumoId(3L, 5L)).thenReturn(false);
		when(repReceitaInsumo.save(any(ReceitaInsumo.class)))
			.thenThrow(new DataIntegrityViolationException("uk_receita_insumo"));

		comoLogado(requisicao("POST", "/espacos/1/receitas/3/insumos", vinculoJson(5, "0.5")))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.status").value(409));
	}

	// =================================================================
	// GET /espacos/{id}/receitas/{id}/insumos  (ficha técnica)
	// =================================================================

	@Test
	void listarFichaTecnicaRetornaOsInsumosDaReceita() throws Exception {
		logadoComPapel(PapelMembro.OPERADOR);
		when(repReceita.findByIdAndEspacoId(3L, 1L)).thenReturn(Optional.of(mock(Receita.class)));
		Insumo farinha = insumoMock(5L, "Farinha");
		Insumo ovo = insumoMock(6L, "Ovo");
		when(repReceitaInsumo.findByReceitaId(3L)).thenReturn(List.of(
			vinculoReal(farinha, 0.5), vinculoReal(ovo, 2.0)));

		comoLogado(get("/espacos/1/receitas/3/insumos"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(2))
			.andExpect(jsonPath("$[0].idInsumo").value(5))
			.andExpect(jsonPath("$[0].nomeInsumo").value("Farinha"))
			.andExpect(jsonPath("$[0].quantidadePorUnidade").value(0.5))
			.andExpect(jsonPath("$[1].nomeInsumo").value("Ovo"))
			.andExpect(jsonPath("$[1].quantidadePorUnidade").value(2.0));
	}

	@Test
	void listarFichaTecnicaDeReceitaSemInsumosRetornaListaVazia() throws Exception {
		logadoComPapel(PapelMembro.OPERADOR);
		when(repReceita.findByIdAndEspacoId(3L, 1L)).thenReturn(Optional.of(mock(Receita.class)));
		when(repReceitaInsumo.findByReceitaId(3L)).thenReturn(List.of());

		comoLogado(get("/espacos/1/receitas/3/insumos"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$").isEmpty());
	}

	@Test
	void listarFichaTecnicaDeReceitaDeOutroEspacoRetorna404SemConsultarVinculos() throws Exception {
		logadoComPapel(PapelMembro.OPERADOR);
		when(repReceita.findByIdAndEspacoId(3L, 1L)).thenReturn(Optional.empty());

		comoLogado(get("/espacos/1/receitas/3/insumos"))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.mensagem").value("Receita não encontrada"));

		verifyNoInteractions(repReceitaInsumo);
	}

	@Test
	void idDeReceitaNaoNumericoRetorna400() throws Exception {
		logadoComPapel(PapelMembro.OPERADOR);

		comoLogado(get("/espacos/1/receitas/abc/insumos"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.status").value(400));
	}

	// =================================================================
	// DELETE /espacos/{id}/receitas/{id}/insumos/{idVinculo}  (mínimo: GERENTE)
	// =================================================================

	@Test
	void removerVinculoApagaERetorna204() throws Exception {
		logadoComPapel(PapelMembro.GERENTE);
		ReceitaInsumo vinculo = vinculoReal(insumoMock(5L, "Farinha"), 0.5);
		when(repReceita.findByIdAndEspacoId(3L, 1L)).thenReturn(Optional.of(mock(Receita.class)));
		when(repReceitaInsumo.findByIdAndReceitaId(10L, 3L)).thenReturn(Optional.of(vinculo));

		comoLogado(requisicao("DELETE", "/espacos/1/receitas/3/insumos/10", ""))
			.andExpect(status().isNoContent())
			.andExpect(content().string(""));

		verify(repReceitaInsumo).delete(vinculo);
	}

	@Test
	void removerVinculoDeReceitaDeOutroEspacoRetorna404SemApagar() throws Exception {
		logadoComPapel(PapelMembro.GERENTE);
		when(repReceita.findByIdAndEspacoId(3L, 1L)).thenReturn(Optional.empty());

		comoLogado(requisicao("DELETE", "/espacos/1/receitas/3/insumos/10", ""))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.mensagem").value("Receita não encontrada"));

		verifyNoInteractions(repReceitaInsumo);
	}

	@Test
	void removerVinculoDeOutraReceitaRetorna404SemApagar() throws Exception {
		logadoComPapel(PapelMembro.GERENTE);
		when(repReceita.findByIdAndEspacoId(3L, 1L)).thenReturn(Optional.of(mock(Receita.class)));
		when(repReceitaInsumo.findByIdAndReceitaId(10L, 3L)).thenReturn(Optional.empty());

		comoLogado(requisicao("DELETE", "/espacos/1/receitas/3/insumos/10", ""))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.mensagem").value("Vínculo não encontrado nesta receita"));

		verify(repReceitaInsumo, never()).delete(any());
	}

	@Test
	void idDeVinculoNaoNumericoRetorna400() throws Exception {
		logadoComPapel(PapelMembro.GERENTE);

		comoLogado(requisicao("DELETE", "/espacos/1/receitas/3/insumos/abc", ""))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.status").value(400));

		verify(repReceitaInsumo, never()).delete(any());
	}

	// =================================================================
	// SEGURANÇA TRANSVERSAL: vale para todos os endpoints
	// =================================================================

	@ParameterizedTest(name = "sem autenticação: {0} -> 401")
	@MethodSource("todosOsEndpoints")
	void semAutenticacaoRetorna401(String nome, String metodo, String url, String corpo) throws Exception {
		// se o seu AuthenticationEntryPoint não estiver configurado, o Spring devolve 403
		mockMvc.perform(requisicao(metodo, url, corpo))
			.andExpect(status().isUnauthorized());

		verifyNoInteractions(repReceita, repReceitaInsumo, repInsumo, repEspaco);
	}

	@ParameterizedTest(name = "quem não é membro: {0} -> 403")
	@MethodSource("todosOsEndpoints")
	void quemNaoEMembroRecebe403ENenhumDadoEConsultado(String nome, String metodo, String url, String corpo) throws Exception {
		logadoSemSerMembro();

		comoLogado(requisicao(metodo, url, corpo))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.status").value(403));

		// o interceptor barrou: nenhum repository do controller foi tocado
		verifyNoInteractions(repReceita, repReceitaInsumo, repInsumo, repEspaco);
	}

	@ParameterizedTest(name = "OPERADOR não pode: {0} -> 403")
	@MethodSource("endpointsDaFichaTecnicaQueAlteram")
	void operadorNaoPodeAlterarAFichaTecnica(String nome, String metodo, String url, String corpo) throws Exception {
		logadoComPapel(PapelMembro.OPERADOR);

		comoLogado(requisicao(metodo, url, corpo))
			.andExpect(status().isForbidden());

		verifyNoInteractions(repReceita, repReceitaInsumo, repInsumo, repEspaco);
	}

	@ParameterizedTest(name = "{0} pode ver as receitas")
	@EnumSource(PapelMembro.class)
	void qualquerPapelPodeLer(PapelMembro papel) throws Exception {
		logadoComPapel(papel);
		when(repReceita.findByIdAndEspacoId(anyLong(), anyLong())).thenReturn(Optional.of(mock(Receita.class)));

		comoLogado(get("/espacos/1/receitas")).andExpect(status().isOk());
		comoLogado(get("/espacos/1/receitas/sugestoes")).andExpect(status().isOk());
		comoLogado(get("/espacos/1/receitas/3/insumos")).andExpect(status().isOk());
	}

	static Stream<Arguments> endpointsDaFichaTecnicaQueAlteram() {
		return Stream.of(
			Arguments.of("vincular insumo", "POST", "/espacos/1/receitas/3/insumos", vinculoJson(5, "0.5")),
			Arguments.of("remover vínculo", "DELETE", "/espacos/1/receitas/3/insumos/10", "")
		);
	}

	static Stream<Arguments> todosOsEndpoints() {
		return Stream.of(
			Arguments.of("criar receita", "POST", "/espacos/1/receitas", receitaJson()),
			Arguments.of("listar receitas", "GET", "/espacos/1/receitas", ""),
			Arguments.of("sugestões", "GET", "/espacos/1/receitas/sugestoes", ""),
			Arguments.of("vincular insumo", "POST", "/espacos/1/receitas/3/insumos", vinculoJson(5, "0.5")),
			Arguments.of("ficha técnica", "GET", "/espacos/1/receitas/3/insumos", ""),
			Arguments.of("remover vínculo", "DELETE", "/espacos/1/receitas/3/insumos/10", "")
		);
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

	/** O interceptor @AcessoEspaco vai enxergar o usuário logado com este papel. */
	private void logadoComPapel(PapelMembro papel) {
		MembroEspaco membro = new MembroEspaco();
		membro.setPapel(papel);
		when(repMembro.findByEspacoIdAndUsuarioId(anyLong(), anyLong())).thenReturn(Optional.of(membro));
	}

	private void logadoSemSerMembro() {
		when(repMembro.findByEspacoIdAndUsuarioId(anyLong(), anyLong())).thenReturn(Optional.empty());
	}

	// mock criado fora de qualquer when(...).thenReturn(...), porque o Mockito não aceita stubbing aninhado
	private static Insumo insumoMock(long id, String nome) {
		Insumo i = mock(Insumo.class);
		when(i.getId()).thenReturn(id);
		when(i.getNome()).thenReturn(nome);
		return i;
	}

	private static Receita receitaReal(String nome, Espaco espaco) {
		Receita r = new Receita();
		r.setNome(nome);
		r.setTipo(TipoReceita.SUGESTAO_CONSUMO);
		r.setModoPreparo("Misturar e assar");
		r.setEspaco(espaco);
		return r;
	}

	private static ReceitaInsumo vinculoReal(Insumo insumo, double quantidadePorUnidade) {
		ReceitaInsumo v = new ReceitaInsumo();
		v.setInsumo(insumo);
		v.setQuantidadePorUnidade(quantidadePorUnidade);
		return v;
	}

	private static String receitaJson() {
		return json("nome", q("Bolo de cenoura"), "tipo", q(TIPO_RECEITA), "modoPreparo", q("Misturar e assar"));
	}

	private static String vinculoJson(long idInsumo, String quantidadePorUnidade) {
		return json("idInsumo", String.valueOf(idInsumo), "quantidadePorUnidade", quantidadePorUnidade);
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