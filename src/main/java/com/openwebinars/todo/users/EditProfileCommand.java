package com.openwebinars.todo.users;

// DTO para que el usuario actualice su propio perfil sin tocar la contraseña
public record EditProfileCommand(
        String username,
        String fullname,
        String email
) {
}
