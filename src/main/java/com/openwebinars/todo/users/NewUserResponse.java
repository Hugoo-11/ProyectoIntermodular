package com.openwebinars.todo.users;

// DTO de respuesta para usuarios: no expone la contraseña ni datos internos de JPA
public record NewUserResponse(
        Long id,
        String username,
        String fullname,
        String email,
        UserRole role
) {
    public static NewUserResponse of(User user) {
        return new NewUserResponse(
                user.getId(),
                user.getUsername(),
                user.getFullname(),
                user.getEmail(),
                user.getRole()
        );
    }
}
