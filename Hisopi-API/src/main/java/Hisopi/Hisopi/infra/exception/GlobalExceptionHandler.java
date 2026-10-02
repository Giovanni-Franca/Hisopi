package Hisopi.Hisopi.infra.exception;

import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import Hisopi.Hisopi.DTO.ErrorResponseDTO;

@RestControllerAdvice
public class GlobalExceptionHandler {

	
	@ExceptionHandler (RegraNegocioException.class)
	public ResponseEntity<ErrorResponseDTO> tratarRegraNegocio(RegraNegocioException ex){
		ErrorResponseDTO error = new ErrorResponseDTO(
				400,
				"BAD_REQUEST",
				ex.getMessage()
		);
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
	}
	
	// login invalido
	@ExceptionHandler (BadCredentialsException.class)
	public ResponseEntity<ErrorResponseDTO> tratarCredencialInvalida(BadCredentialsException ex){
		ErrorResponseDTO error = new ErrorResponseDTO(
				401,
				"UNAUTHORIZED",
				"Usuário ou senha inválidos"
		);
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
	}

	// senha recebida invalida
	@ExceptionHandler (MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponseDTO> tratarValidacao(MethodArgumentNotValidException ex) {
		String mensagem = ex.getBindingResult()
	            .getFieldErrors()
	            .stream()
	            .map(error -> error.getField() + ": " + error.getDefaultMessage())
	            .collect(Collectors.joining("; "));

	    ErrorResponseDTO error = new ErrorResponseDTO(
	            400,
	            "VALIDATION_ERROR",
	            mensagem
	    );
	    return ResponseEntity.badRequest().body(error);
	}
	
	// trata tokens invalidos/expirados
	@ExceptionHandler(TokenInvalidoException.class)
	public ResponseEntity<ErrorResponseDTO> tratarTokenInvalido(TokenInvalidoException ex) {
	    ErrorResponseDTO erro = new ErrorResponseDTO(
	            401,
	            "UNAUTHORIZED",
	            ex.getMessage()
	    );
	    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(erro);
	}
	
	@ExceptionHandler(AcessoNegadoException.class)
		public ResponseEntity<ErrorResponseDTO> tratarAcessoNegado(AcessoNegadoException ex) {
	    ErrorResponseDTO erro = new ErrorResponseDTO(
	            403,
	            "FORBIDDEN",
	            ex.getMessage()
	    );
	    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(erro);
	}
	
}
