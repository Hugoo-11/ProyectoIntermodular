package com.openwebinars.todo.users;

import com.openwebinars.todo.error.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User register(NewUserCommand cmd) {
        User user = User.builder()
                .username(cmd.username())
                .fullname(cmd.fullname())
                .email(cmd.email())
                .password(passwordEncoder.encode(cmd.password()))
                .role(UserRole.USER)
                .build();
        return userRepository.save(user);
    }

    // actualizar datos del perfil propio (sin contraseña)
    public User updateProfile(User user, EditProfileCommand cmd) {
        user.setUsername(cmd.username());
        user.setFullname(cmd.fullname());
        user.setEmail(cmd.email());
        return userRepository.save(user);
    }

    // el propio usuario cambia su contraseña
    public void changePassword(User user, ChangePasswordCommand cmd) {
        if (!passwordEncoder.matches(cmd.oldPassword(), user.getPassword()))
            throw new AccessDeniedException("La contraseña actual no es correcta");
        user.setPassword(passwordEncoder.encode(cmd.newPassword()));
        userRepository.save(user);
    }

    // -- operaciones de admin --

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    public User editUser(Long id, EditUserCommand cmd) {
        User u = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        u.setUsername(cmd.username());
        u.setFullname(cmd.fullname());
        u.setEmail(cmd.email());
        return userRepository.save(u);
    }

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id))
            throw new UserNotFoundException(id);
        userRepository.deleteById(id);
    }

    public User promoteToGestor(Long id) {
        User u = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        u.setRole(UserRole.GESTOR);
        return userRepository.save(u);
    }

    public User demoteToUser(Long id) {
        User u = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        u.setRole(UserRole.USER);
        return userRepository.save(u);
    }
}
