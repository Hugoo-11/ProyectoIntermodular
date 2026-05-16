package com.openwebinars.todo.users;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // registro publico, no hace falta autenticacion
    @Operation(summary = "Registrar usuario", description = "Endpoint publico, no hace falta autenticacion")
    @ApiResponse(responseCode = "201", description = "Usuario creado")
    @PostMapping("/auth/register")
    public ResponseEntity<NewUserResponse> createUser(@RequestBody NewUserCommand cmd) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(NewUserResponse.of(userService.register(cmd)));
    }

    // el usuario actualiza su propio perfil (username, nombre, email)
    @Operation(summary = "Actualizar perfil", description = "El usuario actualiza sus propios datos")
    @SecurityRequirement(name = "basicAuth")
    @PutMapping("/user/profile")
    public NewUserResponse updateProfile(@RequestBody EditProfileCommand cmd,
                                          @AuthenticationPrincipal User user) {
        return NewUserResponse.of(userService.updateProfile(user, cmd));
    }

    @Operation(summary = "Cambiar contraseña")
    @ApiResponse(responseCode = "403", description = "La contraseña actual no coincide")
    @SecurityRequirement(name = "basicAuth")
    @PutMapping("/user/password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordCommand cmd,
                                             @AuthenticationPrincipal User user) {
        userService.changePassword(user, cmd);
        return ResponseEntity.ok("Contraseña actualizada");
    }

    // -- admin --

    @Operation(summary = "Listar usuarios", description = "Solo ADMIN")
    @SecurityRequirement(name = "basicAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/users")
    public List<NewUserResponse> getAllUsers() {
        return userService.findAll().stream()
                .map(NewUserResponse::of)
                .toList();
    }

    @Operation(summary = "Obtener usuario por id", description = "Solo ADMIN")
    @ApiResponse(responseCode = "404", description = "No encontrado")
    @SecurityRequirement(name = "basicAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/users/{id}")
    public NewUserResponse getUserById(@PathVariable Long id) {
        return NewUserResponse.of(userService.findById(id));
    }

    @Operation(summary = "Editar usuario", description = "Solo ADMIN")
    @SecurityRequirement(name = "basicAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/admin/users/{id}")
    public NewUserResponse editUser(@PathVariable Long id, @RequestBody EditUserCommand cmd) {
        return NewUserResponse.of(userService.editUser(id, cmd));
    }

    @Operation(summary = "Eliminar usuario", description = "Solo ADMIN")
    @ApiResponse(responseCode = "204", description = "Eliminado")
    @SecurityRequirement(name = "basicAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/admin/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Promover a GESTOR", description = "El admin sube a un usuario a GESTOR")
    @SecurityRequirement(name = "basicAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/users/{id}/promote")
    public NewUserResponse promote(@PathVariable Long id) {
        return NewUserResponse.of(userService.promoteToGestor(id));
    }

    @Operation(summary = "Degradar a USER", description = "El admin baja a un GESTOR a USER")
    @SecurityRequirement(name = "basicAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/users/{id}/demote")
    public NewUserResponse demote(@PathVariable Long id) {
        return NewUserResponse.of(userService.demoteToUser(id));
    }
}
