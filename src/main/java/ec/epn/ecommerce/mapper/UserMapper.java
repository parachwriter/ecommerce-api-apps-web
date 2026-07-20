package ec.epn.ecommerce.mapper;

import ec.epn.ecommerce.dto.auth.RegisterDTO;
import ec.epn.ecommerce.dto.user.UserRequestDTO;
import ec.epn.ecommerce.dto.user.UserResponseDTO;
import ec.epn.ecommerce.entity.User;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class UserMapper {

    public User toEntity(RegisterDTO dto) {
        if (dto == null) return null;
        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPassword(dto.getPassword()); // Se cifrará en el servicio
        return user;
    }

    public void updateEntity(UserRequestDTO dto, User user) {
        if (dto == null || user == null) return;
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
    }

    public UserResponseDTO toResponseDTO(User entity) {
        if (entity == null) return null;
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setEmail(entity.getEmail());
        return dto;
    }
}