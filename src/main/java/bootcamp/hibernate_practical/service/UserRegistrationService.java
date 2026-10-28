package bootcamp.hibernate_practical.service;

import bootcamp.hibernate_practical.entity.AppUser;
import bootcamp.hibernate_practical.entity.Role;
import bootcamp.hibernate_practical.exception.UsernameTakenException;
import bootcamp.hibernate_practical.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserRegistrationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserRegistrationService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Self-registration can only ever create a USER; ADMIN accounts are created by the application itself.
    public void register(String username, String rawPassword) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new UsernameTakenException(username);
        }
        try {
            userRepository.save(new AppUser(username, passwordEncoder.encode(rawPassword), Role.USER));
        } catch (DataIntegrityViolationException e) {
            // two people registering the same name at the same moment: the unique column wins
            throw new UsernameTakenException(username);
        }
    }
}