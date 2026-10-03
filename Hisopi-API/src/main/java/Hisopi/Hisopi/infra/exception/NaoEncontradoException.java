package Hisopi.Hisopi.infra.exception;

public class NaoEncontradoException extends RuntimeException {
	public NaoEncontradoException(String mensagem) {
		super(mensagem);
	}
}
