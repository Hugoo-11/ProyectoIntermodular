package com.openwebinars.todo.users;

public record ChangePasswordCommand(
        String oldPassword,
        String newPassword
) {
}
