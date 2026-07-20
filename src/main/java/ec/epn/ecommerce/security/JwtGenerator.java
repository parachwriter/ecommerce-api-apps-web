package ec.epn.ecommerce.security;

import ec.epn.ecommerce.entity.User;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.HashSet;
import java.util.Arrays;

@ApplicationScoped
public class JwtGenerator {

    public String generateToken(User user) {
        return Jwt.issuer("https://epn.edu.ec/ecommerce")
                .upn(user.getEmail())
                .subject(user.getId().toString())
                .groups(new HashSet<>(Arrays.asList("USER")))
                .claim("name", user.getName())
                .expiresIn(7200) // 2 horas de validez (7200 segundos)
                .sign();
    }
}