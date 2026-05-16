package com.openwebinars.todo.error;

public class TagNotFoundException extends RuntimeException {

    public TagNotFoundException(Long id) {
        super("No se encontro el tag con id: " + id);
    }
}
