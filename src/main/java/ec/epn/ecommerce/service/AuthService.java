package ec.epn.ecommerce.service;

import ec.epn.ecommerce.dto.auth.LoginDTO;
import ec.epn.ecommerce.dto.auth.TokenDTO;
import ec.epn.ecommerce.entity.User;
import ec.epn.ecommerce.exception.BadRequestException;
import ec.epn.ecommerce.repository.UserRepository;
import ec.epn.ecommerce.security.JwtGenerator;
import ec.epn.ecommerce.security.PasswordEncoder;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.Optional;

@ApplicationScoped
public class AuthService {

    @Inject
    UserRepository userRepository;

    @Inject
    PasswordEncoder passwordEncoder;

    @Inject
    JwtGenerator jwtGenerator;

    public TokenDTO login(LoginDTO dto) {
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new BadRequestException("Credenciales incorrectas (Correo no encontrado)."));

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BadRequestException("Credenciales incorrectas (Contraseña inválida).");
        }

        String token = jwtGenerator.generateToken(user);
        return new TokenDTO(token);
    }
}