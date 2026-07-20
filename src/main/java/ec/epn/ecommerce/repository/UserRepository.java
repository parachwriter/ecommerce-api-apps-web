package ec.epn.ecommerce.repository;

import ec.epn.ecommerce.entity.User;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Optional;

@ApplicationScoped
public class UserRepository implements PanacheRepository<User> {
    
    // Método personalizado indispensable para la autenticación JWT posterior
    public Optional<User> findByEmail(String email) {
        return find("email", email).firstResultOptional();
    }
}