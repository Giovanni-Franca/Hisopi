package Hisopi.Hisopi.DTO;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import Hisopi.Hisopi.Enum.PapelMembro;
import Hisopi.Hisopi.infra.security.TokenService;
import Hisopi.Hisopi.model.MembroEspaco;
import Hisopi.Hisopi.model.Usuario;
import Hisopi.Hisopi.repository.EspacoRepository;
import Hisopi.Hisopi.repository.InsumoRepository;
import Hisopi.Hisopi.repository.LoteInsumoRepository;
import Hisopi.Hisopi.repository.MembroEspacoRepository;
import Hisopi.Hisopi.repository.MovimentacaoEstoqueRepository;
import Hisopi.Hisopi.repository.ReceitaInsumoRepository;
import Hisopi.Hisopi.repository.ReceitaRepository;
import Hisopi.Hisopi.repository.UsuarioRepository;

/**
 * Detector de constraints faltando nos DTOs de entrada.
 *
 * COMO FUNCIONA
 *  - Cada caso envia um corpo INVÁLIDO para um endpoint e exige 400 no formato
 *    do ErrorResponseDTO.
 *  - Os repositories são mocks. Se a constraint faltar, o corpo passa pela
 *    validação, o controller executa e a resposta vira 200/404/500 em vez de 400.
 *    O teste falha e o nome do caso diz exatamente qual constraint falta.
 *  - O usuário logado é DONO do espaço, então o interceptor @AcessoEspaco
 *    sempre libera. O que sobra para barrar o corpo é a validação.
 *
 * COMO LER UMA FALHA
 *  "expected 400 but was 404" ou 200 ou 500 => falta @NotBlank/@Positive/etc. no DTO.
 */
@SpringBootTest
@AutoConfigureMockMvc
public class ValidacaoDtosTest {

	private static final String TIPO_ESPACO = "ORGANIZACAO";
	private static final String PAPEL_VALIDO = "OPERADOR";
	private static final String CATEGORIA = "ALIMENTO";       
	private static final String UNIDADE = "KG";                
	private static final String TIPO_PERDA = "PERDA_VALIDADE";
	private static final String TIPO_RECEITA = "SUGESTAO_CONSUMO";

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean private UsuarioRepository repU;
	@MockitoBean private MembroEspacoRepository repMembro;
	@MockitoBean private EspacoRepository repEspaco;
	@MockitoBean private InsumoRepository repInsumo;
	@MockitoBean private LoteInsumoRepository repLote;
	@MockitoBean private MovimentacaoEstoqueRepository repMov;
	@MockitoBean private ReceitaRepository repReceita;
	@MockitoBean private ReceitaInsumoRepository repReceitaInsumo;
	@MockitoBean private TokenService tokenService;

	private Usuario usuario;

	@BeforeEach
	void prepararUsuarioDono() {
		usuario = mock(Usuario.class);
		when(usuario.getId()).thenReturn(1L);
		MembroEspaco dono = new MembroEspaco();
		dono.setPapel(PapelMembro.DONO);
		when(repMembro.findByEspacoIdAndUsuarioId(anyLong(), anyLong()))
			.thenReturn(Optional.of(dono));
	}

	// =================================================================
	// 1) Corpos INVÁLIDOS: todos devem resultar em 400 padronizado
	// =================================================================

	@ParameterizedTest(name = "{0}")
	@MethodSource("corposInvalidos")
	void corpoInvalidoDeveRetornar400(String nome, String metodo, String url, String corpo) throws Exception {
		// todas as mensagens abaixo levam o nome do caso, porque o relatório do
		// JUnit nem sempre mostra o displayName de testes parametrizados
		MvcResult resultado = executar(nome, metodo, url, corpo);
		MockHttpServletResponse resposta = resultado.getResponse();

		assertEquals(400, resposta.getStatus(),
			"[" + nome + "] esperado 400, mas veio " + resposta.getStatus()
			+ ". A constraint do DTO está faltando.");

		// 400 sem corpo = o Spring recusou, mas nenhum handler montou o ErrorResponseDTO
		String corpoResposta = resposta.getContentAsString();
		assertFalse(corpoResposta.isBlank(),
			"[" + nome + "] 400 sem corpo. Falta um handler padronizado para esta exceção.");
		assertTrue(corpoResposta.contains("\"status\":400") && corpoResposta.contains("\"mensagem\""),
			"[" + nome + "] 400 fora do formato ErrorResponseDTO: " + corpoResposta);

		nadaFoiSalvo();
	}

	static Stream<Arguments> corposInvalidos() {
		return Stream.of(
			// ---------- Espaço (EspacoDTO) ----------
			Arguments.of("Espaço: sem corpo", "POST", "/espacos", ""),
			Arguments.of("Espaço: JSON malformado", "POST", "/espacos", "{ \"nome\": "),
			Arguments.of("Espaço: corpo vazio", "POST", "/espacos", "{}"),
			Arguments.of("Espaço: nome em branco", "POST", "/espacos",
				json("nome", q("  "), "tipo", q(TIPO_ESPACO))),
			Arguments.of("Espaço: tipo ausente", "POST", "/espacos",
				json("nome", q("Cozinha"))),

			// ---------- Membro (MembroDTO) ----------
			Arguments.of("Membro: corpo vazio", "POST", "/espacos/1/membros", "{}"),
			Arguments.of("Membro: e-mail malformado", "POST", "/espacos/1/membros",
				json("email", q("nao-e-email"), "papel", q(PAPEL_VALIDO))),
			Arguments.of("Membro: e-mail ausente", "POST", "/espacos/1/membros",
				json("papel", q(PAPEL_VALIDO))),
			Arguments.of("Membro: papel ausente", "POST", "/espacos/1/membros",
				json("email", q("a@b.com"))),
			// depende do handler de HttpMessageNotReadableException
			Arguments.of("Membro: papel inexistente no enum", "POST", "/espacos/1/membros",
				json("email", q("a@b.com"), "papel", q("CHEFE"))),

			// ---------- Insumo (InsumoDTO) ----------
			Arguments.of("Insumo: corpo vazio", "POST", "/espacos/1/insumos", "{}"),
			Arguments.of("Insumo: nome em branco", "POST", "/espacos/1/insumos",
				insumo(q(" "), "5", "2.5")),
			Arguments.of("Insumo: estoqueMinimo negativo", "POST", "/espacos/1/insumos",
				insumo(q("Farinha"), "-1", "2.5")),
			Arguments.of("Insumo: custoUnitario negativo", "POST", "/espacos/1/insumos",
				insumo(q("Farinha"), "5", "-2.5")),
			Arguments.of("Insumo: categoria ausente", "POST", "/espacos/1/insumos",
				json("nome", q("Farinha"), "unidadeMedida", q(UNIDADE),
					"estoqueMinimo", "5", "custoUnitario", "2.5")),
			Arguments.of("Insumo: unidadeMedida ausente", "POST", "/espacos/1/insumos",
				json("nome", q("Farinha"), "categoria", q(CATEGORIA),
					"estoqueMinimo", "5", "custoUnitario", "2.5")),
			// o PUT reutiliza o mesmo DTO
			Arguments.of("Insumo (editar): corpo vazio", "PUT", "/espacos/1/insumos/5", "{}"),

			// ---------- Lote (LoteDTO) ----------
			Arguments.of("Lote: corpo vazio", "POST", "/espacos/1/insumos/5/lotes", "{}"),
			Arguments.of("Lote: quantidade zero", "POST", "/espacos/1/insumos/5/lotes",
				lote("0", q("2030-12-31"))),
			Arguments.of("Lote: quantidade negativa", "POST", "/espacos/1/insumos/5/lotes",
				lote("-3", q("2030-12-31"))),
			Arguments.of("Lote: dataValidade ausente", "POST", "/espacos/1/insumos/5/lotes",
				json("quantidade", "10", "fornecedor", q("Forn"))),
			Arguments.of("Lote: dataValidade em formato inválido", "POST", "/espacos/1/insumos/5/lotes",
				lote("10", q("31/12/2030"))),

			// ---------- Perda (PerdaDTO) ----------
			Arguments.of("Perda: corpo vazio", "PUT", "/espacos/1/insumos/lotes/9/perda", "{}"),
			Arguments.of("Perda: tipo ausente", "PUT", "/espacos/1/insumos/lotes/9/perda",
				json("quantidade", "1", "motivo", q("Quebra"))),
			Arguments.of("Perda: quantidade zero", "PUT", "/espacos/1/insumos/lotes/9/perda",
				perda("0", q(TIPO_PERDA))),
			Arguments.of("Perda: quantidade negativa", "PUT", "/espacos/1/insumos/lotes/9/perda",
				perda("-2", q(TIPO_PERDA))),
			Arguments.of("Perda: tipo inexistente no enum", "PUT", "/espacos/1/insumos/lotes/9/perda",
				perda("1", q("XYZ"))),

			// ---------- Receita (ReceitaDTO) ----------
			// confirme o mapping base do seu ReceitaController
			Arguments.of("Receita: corpo vazio", "POST", "/espacos/1/receitas", "{}"),
			Arguments.of("Receita: nome em branco", "POST", "/espacos/1/receitas",
				json("nome", q(" "), "tipo", q(TIPO_RECEITA), "modoPreparo", q("Misturar"))),
			Arguments.of("Receita: tipo ausente", "POST", "/espacos/1/receitas",
				json("nome", q("Bolo"), "modoPreparo", q("Misturar"))),

			// ---------- Vínculo receita-insumo (ReceitaInsumoDTO) ----------
			Arguments.of("Vínculo: corpo vazio", "POST", "/espacos/1/receitas/3/insumos", "{}"),
			Arguments.of("Vínculo: idInsumo ausente", "POST", "/espacos/1/receitas/3/insumos",
				json("quantidadePorUnidade", "0.5")),
			Arguments.of("Vínculo: quantidade zero", "POST", "/espacos/1/receitas/3/insumos",
				json("idInsumo", "1", "quantidadePorUnidade", "0")),
			Arguments.of("Vínculo: quantidade negativa", "POST", "/espacos/1/receitas/3/insumos",
				json("idInsumo", "1", "quantidadePorUnidade", "-0.5"))
		);
	}

	// =================================================================
	// 2) Corpos VÁLIDOS: nunca podem retornar 400
	// Protege os testes acima contra falso positivo. Se um destes falhar, o
	// problema está nos valores de enum das constantes (ou em uma constraint
	// exagerada), e não nos testes de corpo inválido.
	// =================================================================

	@ParameterizedTest(name = "{0}")
	@MethodSource("corposValidos")
	void corpoValidoNaoPodeRetornar400(String nome, String metodo, String url, String corpo) throws Exception {
		int statusHttp = executar(nome, metodo, url, corpo).getResponse().getStatus();
		assertNotEquals(400, statusHttp,
			"[" + nome + "] corpo válido foi recusado. Confira as constantes de enum e as constraints do DTO.");
	}

	static Stream<Arguments> corposValidos() {
		return Stream.of(
			Arguments.of("Válido: criar espaço", "POST", "/espacos",
				json("nome", q("Cozinha"), "tipo", q(TIPO_ESPACO))),
			Arguments.of("Válido: adicionar membro", "POST", "/espacos/1/membros",
				json("email", q("a@b.com"), "papel", q(PAPEL_VALIDO))),
			Arguments.of("Válido: criar insumo", "POST", "/espacos/1/insumos",
				insumo(q("Farinha"), "5", "2.5")),
			Arguments.of("Válido: registrar lote", "POST", "/espacos/1/insumos/5/lotes",
				lote("10", q("2030-12-31"))),
			Arguments.of("Válido: registrar perda", "PUT", "/espacos/1/insumos/lotes/9/perda",
				perda("1", q(TIPO_PERDA))),
			Arguments.of("Válido: perda sem quantidade (lote inteiro)", "PUT", "/espacos/1/insumos/lotes/9/perda",
				json("tipo", q(TIPO_PERDA), "motivo", q("Vencido"))),
			Arguments.of("Válido: criar receita", "POST", "/espacos/1/receitas",
				json("nome", q("Bolo"), "tipo", q(TIPO_RECEITA), "modoPreparo", q("Misturar"))),
			Arguments.of("Válido: vincular insumo", "POST", "/espacos/1/receitas/3/insumos",
				json("idInsumo", "1", "quantidadePorUnidade", "0.5"))
		);
	}

	// =================================================================
	// auxiliares
	// =================================================================

	// exceção sem handler: o MockMvc a propaga em vez de devolver 500, então
	// convertemos em falha com o nome do caso e a causa raiz
	private MvcResult executar(String nome, String metodo, String url, String corpo) {
		try {
			return mockMvc.perform(requisicao(metodo, url, corpo)).andReturn();
		} catch (Exception e) {
			throw new AssertionError("[" + nome + "] exceção sem handler: " + causaRaiz(e), e);
		}
	}

	private static String causaRaiz(Throwable t) {
		while (t.getCause() != null) {
			t = t.getCause();
		}
		return t.getClass().getSimpleName() + ": " + t.getMessage();
	}

	private MockHttpServletRequestBuilder requisicao(String metodo, String url, String corpo) {
		return request(HttpMethod.valueOf(metodo), url)
			.contentType(MediaType.APPLICATION_JSON)
			.content(corpo)
			.with(authentication(new UsernamePasswordAuthenticationToken(usuario, null, List.of())));
	}

	// se algum corpo inválido passou da validação, algum save pode ter sido chamado
	private void nadaFoiSalvo() {
		verify(repEspaco, never()).save(any());
		verify(repMembro, never()).save(any());
		verify(repInsumo, never()).save(any());
		verify(repLote, never()).save(any());
		verify(repMov, never()).save(any());
		verify(repReceita, never()).save(any());
		verify(repReceitaInsumo, never()).save(any());
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

	private static String insumo(String nome, String estoqueMinimo, String custo) {
		return json("nome", nome, "categoria", q(CATEGORIA), "unidadeMedida", q(UNIDADE),
			"estoqueMinimo", estoqueMinimo, "custoUnitario", custo);
	}

	private static String lote(String quantidade, String dataValidade) {
		return json("quantidade", quantidade, "dataValidade", dataValidade, "fornecedor", q("Fornecedor"));
	}

	private static String perda(String quantidade, String tipo) {
		return json("quantidade", quantidade, "tipo", tipo, "motivo", q("Quebra"));
	}
}