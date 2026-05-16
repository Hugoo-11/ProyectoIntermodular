package com.openwebinars.todo.users;

public record EditUserCommand(
        String username,
        String fullname,
        String email
) {
}
