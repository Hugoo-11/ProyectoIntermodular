package com.openwebinars.todo.controller;

import com.openwebinars.todo.dto.CreateCategoryDto;
import com.openwebinars.todo.dto.GetCategoryDto;
import com.openwebinars.todo.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// CRUD de categorias para gestores (y tambien admin)
@RestController
@RequestMapping("/manager/categories")
@RequiredArgsConstructor
@SecurityRequirement(name = "basicAuth")
@PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
public class ManagerCategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "Listar categorias (gestor)")
    @GetMapping
    public List<GetCategoryDto> getAll() {
        return categoryService.findAll().stream()
                .map(GetCategoryDto::of)
                .toList();
    }

    @Operation(summary = "Obtener categoria por id (gestor)")
    @ApiResponse(responseCode = "404", description = "No encontrada")
    @GetMapping("/{id}")
    public GetCategoryDto getById(@PathVariable Long id) {
        return GetCategoryDto.of(categoryService.findById(id));
    }

    @Operation(summary = "Crear categoria (gestor)")
    @ApiResponse(responseCode = "201", description = "Creada")
    @PostMapping
    public ResponseEntity<GetCategoryDto> create(@RequestBody CreateCategoryDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(GetCategoryDto.of(categoryService.save(dto)));
    }

    @Operation(summary = "Editar categoria (gestor)")
    @PutMapping("/{id}")
    public GetCategoryDto edit(@PathVariable Long id, @RequestBody CreateCategoryDto dto) {
        return GetCategoryDto.of(categoryService.edit(id, dto));
    }

    @Operation(summary = "Eliminar categoria (gestor)")
    @ApiResponse(responseCode = "204", description = "Eliminada")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
