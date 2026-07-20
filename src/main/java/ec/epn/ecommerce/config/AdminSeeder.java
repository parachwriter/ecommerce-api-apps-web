package ec.epn.ecommerce.config;

import ec.epn.ecommerce.entity.User;
import ec.epn.ecommerce.repository.UserRepository;
import ec.epn.ecommerce.security.PasswordEncoder;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

/**
 * Se ejecuta una sola vez al arrancar la aplicación (evento StartupEvent de
 * Quarkus).
 * Si no existe ningún usuario con el correo configurado como administrador, lo
 * crea.
 * Así nunca dependes de que alguien se autoasigne el rol ADMIN desde
 * /api/users/register.
 */
@ApplicationScoped
public class AdminSeeder {

    private static final Logger LOG = Logger.getLogger(AdminSeeder.class);

    @Inject
    UserRepository userRepository;

    @Inject
    PasswordEncoder passwordEncoder;

    // Estos valores se leen de application.properties (o variables de entorno,
    // ej. APP_ADMIN_EMAIL / APP_ADMIN_PASSWORD). Nunca hardcodees la contraseña
    // final aquí; usa el default solo como fallback de desarrollo.
    @ConfigProperty(name = "app.admin.email", defaultValue = "admin@epn.edu.ec")
    String adminEmail;

    @ConfigProperty(name = "app.admin.password", defaultValue = "ChangeMe123!")
    String adminPassword;

    @Transactional
    void onStart(@Observes StartupEvent ev) {
        if (userRepository.findByEmail(adminEmail).isPresent()) {
            LOG.info("El usuario administrador ya existe, no se crea uno nuevo.");
            return;
        }

        User admin = new User();
        admin.setName("Administrador");
        admin.setEmail(adminEmail);
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.setRole("ADMIN");

        userRepository.persist(admin);
        LOG.infof("Usuario administrador creado con correo: %s", adminEmail);
    }
}
