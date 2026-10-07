package Hisopi.Hisopi.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import Hisopi.Hisopi.Enum.PapelMembro;
import Hisopi.Hisopi.model.Espaco;
import Hisopi.Hisopi.model.MembroEspaco;
import Hisopi.Hisopi.model.Usuario;
import Hisopi.Hisopi.repository.EspacoRepository;
import Hisopi.Hisopi.repository.MembroEspacoRepository;
import Hisopi.Hisopi.repository.UsuarioRepository;

/**
 * Testes do EspacoController.
 *
 * COMO FUNCIONA
 *  - Os repositories são mocks (sem banco). O interceptor @AcessoEspaco consulta
 *    repM.findByEspacoIdAndUsuarioId para descobrir o papel do usuário logado, então
 *    cada teste "escolhe" o papel com logadoComPapel(...) ou logadoSemSerMembro().
 *  - O usuário autenticado é um mock de Usuario com id 1.
 *  - Os testes de hierarquia são parametrizados: uma linha por combinação
 *    (papel de quem executa, papel do alvo, status esperado).
 *
 * TESTES QUE PODEM FALHAR HOJE (e por quê)
 *  - listarMeusEspacosNaoExpoeSenha: falha se o endpoint ainda devolve a entidade
 *    MembroEspaco (com Usuario aninhado) em vez de um DTO.
 *  - idEspacoNaoNumerico...: falha se o interceptor ainda não trata o NumberFormatException.
 */
@SpringBootTest
@AutoConfigureMockMvc
public class EspacoControllerTest {

	private static final String TIPO_ESPACO = "ORGANIZACAO"; // valor real do seu enum de tipo

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean private EspacoRepository repE;
	@MockitoBean private MembroEspacoRepository repM;
	@MockitoBean private UsuarioRepository repU;

	private Usuario logado;     
	private Usuario convidado; 

	@BeforeEach
	void prepararUsuarios() {
		logado = usuario(1L, "Logado", "logado@gmail.com");
		convidado = usuario(2L, "Novo", "novo@gmail.com");
	}

	// =================================================================
	// POST /espacos
	// =================================================================

	@Test
	void criarEspacoSalvaEspacoEVinculaCriadorComoDono() throws Exception {
		comoLogado(postJson("/espacos", json("nome", q("Cozinha"), "tipo", q(TIPO_ESPACO))))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.nome").value("Cozinha"));

		// o espaço foi salvo com os dados enviados
		ArgumentCaptor<Espaco> espacoSalvo = ArgumentCaptor.forClass(Espaco.class);
		verify(repE).save(espacoSalvo.capture());
		assertEquals("Cozinha", espacoSalvo.getValue().getNome());
		assertEquals(TIPO_ESPACO, espacoSalvo.getValue().getTipo().name());

		// e quem criou virou DONO desse mesmo espaço
		ArgumentCaptor<MembroEspaco> membroSalvo = ArgumentCaptor.forClass(MembroEspaco.class);
		verify(repM).save(membroSalvo.capture());
		assertEquals(PapelMembro.DONO, membroSalvo.getValue().getPapel());
		assertSame(logado, membroSalvo.getValue().getUsuario());
		assertSame(espacoSalvo.getValue(), membroSalvo.getValue().getEspaco());
	}

	@Test
	void criarEspacoSemAutenticacaoRetorna401() throws Exception {
		// se o seu AuthenticationEntryPoint não estiver configurado, o Spring devolve 403
		mockMvc.perform(postJson("/espacos", json("nome", q("Cozinha"), "tipo", q(TIPO_ESPACO))))
			.andExpect(status().isUnauthorized());

		verify(repE, never()).save(any());
	}

	// =================================================================
	// GET /espacos/{idEspaco}
	// =================================================================

	@Test
	void buscarEspacoComoMembroRetornaOEspaco() throws Exception {
		logadoComPapel(PapelMembro.OPERADOR); // o menor papel já pode ler

		Espaco espaco = new Espaco();
		espaco.setNome("Cozinha");
		when(repE.findById(1L)).thenReturn(Optional.of(espaco));

		comoLogado(get("/espacos/1"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.nome").value("Cozinha"));
	}

	@Test
	void buscarEspacoInexistenteRetorna404Padronizado() throws Exception {
		logadoComPapel(PapelMembro.DONO);
		when(repE.findById(1L)).thenReturn(Optional.empty());

		comoLogado(get("/espacos/1"))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.status").value(404))
			.andExpect(jsonPath("$.mensagem").value("Espaço não encontrado"));
	}

	@Test
	void buscarEspacoSemSerMembroRetorna403ENaoConsultaOEspaco() throws Exception {
		logadoSemSerMembro();

		comoLogado(get("/espacos/1"))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.status").value(403));

		verify(repE, never()).findById(anyLong());
	}

	@Test
	void idEspacoNaoNumericoRetorna400() throws Exception {
		comoLogado(get("/espacos/abc"))
			.andExpect(status().isBadRequest());
	}

	// =================================================================
	// GET /espacos/minhas
	// =================================================================

	@Test
	void listarMeusEspacosConsultaPeloUsuarioLogado() throws Exception {
		Espaco espaco = new Espaco();
		espaco.setNome("Cozinha");

		MembroEspaco vinculo = membro(PapelMembro.DONO);
		vinculo.setEspaco(espaco);
		vinculo.setUsuario(new Usuario());
		when(repM.findByUsuarioId(1L)).thenReturn(List.of(vinculo));

		comoLogado(get("/espacos/minhas"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(1));

		verify(repM).findByUsuarioId(1L);
	}

	@Test
	void listarMeusEspacosNaoExpoeSenha() throws Exception {
		Espaco espaco = new Espaco();
		espaco.setNome("Cozinha");

		MembroEspaco vinculo = membro(PapelMembro.DONO);
		vinculo.setEspaco(espaco);
		vinculo.setUsuario(new Usuario());
		when(repM.findByUsuarioId(1L)).thenReturn(List.of(vinculo));

		// a chave "password" ou "senha" não pode aparecer nem com valor nulo
		comoLogado(get("/espacos/minhas"))
			.andExpect(status().isOk())
			.andExpect(content().string(not(containsString("password"))))
			.andExpect(content().string(not(containsString("senha"))));
	}

	@Test
	void listarMeusEspacosSemVinculosRetornaListaVazia() throws Exception {
		when(repM.findByUsuarioId(1L)).thenReturn(List.of());

		comoLogado(get("/espacos/minhas"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$").isEmpty());
	}

	// =================================================================
	// POST /espacos/{idEspaco}/membros
	// =================================================================

	@Test
	void adicionarMembroSalvaComOPapelEOUsuarioCorretos() throws Exception {
		logadoComPapel(PapelMembro.ADMIN);
		prepararConviteValido();

		comoLogado(postJson("/espacos/1/membros",
				json("email", q("novo@gmail.com"), "papel", q("OPERADOR"))))
			.andExpect(status().isOk());

		ArgumentCaptor<MembroEspaco> salvo = ArgumentCaptor.forClass(MembroEspaco.class);
		verify(repM).save(salvo.capture());
		assertSame(convidado, salvo.getValue().getUsuario());
		assertEquals(PapelMembro.OPERADOR, salvo.getValue().getPapel());
	}

	/**
	 * Hierarquia: só dá para atribuir um papel ESTRITAMENTE inferior ao seu.
	 * Isso impede que um ADMIN crie outro ADMIN ou um DONO (escalada de privilégio).
	 */
	@ParameterizedTest(name = "{0} adiciona como {1} -> HTTP {2}")
	@CsvSource({
		"ADMIN, ADMIN,    403",
		"ADMIN, DONO,     403",
		"DONO,  DONO,     403",
		"ADMIN, GERENTE,  200",
		"ADMIN, OPERADOR, 200",
		"DONO,  ADMIN,    200"
	})
	void hierarquiaAoAdicionarMembro(PapelMembro papelLogado, PapelMembro papelConvidado, int esperado) throws Exception {
		logadoComPapel(papelLogado);
		prepararConviteValido();

		comoLogado(postJson("/espacos/1/membros",
				json("email", q("novo@gmail.com"), "papel", q(papelConvidado.name()))))
			.andExpect(status().is(esperado));

		if (esperado == 200) {
			verify(repM).save(any(MembroEspaco.class));
		} else {
			verify(repM, never()).save(any());
		}
	}

	/** GERENTE e OPERADOR não têm o papel mínimo (ADMIN): o interceptor barra antes do controller. */
	@ParameterizedTest(name = "{0} não pode adicionar membros")
	@EnumSource(value = PapelMembro.class, names = { "GERENTE", "OPERADOR" })
	void papelInsuficienteNaoAdicionaMembro(PapelMembro papelLogado) throws Exception {
		logadoComPapel(papelLogado);
		prepararConviteValido();

		comoLogado(postJson("/espacos/1/membros",
				json("email", q("novo@gmail.com"), "papel", q("OPERADOR"))))
			.andExpect(status().isForbidden());

		verify(repU, never()).findByEmail(anyString());
		verify(repM, never()).save(any());
	}

	@Test
	void adicionarMembroSemSerMembroDoEspacoRetorna403() throws Exception {
		logadoSemSerMembro();
		prepararConviteValido();

		comoLogado(postJson("/espacos/1/membros",
				json("email", q("novo@gmail.com"), "papel", q("OPERADOR"))))
			.andExpect(status().isForbidden());

		verify(repM, never()).save(any());
	}

	@Test
	void adicionarMembroComEmailInexistenteRetorna404() throws Exception {
		logadoComPapel(PapelMembro.ADMIN);
		when(repE.findById(1L)).thenReturn(Optional.of(mock(Espaco.class)));
		when(repU.findByEmail("ninguem@gmail.com")).thenReturn(null);

		comoLogado(postJson("/espacos/1/membros",
				json("email", q("ninguem@gmail.com"), "papel", q("OPERADOR"))))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.mensagem").value("Usuário não encontrado"));

		verify(repM, never()).save(any());
	}

	@Test
	void adicionarMembroEmEspacoInexistenteRetorna404() throws Exception {
		logadoComPapel(PapelMembro.ADMIN);
		when(repE.findById(1L)).thenReturn(Optional.empty());

		comoLogado(postJson("/espacos/1/membros",
				json("email", q("novo@gmail.com"), "papel", q("OPERADOR"))))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.mensagem").value("Espaço não encontrado"));

		verify(repM, never()).save(any());
	}

	@Test
	void adicionarMembroQueJaPertenceAoEspacoRetorna409() throws Exception {
		logadoComPapel(PapelMembro.ADMIN);
		prepararConviteValido();
		when(repM.existsByEspacoIdAndUsuarioId(1L, 2L)).thenReturn(true);

		comoLogado(postJson("/espacos/1/membros",
				json("email", q("novo@gmail.com"), "papel", q("OPERADOR"))))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.status").value(409))
			.andExpect(jsonPath("$.mensagem").value("Usuário já adicionado"));

		verify(repM, never()).save(any());
	}

	// =================================================================
	// GET /espacos/{idEspaco}/membros
	// =================================================================

	@Test
	void listarMembrosRetornaDadosPlanosSemSenha() throws Exception {
		logadoComPapel(PapelMembro.OPERADOR); // qualquer membro pode listar

		Usuario maria = usuario(5L, "Maria", "maria@gmail.com");
		MembroEspaco vinculo = membro(PapelMembro.ADMIN);
		vinculo.setUsuario(maria);
		when(repM.findByEspacoId(1L)).thenReturn(List.of(vinculo));

		comoLogado(get("/espacos/1/membros"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[0].idUsuario").value(5))
			.andExpect(jsonPath("$[0].nome").value("Maria"))
			.andExpect(jsonPath("$[0].email").value("maria@gmail.com"))
			.andExpect(jsonPath("$[0].papel").value("ADMIN"))
			.andExpect(content().string(not(containsString("password"))))
			.andExpect(content().string(not(containsString("senha"))));
	}

	@Test
	void listarMembrosSemSerMembroRetorna403() throws Exception {
		logadoSemSerMembro();

		comoLogado(get("/espacos/1/membros"))
			.andExpect(status().isForbidden());

		verify(repM, never()).findByEspacoId(anyLong());
	}

	// =================================================================
	// DELETE /espacos/{idEspaco}/membros/{idMembro}
	// =================================================================

	/** Mesma regra da adição: só remove quem tem papel ESTRITAMENTE inferior ao seu. */
	@ParameterizedTest(name = "{0} remove {1} -> HTTP {2}")
	@CsvSource({
		"ADMIN, ADMIN,    403",
		"ADMIN, DONO,     403",
		"DONO,  DONO,     403",
		"ADMIN, GERENTE,  200",
		"ADMIN, OPERADOR, 200",
		"DONO,  ADMIN,    200"
	})
	void hierarquiaAoRemoverMembro(PapelMembro papelLogado, PapelMembro papelAlvo, int esperado) throws Exception {
		logadoComPapel(papelLogado);
		MembroEspaco alvo = alvoNoEspaco(10L, papelAlvo, 1L);

		comoLogado(delete("/espacos/1/membros/10"))
			.andExpect(status().is(esperado));

		if (esperado == 200) {
			verify(repM).delete(alvo);
		} else {
			verify(repM, never()).delete(any());
		}
	}

	@Test
	void removerMembroRetornaMensagemDeSucesso() throws Exception {
		logadoComPapel(PapelMembro.ADMIN);
		alvoNoEspaco(10L, PapelMembro.OPERADOR, 1L);

		comoLogado(delete("/espacos/1/membros/10"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("Membro removido do espaço"));
	}

	/** IDOR: um ADMIN do espaço 1 não pode apagar o membro de outro espaço só trocando o id. */
	@Test
	void naoRemoveMembroDeOutroEspaco() throws Exception {
		logadoComPapel(PapelMembro.ADMIN);
		MembroEspaco deOutroEspaco = alvoNoEspaco(10L, PapelMembro.OPERADOR, 99L);

		comoLogado(delete("/espacos/1/membros/10"))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.mensagem").value("Membro não encontrado"));

		verify(repM, never()).delete(deOutroEspaco);
	}

	@Test
	void removerMembroInexistenteRetorna404() throws Exception {
		logadoComPapel(PapelMembro.ADMIN);
		when(repM.findById(10L)).thenReturn(Optional.empty());

		comoLogado(delete("/espacos/1/membros/10"))
			.andExpect(status().isNotFound());

		verify(repM, never()).delete(any());
	}

	@ParameterizedTest(name = "{0} não pode remover membros")
	@EnumSource(value = PapelMembro.class, names = { "GERENTE", "OPERADOR" })
	void papelInsuficienteNaoRemoveMembro(PapelMembro papelLogado) throws Exception {
		logadoComPapel(papelLogado);
		alvoNoEspaco(10L, PapelMembro.OPERADOR, 1L);

		comoLogado(delete("/espacos/1/membros/10"))
			.andExpect(status().isForbidden());

		verify(repM, never()).findById(anyLong());
		verify(repM, never()).delete(any());
	}

	@Test
	void idMembroNaoNumericoRetorna400() throws Exception {
		logadoComPapel(PapelMembro.ADMIN);

		comoLogado(delete("/espacos/1/membros/abc"))
			.andExpect(status().isBadRequest());
	}

	// =================================================================
	// auxiliares
	// =================================================================

	private RequestPostProcessor autenticado() {
		return authentication(new UsernamePasswordAuthenticationToken(logado, null, List.of()));
	}

	private ResultActions comoLogado(MockHttpServletRequestBuilder requisicao) throws Exception {
		return mockMvc.perform(requisicao.with(autenticado()));
	}

	private static MockHttpServletRequestBuilder postJson(String url, String corpo) {
		return post(url).contentType(MediaType.APPLICATION_JSON).content(corpo);
	}

	/** O interceptor @AcessoEspaco vai enxergar o usuário logado com este papel. */
	private void logadoComPapel(PapelMembro papel) {
		when(repM.findByEspacoIdAndUsuarioId(anyLong(), anyLong()))
			.thenReturn(Optional.of(membro(papel)));
	}

	private void logadoSemSerMembro() {
		when(repM.findByEspacoIdAndUsuarioId(anyLong(), anyLong()))
			.thenReturn(Optional.empty());
	}

	/** Espaço 1 existe, o e-mail novo@gmail.com existe e ainda não é membro. */
	private void prepararConviteValido() {
		Espaco espaco = mock(Espaco.class);
		when(repE.findById(1L)).thenReturn(Optional.of(espaco));
		when(repU.findByEmail("novo@gmail.com")).thenReturn(convidado);
		when(repM.existsByEspacoIdAndUsuarioId(1L, 2L)).thenReturn(false);
	}

	/** Cria um vínculo (id, papel) que pertence ao espaço informado e o registra no repository. */
	private MembroEspaco alvoNoEspaco(Long idMembro, PapelMembro papel, Long idEspacoDoAlvo) {
		Espaco espaco = mock(Espaco.class);
		when(espaco.getId()).thenReturn(idEspacoDoAlvo);

		MembroEspaco alvo = membro(papel);
		alvo.setEspaco(espaco);
		alvo.setUsuario(convidado);
		when(repM.findById(idMembro)).thenReturn(Optional.of(alvo));
		return alvo;
	}

	private static MembroEspaco membro(PapelMembro papel) {
		MembroEspaco m = new MembroEspaco();
		m.setPapel(papel);
		return m;
	}

	private static Usuario usuario(long id, String nome, String email) {
		Usuario u = mock(Usuario.class);
		when(u.getId()).thenReturn(id);
		when(u.getNome()).thenReturn(nome);
		when(u.getEmail()).thenReturn(email);
		return u;
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