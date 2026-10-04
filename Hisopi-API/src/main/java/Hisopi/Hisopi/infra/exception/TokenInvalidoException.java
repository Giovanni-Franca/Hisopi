package Hisopi.Hisopi.infra.exception;

public class TokenInvalidoException extends RuntimeException {

    public TokenInvalidoException(String mensagem) {
        super(mensagem);
    }
}