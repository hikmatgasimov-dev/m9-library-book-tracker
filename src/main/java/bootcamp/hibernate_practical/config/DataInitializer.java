package bootcamp.hibernate_practical.config;

import bootcamp.hibernate_practical.entity.AppUser;
import bootcamp.hibernate_practical.entity.Role;
import bootcamp.hibernate_practical.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {
    // Demo users, created at startup with BCrypt-hashed passwords (the database is in memory).

    @Bean
    public CommandLineRunner createDemoUsers(UserRepository userRepository, PasswordEncoder passwordEncoder){
       return args -> {
           createIfMissing(userRepository, passwordEncoder, "user", "user", Role.USER);
           createIfMissing(userRepository, passwordEncoder, "admin", "admin", Role.ADMIN);
       };
    }
    private void createIfMissing(UserRepository userRepository, PasswordEncoder passwordEncoder,
                                 String username, String rawPassword, Role role) {
        if (userRepository.findByUsername(username).isEmpty()) {
            userRepository.save(new AppUser(username, passwordEncoder.encode(rawPassword), role));
        }
    }
}
