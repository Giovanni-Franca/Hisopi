package Hisopi.Hisopi.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import Hisopi.Hisopi.infra.security.TokenService;
import Hisopi.Hisopi.model.Usuario;
import Hisopi.Hisopi.repository.UsuarioRepository;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthenticationControllerTest {
 
	@Autowired
	private MockMvc mockMvc;
 
	@MockitoBean
	private UsuarioRepository repU;
 
	@MockitoBean
	private AuthenticationManager authenticationManager;
 
	@MockitoBean
	private TokenService tokenService;
 
	// =====================================================
	// POST /auth/register
	// =====================================================
 
	@Test
	void recusarCadastroJaExistente() throws Exception {
		when(repU.findByEmail("teste@gmail.com")).thenReturn(new Usuario());
 
		mockMvc.perform(
				post("/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(registro("Teste123", "teste@gmail.com", "senha12345678")))
			.andExpect(status().isBadRequest()) // troque para isUnprocessableEntity() se o handler usar 422
			.andExpect(jsonPath("$.mensagem").value("E-mail já cadastrado"));
 
		verify(repU, never()).save(any());
	}
 
	@Test
	void cadastrarEmailNovo() throws Exception {
		when(repU.findByEmail("teste2@gmail.com")).thenReturn(null);
 
		mockMvc.perform(
				post("/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(registro("Teste1234", "teste2@gmail.com", "senha12345678")))
			.andExpect(status().isOk());
 
		verify(repU).findByEmail("teste2@gmail.com");
 
		// confere o que foi persistido: senha nunca em texto puro
		ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
		verify(repU).save(captor.capture());
 
		Usuario salvo = captor.getValue();
		assertEquals("teste2@gmail.com", salvo.getEmail());
		assertNotEquals("senha12345678", salvo.getPassword());
		assertTrue(new BCryptPasswordEncoder().matches("senha12345678", salvo.getPassword()));
	}
 
	@Test
	void cadastroComDadosInvalidosRetorna400() throws Exception {
		// ajuste conforme as validações do seu RegisterDTO
		mockMvc.perform(
				post("/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(registro("", "nao-e-um-email", "1")))
			.andExpect(status().isBadRequest());
 
		verify(repU, never()).save(any());
	}
 
	// =====================================================
	// POST /auth/login
	// =====================================================
 
	@Test
	void loginComCredenciaisValidasRetornaTokens() throws Exception {
		Usuario usuario = new Usuario();
		when(authenticationManager.authenticate(any()))
			.thenReturn(new UsernamePasswordAuthenticationToken(usuario, null, List.of()));
		when(tokenService.generateToken(usuario)).thenReturn("access-123");
		when(tokenService.generateRefreshToken(usuario)).thenReturn("refresh-456");
 
		mockMvc.perform(
				post("/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{ "login": "teste@gmail.com", "senha": "senha12345678" }
						"""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.accessToken").value("access-123"))
			.andExpect(jsonPath("$.refreshToken").value("refresh-456"));
	}
 
	@Test
	void loginComCredenciaisInvalidasRetorna401() throws Exception {
		when(authenticationManager.authenticate(any()))
			.thenThrow(new BadCredentialsException("Credenciais inválidas"));
 
		mockMvc.perform(
				post("/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{ "login": "teste@gmail.com", "senha": "errada" }
						"""))
			.andExpect(status().isUnauthorized());
	}
 
	@Test
	void loginSemCorpoValidoRetorna400() throws Exception {
		mockMvc.perform(
				post("/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{}"))
			.andExpect(status().isBadRequest());
 
		verify(authenticationManager, never()).authenticate(any());
	}
 
	// =====================================================
	// POST /auth/refresh
	// =====================================================
 
	@Test
	void refreshComTokenValidoRetornaNovosTokens() throws Exception {
		Usuario usuario = new Usuario();
		when(tokenService.validateRefreshToken("refresh-456")).thenReturn("teste@gmail.com");
		when(repU.findByEmail("teste@gmail.com")).thenReturn(usuario);
		when(tokenService.generateToken(usuario)).thenReturn("novo-access");
		when(tokenService.generateRefreshToken(usuario)).thenReturn("novo-refresh");
 
		mockMvc.perform(
				post("/auth/refresh")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{ "refreshToken": "refresh-456" }
						"""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.accessToken").value("novo-access"))
			.andExpect(jsonPath("$.refreshToken").value("novo-refresh"));
	}
 
	@Test
	void refreshComTokenInvalidoRetorna401() throws Exception {
		when(tokenService.validateRefreshToken("token-ruim")).thenReturn("");
 
		mockMvc.perform(
				post("/auth/refresh")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{ "refreshToken": "token-ruim" }
						"""))
			.andExpect(status().isUnauthorized());
	}
 
	@Test
	void refreshComUsuarioInexistenteRetorna401() throws Exception {
		when(tokenService.validateRefreshToken("refresh-456")).thenReturn("sumiu@gmail.com");
		when(repU.findByEmail("sumiu@gmail.com")).thenReturn(null);
 
		mockMvc.perform(
				post("/auth/refresh")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{ "refreshToken": "refresh-456" }
						"""))
			.andExpect(status().isUnauthorized());
	}
 
	// =====================================================
	// GET /auth/me
	// =====================================================
 
	@Test
	void meRetornaDadosDoUsuarioAutenticado() throws Exception {
		Usuario usuario = mock(Usuario.class);
		when(usuario.getId()).thenReturn(1L);
		when(usuario.getNome()).thenReturn("Teste123");
		when(usuario.getEmail()).thenReturn("teste@gmail.com");
 
		mockMvc.perform(
				get("/auth/me")
				.with(authentication(
					new UsernamePasswordAuthenticationToken(usuario, null, List.of()))))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(1))
			.andExpect(jsonPath("$.nome").value("Teste123"))
			.andExpect(jsonPath("$.email").value("teste@gmail.com"));
	}
 
	@Test
	void meSemAutenticacaoRetorna401() throws Exception {
		mockMvc.perform(get("/auth/me"))
			.andExpect(status().isUnauthorized());
	}
 
	// =====================================================
	// auxiliar
	// =====================================================
 
	private String registro(String nome, String login, String senha) {
		return """
				{
					"nome": "%s",
					"login": "%s",
					"senha": "%s",
					"role": "USER"
				}
				""".formatted(nome, login, senha);
	
	}
}
