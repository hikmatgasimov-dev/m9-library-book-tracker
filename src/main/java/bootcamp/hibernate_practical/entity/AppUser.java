package bootcamp.hibernate_practical.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "app_user")
@Getter
@Setter
@NoArgsConstructor
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false,unique = true)
    private String username;

    // BCrypt hash, never the plain password
    @Column(nullable = false)
    private  String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;


    private boolean enabled=true;

    public  AppUser(String username,String password,Role role){
      this.username=username;
      this.password=password;
      this.role=role;
    }


}
