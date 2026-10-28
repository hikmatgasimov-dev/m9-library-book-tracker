package bootcamp.hibernate_practical.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
        // CSRF stays enabled for browser forms; REST clients (Basic Auth) and the H2 console are exempt
        http
                 .csrf(csrf->csrf.ignoringRequestMatchers("/api/**","/h2-console/**"))
                 .authorizeHttpRequests(auth->auth
                         .requestMatchers("/login", "/register", "/css/**").permitAll()
                         //REST API
                         .requestMatchers(HttpMethod.GET,"/api/**").authenticated()
                         .requestMatchers("/api/**").hasRole("ADMIN")

                         // Browser UI: reading is for any user, every form submit
                         .requestMatchers(HttpMethod.POST,"/books/**").hasRole("ADMIN")
                         .anyRequest().authenticated())
                .formLogin(form->form.loginPage("/login")
                        .defaultSuccessUrl("/books",true))
                .logout(Customizer.withDefaults())
                .httpBasic(Customizer.withDefaults())
                 .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));

      return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return  new BCryptPasswordEncoder();
    }




}
