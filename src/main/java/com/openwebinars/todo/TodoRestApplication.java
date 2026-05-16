package com.openwebinars.todo;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@OpenAPIDefinition(info =
@Info(
        title = "TodoList API REST",
        description = "API para gestionar tareas personales con categorias, tags y control de acceso por roles",
        version = "1.0",
        contact = @Contact(name = "Hugo Iglesias")
)
)
@SpringBootApplication
public class TodoRestApplication {

	public static void main(String[] args) {
		SpringApplication.run(TodoRestApplication.class, args);
	}

}
