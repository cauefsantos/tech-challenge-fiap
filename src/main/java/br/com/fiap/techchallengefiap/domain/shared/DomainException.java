package br.com.fiap.oficina.domain.shared;

public class DomainException extends RuntimeException {
    public DomainException(String message) {
        super(message);
    }
}
