package com.openwebinars.todo.users;

import com.openwebinars.todo.model.Task;
import com.openwebinars.todo.repos.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OwnerCheck {

    private final TaskRepository taskRepository;

    public boolean check(Task task, Long userId) {
        if (task != null && task.getAuthor() != null)
            return task.getAuthor().getId().equals(userId);
        return false;
    }

    public boolean check(Long taskId, Long userId) {
        return taskRepository.findById(taskId)
                .map(t -> t.getAuthor().getId().equals(userId))
                .orElse(false);
    }

}
