package Hisopi.Hisopi.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import Hisopi.Hisopi.model.Usuario;
import Hisopi.Hisopi.repository.UsuarioRepository;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthenticationControllerTest {

	@Autowired
	private MockMvc mockMvc;
	
	@MockitoBean
	private UsuarioRepository repU;
	
	
	@Test
	void recusarCadastroJaExistente() throws Exception {
		when(repU.findByEmail("teste@gmail.com")).thenReturn(new Usuario());
		
		String requisicao = """
				{
					"nome": "Teste123",
					"login": "teste@gmail.com",
					"senha": "senha12345678",
					"role": "USER"
				}
				""";
		mockMvc.perform(
				post("/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(requisicao)
		)
	.andExpect(status().isBadRequest());
	}
	
	@Test
	void cadastrarEmailNovo() throws Exception{
		when(repU.findByEmail("teste2@gmail.com")).thenReturn(null);
		
		String requisicao = """
				{
					"nome": "Teste1234",
					"login": "teste2@gmail.com",
					"senha": "senha12345678",
					"role": "USER"
				}
				""";
		mockMvc.perform(
				post("/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(requisicao)
		)
	.andExpect(status().isOk());
	
	verify(repU).findByEmail("teste2@gmail.com");
	}
	
}
