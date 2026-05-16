package com.openwebinars.todo.config;

import com.openwebinars.todo.model.Category;
import com.openwebinars.todo.model.Priority;
import com.openwebinars.todo.model.Tag;
import com.openwebinars.todo.model.Task;
import com.openwebinars.todo.repos.CategoryRepository;
import com.openwebinars.todo.repos.TagRepository;
import com.openwebinars.todo.repos.TaskRepository;
import com.openwebinars.todo.users.User;
import com.openwebinars.todo.users.UserRepository;
import com.openwebinars.todo.users.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final TaskRepository taskRepository;

    @Override
    public void run(ApplicationArguments args) {
        if (userRepository.findFirstByUsername("pepe").isPresent()) return;

        Category personal = categoryRepository.save(new Category(null, "Personal"));
        Category trabajo  = categoryRepository.save(new Category(null, "Trabajo"));
        Category estudio  = categoryRepository.save(new Category(null, "Estudio"));

        userRepository.save(User.builder()
                .username("admin").fullname("Administrador Principal")
                .email("admin@todo.com").password("{noop}admin123")
                .role(UserRole.ADMIN).build());

        userRepository.save(User.builder()
                .username("gestor").fullname("Gestor Uno")
                .email("gestor@todo.com").password("{noop}gestor123")
                .role(UserRole.GESTOR).build());

        User pepe = userRepository.save(User.builder()
                .username("pepe").fullname("Pepe Garcia")
                .email("pepe@openwebinars.net").password("{noop}12345")
                .role(UserRole.USER).build());

        Tag urgente    = tagRepository.save(Tag.builder().name("urgente").author(pepe).build());
        Tag importante = tagRepository.save(Tag.builder().name("importante").author(pepe).build());
        tagRepository.save(Tag.builder().name("revision").author(pepe).build());

        taskRepository.save(Task.builder()
                .title("Comprar comida").description("Hacer la compra del supermercado")
                .deadline(LocalDateTime.of(2025, 12, 31, 23, 59, 59))
                .priority(Priority.MEDIA).author(pepe).category(personal).build());

        taskRepository.save(Task.builder()
                .title("Estudiar Spring Boot").description("Terminar el proyecto final del modulo")
                .deadline(LocalDateTime.of(2025, 6, 20, 23, 59, 59))
                .priority(Priority.ALTA).importante(true).author(pepe).category(estudio).build());

        taskRepository.save(Task.builder()
                .title("Pagar facturas").description("Pagar la luz y el internet este mes")
                .deadline(LocalDateTime.of(2025, 5, 10, 23, 59, 59))
                .completed(true).priority(Priority.ALTA).author(pepe).category(personal).build());

        taskRepository.save(Task.builder()
                .title("Preparar presentacion").description("Presentacion del proyecto final para clase")
                .deadline(LocalDateTime.of(2025, 7, 1, 23, 59, 59))
                .priority(Priority.MEDIA).importante(true).author(pepe).category(estudio).build());

        taskRepository.save(Task.builder()
                .title("Actualizar CV").description("Añadir las practicas y el modulo de DAW")
                .deadline(LocalDateTime.of(2025, 12, 31, 23, 59, 59))
                .priority(Priority.BAJA).author(pepe).category(trabajo).build());
    }
}
