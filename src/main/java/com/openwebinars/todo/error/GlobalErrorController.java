package com.openwebinars.todo.error;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;

@RestControllerAdvice
public class GlobalErrorController extends ResponseEntityExceptionHandler {

    @ExceptionHandler(TaskNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ProblemDetail handleTaskNotFound(TaskNotFoundException ex) {
        ProblemDetail result = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        result.setTitle("Tarea no encontrada");
        result.setType(URI.create("https://www.openwebinars.net/errors/task-not-found"));
        return result;
    }

    @ExceptionHandler(CategoryNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ProblemDetail handleCategoryNotFound(CategoryNotFoundException ex) {
        ProblemDetail result = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        result.setTitle("Categoria no encontrada");
        result.setType(URI.create("https://www.openwebinars.net/errors/category-not-found"));
        return result;
    }

    @ExceptionHandler(TagNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ProblemDetail handleTagNotFound(TagNotFoundException ex) {
        ProblemDetail result = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        result.setTitle("Tag no encontrado");
        result.setType(URI.create("https://www.openwebinars.net/errors/tag-not-found"));
        return result;
    }

    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ProblemDetail handleUserNotFound(UserNotFoundException ex) {
        ProblemDetail result = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        result.setTitle("Usuario no encontrado");
        result.setType(URI.create("https://www.openwebinars.net/errors/user-not-found"));
        return result;
    }

    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ProblemDetail handleAuthException(AuthenticationException ex) {
        ProblemDetail result = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, ex.getMessage());
        result.setTitle("No autenticado");
        result.setType(URI.create("https://www.openwebinars.net/errors/unauthorized"));
        return result;
    }

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ProblemDetail handleAccessDeniedException(org.springframework.security.access.AccessDeniedException ex) {
        ProblemDetail result = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, ex.getMessage());
        result.setTitle("Acceso denegado");
        result.setType(URI.create("https://www.openwebinars.net/errors/forbidden"));
        return result;
    }
}
