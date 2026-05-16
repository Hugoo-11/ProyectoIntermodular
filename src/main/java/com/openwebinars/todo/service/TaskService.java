package com.openwebinars.todo.service;

import com.openwebinars.todo.dto.DashboardDto;
import com.openwebinars.todo.dto.EditTaskCommand;
import com.openwebinars.todo.error.CategoryNotFoundException;
import com.openwebinars.todo.error.TagNotFoundException;
import com.openwebinars.todo.error.TaskNotFoundException;
import com.openwebinars.todo.model.Category;
import com.openwebinars.todo.model.Priority;
import com.openwebinars.todo.model.Tag;
import com.openwebinars.todo.model.Task;
import com.openwebinars.todo.repos.CategoryRepository;
import com.openwebinars.todo.repos.TagRepository;
import com.openwebinars.todo.repos.TaskRepository;
import com.openwebinars.todo.users.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;

    public List<Task> findAll() {
        return taskRepository.findAll();
    }

    public List<Task> findByAuthor(User author) {
        return taskRepository.findByAuthor(author);
    }

    public Task findById(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));
    }

    public Task save(EditTaskCommand cmd, User author) {
        Category cat = null;
        if (cmd.categoryId() != null) {
            cat = categoryRepository.findById(cmd.categoryId())
                    .orElseThrow(() -> new CategoryNotFoundException(cmd.categoryId()));
        }
        return taskRepository.save(
                Task.builder()
                        .title(cmd.title())
                        .description(cmd.description())
                        .deadline(cmd.deadline())
                        .completed(cmd.completed())
                        .priority(cmd.priority() != null ? cmd.priority() : Priority.MEDIA)
                        .importante(cmd.importante())
                        .author(author)
                        .category(cat)
                        .build()
        );
    }

    public Task edit(EditTaskCommand cmd, Long id) {
        return taskRepository.findById(id)
                .map(t -> {
                    t.setTitle(cmd.title());
                    t.setDescription(cmd.description());
                    t.setDeadline(cmd.deadline());
                    t.setCompleted(cmd.completed());
                    t.setImportante(cmd.importante());
                    if (cmd.priority() != null)
                        t.setPriority(cmd.priority());
                    if (cmd.categoryId() != null) {
                        Category cat = categoryRepository.findById(cmd.categoryId())
                                .orElseThrow(() -> new CategoryNotFoundException(cmd.categoryId()));
                        t.setCategory(cat);
                    } else {
                        t.setCategory(null);
                    }
                    return taskRepository.save(t);
                })
                .orElseThrow(() -> new TaskNotFoundException(id));
    }

    public void delete(Long id) {
        taskRepository.deleteById(id);
    }

    public List<Task> search(User author, String title, Boolean completed,
                              Long categoryId, Priority priority, Boolean importante) {
        return taskRepository.search(author, title, completed, categoryId, priority, importante);
    }

    public Task addTags(Long taskId, List<Long> tagIds, User user) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));

        for (Long tagId : tagIds) {
            Tag tag = tagRepository.findById(tagId)
                    .orElseThrow(() -> new TagNotFoundException(tagId));
            if (!tag.getAuthor().getId().equals(user.getId()))
                throw new AccessDeniedException("El tag " + tagId + " no te pertenece");
            boolean yaEsta = task.getTags().stream().anyMatch(t -> t.getId().equals(tagId));
            if (!yaEsta)
                task.getTags().add(tag);
        }
        return taskRepository.save(task);
    }

    public Task removeTag(Long taskId, Long tagId, User user) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));
        tagRepository.findById(tagId)
                .orElseThrow(() -> new TagNotFoundException(tagId));

        task.getTags().removeIf(t -> t.getId().equals(tagId));
        return taskRepository.save(task);
    }

    public List<Task> findByTags(User author, List<Long> tagIds) {
        return taskRepository.findByAuthorAndTagsIn(author, tagIds);
    }

    public DashboardDto getDashboard(User author) {
        List<Task> tareas = taskRepository.findByAuthor(author);
        long total = tareas.size();
        long completadas = tareas.stream().filter(Task::isCompleted).count();
        long pendientes = total - completadas;
        long vencidas = tareas.stream()
                .filter(t -> t.getDeadline() != null
                        && t.getDeadline().isBefore(LocalDateTime.now())
                        && !t.isCompleted())
                .count();
        long importantes = tareas.stream().filter(Task::isImportante).count();

        return new DashboardDto(total, completadas, pendientes, vencidas, importantes);
    }
}
