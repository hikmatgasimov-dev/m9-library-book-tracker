package bootcamp.hibernate_practical.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
         http
                 .csrf(csrf->csrf.ignoringRequestMatchers("/api/**","/h2-console/**"))
                 .authorizeHttpRequests(auth->
                         auth.requestMatchers(HttpMethod.GET,"/api/**").authenticated()
                                 .requestMatchers("/api/**").hasRole("ADMIN")
                                 .anyRequest().authenticated())
                 .httpBasic(Customizer.withDefaults())
                 .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));

      return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return  new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder){
     UserDetails user= User.withUsername("user")
             .password(passwordEncoder.encode("user"))
                     .roles("USER")
                     .build();
        UserDetails admin= User.withUsername("admin")
                .password(passwordEncoder.encode("admin"))
                        .roles("ADMIN")
                        .build();


        return  new InMemoryUserDetailsManager(user,admin);
    }


}
