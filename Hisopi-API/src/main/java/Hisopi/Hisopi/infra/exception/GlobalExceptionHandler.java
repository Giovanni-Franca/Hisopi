package Hisopi.Hisopi.infra.exception;

import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import Hisopi.Hisopi.DTO.ErrorResponseDTO;

@RestControllerAdvice
public class GlobalExceptionHandler {

	private ResponseEntity<ErrorResponseDTO> montar(HttpStatus status, String mensagem){
		return ResponseEntity.status(status).body(new ErrorResponseDTO(
				status.value(), 
				status.name(), 
				mensagem
				));
	}
	
	@ExceptionHandler (RegraNegocioException.class)
	public ResponseEntity<ErrorResponseDTO> tratarRegraNegocio(RegraNegocioException ex){
		return montar(HttpStatus.BAD_REQUEST,ex.getMessage());
	}
	
	// login invalido
	@ExceptionHandler (BadCredentialsException.class)
	public ResponseEntity<ErrorResponseDTO> tratarCredencialInvalida(BadCredentialsException ex){
		return montar(HttpStatus.UNAUTHORIZED, ex.getMessage());
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
	    return montar(HttpStatus.UNAUTHORIZED, ex.getMessage());
	}
	
	@ExceptionHandler(AcessoNegadoException.class)
	public ResponseEntity<ErrorResponseDTO> tratarAcessoNegado(AcessoNegadoException ex) {
		return montar(HttpStatus.FORBIDDEN, ex.getMessage());
	}
	
	@ExceptionHandler(NaoEncontradoException.class)
	public ResponseEntity<ErrorResponseDTO> tratarNaoLocalizado(NaoEncontradoException ex){
		return montar(HttpStatus.NOT_FOUND, ex.getMessage());
	}
	
	@ExceptionHandler(ConflitoException.class)
	public ResponseEntity<ErrorResponseDTO> tratarConflito(ConflitoException ex){
		return montar(HttpStatus.CONFLICT, ex.getMessage());
	}
	
	@ExceptionHandler(ObjectOptimisticLockingFailureException.class)
	public ResponseEntity<ErrorResponseDTO> concorrencia(ObjectOptimisticLockingFailureException ex){
		return montar (HttpStatus.CONFLICT, "O registro foi alterado por outra operação, tente novamente");
	}
	
	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ErrorResponseDTO> parametroInvalido(MethodArgumentTypeMismatchException ex) {
	    return montar(HttpStatus.BAD_REQUEST,
	            "Valor inválido para o parâmetro '" + ex.getName() + "'");
	}
	 
	@ExceptionHandler(MissingServletRequestParameterException.class)
	public ResponseEntity<ErrorResponseDTO> parametroAusente(MissingServletRequestParameterException ex) {
	    return montar(HttpStatus.BAD_REQUEST, "Parâmetro obrigatório ausente: '" + ex.getParameterName() + "'");
	}

}
