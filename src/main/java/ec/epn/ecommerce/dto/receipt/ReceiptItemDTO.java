package ec.epn.ecommerce.dto.receipt;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class ReceiptItemDTO {

    @NotNull(message = "El ID del producto es obligatorio")
    private Long productId;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad mínima de compra es 1")
    private Integer quantity;

    // Getters y Setters
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}