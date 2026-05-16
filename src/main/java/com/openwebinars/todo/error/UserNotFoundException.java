package com.openwebinars.todo.error;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(Long id) {
        super("No se encontro el usuario con id: " + id);
    }
}
