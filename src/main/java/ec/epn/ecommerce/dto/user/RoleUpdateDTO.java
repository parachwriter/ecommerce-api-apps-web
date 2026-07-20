package ec.epn.ecommerce.dto.user;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class RoleUpdateDTO {
    
 
    @NotBlank(message = "El rol es obligatorio")
    @Pattern(regexp = "USER|ADMIN", message = "El rol debe ser USER o ADMIN")
    private String role;
 
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

}
