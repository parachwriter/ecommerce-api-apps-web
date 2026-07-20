package ec.epn.ecommerce.dto.receipt;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class ReceiptCreateDTO {

    @NotNull(message = "El ID del usuario es obligatorio")
    private Long userId;

    @NotEmpty(message = "La nota de venta debe contener al menos un producto")
    @Valid // Permite validar las anotaciones de cada item de la lista
    private List<ReceiptItemDTO> items;

    // Getters y Setters
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public List<ReceiptItemDTO> getItems() { return items; }
    public void setItems(List<ReceiptItemDTO> items) { this.items = items; }
}