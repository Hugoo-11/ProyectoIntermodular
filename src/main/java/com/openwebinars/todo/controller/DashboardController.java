package com.openwebinars.todo.controller;

import com.openwebinars.todo.dto.DashboardDto;
import com.openwebinars.todo.service.TaskService;
import com.openwebinars.todo.users.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
@SecurityRequirement(name = "basicAuth")
public class DashboardController {

    private final TaskService taskService;

    @Operation(summary = "Dashboard", description = "Resumen de tareas: totales, completadas, pendientes, vencidas e importantes")
    @GetMapping
    public DashboardDto getDashboard(@AuthenticationPrincipal User user) {
        return taskService.getDashboard(user);
    }
}
