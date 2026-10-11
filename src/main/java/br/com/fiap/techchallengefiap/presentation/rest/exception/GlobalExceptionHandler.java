package br.com.fiap.oficina.presentation.rest.exception;

import br.com.fiap.oficina.domain.shared.DomainException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler({DomainException.class, IllegalArgumentException.class})
    ResponseEntity<ApiErrorResponse> badRequest(RuntimeException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), req);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiErrorResponse> validation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "Dados de entrada invalidos.", req);
    }

    private ResponseEntity<ApiErrorResponse> build(HttpStatus status, String msg, HttpServletRequest req) {
        return ResponseEntity.status(status).body(new ApiErrorResponse(status.value(), status.getReasonPhrase(), msg, req.getRequestURI(), OffsetDateTime.now()));
    }
}
