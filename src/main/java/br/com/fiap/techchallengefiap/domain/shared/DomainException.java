package br.com.fiap.techchallengefiap.domain.shared;

public class DomainException extends RuntimeException {
    public DomainException(String message) {
        super(message);
    }
}
