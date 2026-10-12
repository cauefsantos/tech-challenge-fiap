package br.com.fiap.techchallengefiap.presentation.rest.exception;

import br.com.fiap.techchallengefiap.domain.shared.DomainException;
import br.com.fiap.techchallengefiap.domain.shared.ConflictException;
import br.com.fiap.techchallengefiap.domain.shared.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler({DomainException.class, IllegalArgumentException.class})
    ResponseEntity<ApiErrorResponse> badRequest(RuntimeException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), req);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    ResponseEntity<ApiErrorResponse> notFound(ResourceNotFoundException ex, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), req);
    }

    @ExceptionHandler({ConflictException.class, DataIntegrityViolationException.class})
    ResponseEntity<ApiErrorResponse> conflict(RuntimeException ex, HttpServletRequest req) {
        String message = ex instanceof ConflictException
                ? ex.getMessage()
                : "A operacao viola uma restricao de integridade dos dados.";
        return build(HttpStatus.CONFLICT, message, req);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiErrorResponse> validation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "Dados de entrada invalidos.", req);
    }

    private ResponseEntity<ApiErrorResponse> build(HttpStatus status, String msg, HttpServletRequest req) {
        return ResponseEntity.status(status).body(new ApiErrorResponse(status.value(), status.getReasonPhrase(), msg, req.getRequestURI(), OffsetDateTime.now()));
    }
}
