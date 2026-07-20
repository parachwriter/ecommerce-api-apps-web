package ec.epn.ecommerce.service;

import ec.epn.ecommerce.dto.auth.RegisterDTO;
import ec.epn.ecommerce.dto.user.UserRequestDTO;
import ec.epn.ecommerce.dto.user.UserResponseDTO;
import ec.epn.ecommerce.entity.User;
import ec.epn.ecommerce.exception.BadRequestException;
import ec.epn.ecommerce.exception.ResourceNotFoundException;
import ec.epn.ecommerce.mapper.UserMapper;
import ec.epn.ecommerce.repository.UserRepository;
import ec.epn.ecommerce.security.PasswordEncoder;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class UserService {

    @Inject
    UserRepository userRepository;

    @Inject
    UserMapper userMapper;

    @Inject
    PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponseDTO registerUser(RegisterDTO dto) {
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new BadRequestException("El correo electrónico ya se encuentra registrado.");
        }
        User user = userMapper.toEntity(dto);
        user.setPassword(passwordEncoder.encode(dto.getPassword())); // Regla: Cifrar contraseña
        userRepository.persist(user);
        return userMapper.toResponseDTO(user); // Regla: No devolver password
    }

    public List<UserResponseDTO> getAllUsers() {
        return userRepository.listAll().stream()
                .map(userMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    public UserResponseDTO getUserById(Long id) {
        User user = userRepository.findByIdOptional(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));
        return userMapper.toResponseDTO(user);
    }

@Transactional
    public UserResponseDTO updateUser(Long id, UserRequestDTO dto) {
        User user = userRepository.findByIdOptional(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));

        userRepository.findByEmail(dto.getEmail())
                .ifPresent(existingUser -> {
                    if (!existingUser.getId().equals(id)) {
                        throw new BadRequestException("El correo ya está en uso por otro usuario.");
                    }
                });

        // 1. Respaldamos la contraseña cifrada actual
        String currentPassword = user.getPassword();

        // 2. MapStruct actualiza nombre y correo
        userMapper.updateEntity(dto, user);
        
        // 3. Restauramos la contraseña para que nunca se vuelva null
        user.setPassword(currentPassword);

        return userMapper.toResponseDTO(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.deleteById(id)) {
            throw new ResourceNotFoundException("No se pudo eliminar. Usuario no encontrado con ID: " + id);
        }
    }
}