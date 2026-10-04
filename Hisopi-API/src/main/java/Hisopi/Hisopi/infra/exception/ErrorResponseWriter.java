package Hisopi.Hisopi.infra.exception;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import Hisopi.Hisopi.DTO.ErrorResponseDTO;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.ObjectMapper;

@Component
public class ErrorResponseWriter {
	private final ObjectMapper objectMapper;
	
	public ErrorResponseWriter(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}
	
	public void write(
			HttpServletResponse response,
            HttpStatus status,
            String erro,
            String mensagem) throws IOException {
		
		ErrorResponseDTO responseDTO = new ErrorResponseDTO(
				status.value(), 
				erro, 
				mensagem);
		
		response.setStatus(status.value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding(StandardCharsets.UTF_8.name());
		response.getWriter().write(objectMapper.writeValueAsString(responseDTO));
	}
	
}
