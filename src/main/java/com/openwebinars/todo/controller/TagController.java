package com.openwebinars.todo.controller;

import com.openwebinars.todo.dto.CreateTagDto;
import com.openwebinars.todo.dto.GetTagDto;
import com.openwebinars.todo.service.TagService;
import com.openwebinars.todo.users.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tag")
@RequiredArgsConstructor
@SecurityRequirement(name = "basicAuth")
public class TagController {

    private final TagService tagService;

    @Operation(summary = "Listar mis tags")
    @GetMapping
    public List<GetTagDto> getAll(@AuthenticationPrincipal User user) {
        return tagService.findByAuthor(user).stream()
                .map(GetTagDto::of)
                .toList();
    }

    @Operation(summary = "Obtener tag por id")
    @ApiResponse(responseCode = "404", description = "No encontrado")
    @GetMapping("/{id}")
    public GetTagDto getById(@PathVariable Long id) {
        return GetTagDto.of(tagService.findById(id));
    }

    @Operation(summary = "Crear tag")
    @ApiResponse(responseCode = "201", description = "Creado")
    @PostMapping
    public ResponseEntity<GetTagDto> create(@RequestBody CreateTagDto dto,
                                             @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(GetTagDto.of(tagService.save(dto, user)));
    }

    @Operation(summary = "Editar tag", description = "Solo el propietario puede editarlo")
    @ApiResponse(responseCode = "403", description = "No es tuyo")
    @PutMapping("/{id}")
    public GetTagDto edit(@PathVariable Long id,
                          @RequestBody CreateTagDto dto,
                          @AuthenticationPrincipal User user) {
        return GetTagDto.of(tagService.edit(id, dto, user));
    }

    @Operation(summary = "Eliminar tag")
    @ApiResponse(responseCode = "204", description = "Eliminado")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id,
                                     @AuthenticationPrincipal User user) {
        tagService.delete(id, user);
        return ResponseEntity.noContent().build();
    }
}
