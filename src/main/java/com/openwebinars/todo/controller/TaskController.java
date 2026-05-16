package com.openwebinars.todo.controller;

import com.openwebinars.todo.dto.AddTagsCommand;
import com.openwebinars.todo.dto.EditTaskCommand;
import com.openwebinars.todo.dto.GetTaskDto;
import com.openwebinars.todo.model.Priority;
import com.openwebinars.todo.service.TaskService;
import com.openwebinars.todo.users.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/task")
@RequiredArgsConstructor
@SecurityRequirement(name = "basicAuth")
public class TaskController {

    private final TaskService taskService;

    @Operation(summary = "Listar mis tareas")
    @GetMapping
    public List<GetTaskDto> getAll(@AuthenticationPrincipal User author) {
        return taskService.findByAuthor(author).stream()
                .map(GetTaskDto::of)
                .toList();
    }

    // busqueda con varios filtros opcionales
    // ej: /task/search?title=comprar&completed=false&priority=ALTA
    @Operation(summary = "Buscar tareas", description = "Filtra por cualquier combinacion de: title, completed, category (id), priority (BAJA/MEDIA/ALTA), importante")
    @GetMapping("/search")
    public List<GetTaskDto> search(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Boolean completed,
            @RequestParam(required = false) Long category,
            @RequestParam(required = false) Priority priority,
            @RequestParam(required = false) Boolean importante,
            @AuthenticationPrincipal User user) {
        return taskService.search(user, title, completed, category, priority, importante)
                .stream()
                .map(GetTaskDto::of)
                .toList();
    }

    @Operation(summary = "Buscar tareas por tag", description = "Devuelve las tareas que tengan el tag indicado: /task/by-tag?tag=1")
    @GetMapping("/by-tag")
    public List<GetTaskDto> getByTag(@RequestParam Long tag,
                                      @AuthenticationPrincipal User user) {
        return taskService.findByTags(user, List.of(tag))
                .stream()
                .map(GetTaskDto::of)
                .toList();
    }

    @Operation(summary = "Obtener tarea por id")
    @ApiResponse(responseCode = "403", description = "No es tu tarea")
    @ApiResponse(responseCode = "404", description = "No encontrada")
    @PostAuthorize("returnObject.author().username() == authentication.principal.username")
    @GetMapping("/{id}")
    public GetTaskDto getById(@PathVariable Long id) {
        return GetTaskDto.of(taskService.findById(id));
    }

    @Operation(summary = "Crear tarea")
    @ApiResponse(responseCode = "201", description = "Creada")
    @PostMapping
    public ResponseEntity<GetTaskDto> create(@RequestBody EditTaskCommand cmd,
                                              @AuthenticationPrincipal User author) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(GetTaskDto.of(taskService.save(cmd, author)));
    }

    @Operation(summary = "Editar tarea")
    @ApiResponse(responseCode = "403", description = "No es tu tarea")
    @PreAuthorize("@ownerCheck.check(#id, authentication.principal.getId())")
    @PutMapping("/{id}")
    public GetTaskDto edit(@RequestBody EditTaskCommand cmd, @PathVariable Long id) {
        return GetTaskDto.of(taskService.edit(cmd, id));
    }

    @Operation(summary = "Eliminar tarea")
    @ApiResponse(responseCode = "204", description = "Eliminada")
    @PreAuthorize("@ownerCheck.check(#id, authentication.principal.getId())")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        taskService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // asignar varios tags de una vez con body {"tagIds": [1, 2]}
    @Operation(summary = "Asignar tags a una tarea", description = "Body: {\"tagIds\": [1, 2, 3]}")
    @ApiResponse(responseCode = "200", description = "Tags asignados")
    @PreAuthorize("@ownerCheck.check(#id, authentication.principal.getId())")
    @PostMapping("/{id}/tags")
    public GetTaskDto addTags(@PathVariable Long id,
                               @RequestBody AddTagsCommand cmd,
                               @AuthenticationPrincipal User user) {
        return GetTaskDto.of(taskService.addTags(id, cmd.tagIds(), user));
    }

    // quitar un tag concreto de una tarea
    @Operation(summary = "Quitar tag de una tarea")
    @PreAuthorize("@ownerCheck.check(#id, authentication.principal.getId())")
    @DeleteMapping("/{id}/tags/{tagId}")
    public GetTaskDto removeTag(@PathVariable Long id,
                                 @PathVariable Long tagId,
                                 @AuthenticationPrincipal User user) {
        return GetTaskDto.of(taskService.removeTag(id, tagId, user));
    }
}
