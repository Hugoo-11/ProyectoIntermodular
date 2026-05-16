package com.openwebinars.todo.error;

public class CategoryNotFoundException extends RuntimeException {

    public CategoryNotFoundException(Long id) {
        super("No se encontro la categoria con id: " + id);
    }
}
