package com.openwebinars.todo.dto;

public record DashboardDto(
        long totalTareas,
        long tareasCompletadas,
        long tareasPendientes,
        long tareasVencidas,
        long tareasImportantes
) {
}
