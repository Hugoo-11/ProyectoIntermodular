package com.openwebinars.todo.users;

public record NewUserCommand(
        String username,
        String fullname,
        String email,
        String password
) {
}
