package Hisopi.Hisopi.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import Hisopi.Hisopi.Enum.UserRole;
import Hisopi.Hisopi.infra.security.RateLimitService;
import Hisopi.Hisopi.model.Usuario;
import Hisopi.Hisopi.repository.UsuarioRepository;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthFluxoIntegrationTest {

	private static final String EMAIL = "maria@email.com";
	private static final String SENHA = "senha12345678";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private UsuarioRepository usuarioRepository;

	@Autowired
	private RateLimitService rateLimitService;

	@BeforeEach
	void limparRateLimit() {
	    rateLimitService.reset();
	}
	
	private void cadastrar(String nome, String email, String senha) throws Exception {
		mockMvc.perform(post("/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"nome":"%s","login":"%s","senha":"%s"}
						""".formatted(nome, email, senha)))
			.andExpect(status().isOk());
	}

	private JsonNode logar(String email, String senha) throws Exception {
		String corpo = mockMvc.perform(post("/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"login":"%s","senha":"%s"}
						""".formatted(email, senha)))
			.andExpect(status().isOk())
			.andReturn().getResponse().getContentAsString();
		return objectMapper.readTree(corpo);
	}

	private JsonNode refresh(String refreshToken) throws Exception {
		String corpo = mockMvc.perform(post("/auth/refresh")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"refreshToken":"%s"}
						""".formatted(refreshToken)))
			.andExpect(status().isOk())
			.andReturn().getResponse().getContentAsString();
		return objectMapper.readTree(corpo);
	}

	@Test
	void cadastrarLogarEAcessarMe() throws Exception {
		cadastrar("Maria", EMAIL, SENHA);

		String accessToken = logar(EMAIL, SENHA).get("accessToken").asString();

		mockMvc.perform(get("/auth/me")
				.header("Authorization", "Bearer " + accessToken))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.nome").value("Maria"))
			.andExpect(jsonPath("$.email").value(EMAIL))
			.andExpect(jsonPath("$.id").isNumber());
	}

	@Test
	void cadastroDevePersistirUsuarioComSenhaCriptografadaERoleUser() throws Exception {
		cadastrar("Maria", EMAIL, SENHA);

		Usuario salvo = (Usuario) usuarioRepository.findByEmail(EMAIL);

		assertThat(salvo).isNotNull();
		assertThat(salvo.getRole()).isEqualTo(UserRole.USER);
		assertThat(salvo.getPassword()).isNotEqualTo(SENHA);
		assertThat(salvo.getPassword()).startsWith("$2");
	}

	@Test
	void cadastroNaoDeveAceitarRoleEnviadaPeloCliente() throws Exception {
		mockMvc.perform(post("/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"nome":"Maria","login":"%s","senha":"%s","role":"ADMIN"}
						""".formatted(EMAIL, SENHA)))
			.andExpect(status().isOk());

		Usuario salvo = (Usuario) usuarioRepository.findByEmail(EMAIL);
		assertThat(salvo.getRole()).isEqualTo(UserRole.USER);
	}

	@Test
	void cadastroComEmailJaExistenteRetornaErroDeNegocio() throws Exception {
		cadastrar("Maria", EMAIL, SENHA);

		mockMvc.perform(post("/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"nome":"Outra","login":"%s","senha":"%s"}
						""".formatted(EMAIL, SENHA)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.mensagem").value("E-mail já cadastrado"));
	}

	@Test
	void loginDeveRetornarDoisTokensDistintos() throws Exception {
		cadastrar("Maria", EMAIL, SENHA);

		JsonNode tokens = logar(EMAIL, SENHA);

		assertThat(tokens.get("accessToken").asString()).isNotBlank();
		assertThat(tokens.get("refreshToken").asString()).isNotBlank();
		assertThat(tokens.get("accessToken").asString())
				.isNotEqualTo(tokens.get("refreshToken").asString());
	}

	@Test
	void loginComSenhaErradaRetorna401ComUsuarioReal() throws Exception {
		cadastrar("Maria", EMAIL, SENHA);

		mockMvc.perform(post("/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"login":"%s","senha":"senhaerrada123"}
						""".formatted(EMAIL)))
			.andExpect(status().isUnauthorized());
	}

	@Test
	void loginComUsuarioInexistenteRetorna401() throws Exception {
		mockMvc.perform(post("/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"login":"naoexiste@email.com","senha":"senha12345678"}
						"""))
			.andExpect(status().isUnauthorized());
	}

	@Test
	void refreshDeveGerarNovoAccessTokenValido() throws Exception {
		cadastrar("Maria", EMAIL, SENHA);
		String refreshToken = logar(EMAIL, SENHA).get("refreshToken").asString();

		JsonNode novos = refresh(refreshToken);

		assertThat(novos.get("accessToken").asString()).isNotBlank();
		assertThat(novos.get("refreshToken").asString()).isNotBlank();

		mockMvc.perform(get("/auth/me")
				.header("Authorization", "Bearer " + novos.get("accessToken").asString()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.email").value(EMAIL));
	}

	@Test
	void refreshComTokenInventadoRetorna401() throws Exception {
		mockMvc.perform(post("/auth/refresh")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"refreshToken":"token.invalido.aqui"}
						"""))
			.andExpect(status().isUnauthorized());
	}

	@Test
	void refreshComUsuarioRemovidoRetorna401() throws Exception {
		cadastrar("Maria", EMAIL, SENHA);
		String refreshToken = logar(EMAIL, SENHA).get("refreshToken").asString();

		Usuario usuario = (Usuario) usuarioRepository.findByEmail(EMAIL);
		usuarioRepository.delete(usuario);
		usuarioRepository.flush();

		mockMvc.perform(post("/auth/refresh")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"refreshToken":"%s"}
						""".formatted(refreshToken)))
			.andExpect(status().isUnauthorized());
	}

	@Test
	void accessTokenNaoDeveSerAceitoComoRefreshToken() throws Exception {
		cadastrar("Maria", EMAIL, SENHA);
		String accessToken = logar(EMAIL, SENHA).get("accessToken").asString();

		mockMvc.perform(post("/auth/refresh")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"refreshToken":"%s"}
						""".formatted(accessToken)))
			.andExpect(status().isUnauthorized());
	}

	@Test
	void refreshTokenNaoDeveSerAceitoParaAcessarEndpointProtegido() throws Exception {
		cadastrar("Maria", EMAIL, SENHA);
		String refreshToken = logar(EMAIL, SENHA).get("refreshToken").asString();

		mockMvc.perform(get("/auth/me")
				.header("Authorization", "Bearer " + refreshToken))
			.andExpect(status().isUnauthorized());
	}

	@Test
	void meSemTokenRetorna401() throws Exception {
		mockMvc.perform(get("/auth/me"))
			.andExpect(status().isUnauthorized());
	}

	@Test
	void meComTokenAdulteradoRetorna401() throws Exception {
		cadastrar("Maria", EMAIL, SENHA);
		String accessToken = logar(EMAIL, SENHA).get("accessToken").asString();
		String adulterado = accessToken.substring(0, accessToken.length() - 2) + "xx";

		mockMvc.perform(get("/auth/me")
				.header("Authorization", "Bearer " + adulterado))
			.andExpect(status().isUnauthorized());
	}

	@Test
	void meComTokenLixoRetorna401() throws Exception {
		mockMvc.perform(get("/auth/me")
				.header("Authorization", "Bearer token.invalido.aqui"))
			.andExpect(status().isUnauthorized());
	}

	@Test
	void meSemPrefixoBearerRetorna401() throws Exception {
		cadastrar("Maria", EMAIL, SENHA);
		String accessToken = logar(EMAIL, SENHA).get("accessToken").asString();

		mockMvc.perform(get("/auth/me")
				.header("Authorization", accessToken))
			.andExpect(status().isUnauthorized());
	}
}