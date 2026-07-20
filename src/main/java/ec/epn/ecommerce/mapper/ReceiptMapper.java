package ec.epn.ecommerce.mapper;

import ec.epn.ecommerce.dto.receipt.ReceiptResponseDTO;
import ec.epn.ecommerce.entity.Receipt;
import ec.epn.ecommerce.entity.ReceiptItem;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.stream.Collectors;

@ApplicationScoped
public class ReceiptMapper {

    public ReceiptResponseDTO toResponseDTO(Receipt entity) {
        if (entity == null) return null;

        ReceiptResponseDTO dto = new ReceiptResponseDTO();
        dto.setId(entity.getId());
        dto.setUserId(entity.getUser().getId());
        dto.setUserName(entity.getUser().getName());
        dto.setTotal(entity.getTotal());
        dto.setDate(entity.getDate());
        
        dto.setItems(entity.getItems().stream()
                .map(this::toItemResponse)
                .collect(Collectors.toList()));

        return dto;
    }

    private ReceiptResponseDTO.ItemResponse toItemResponse(ReceiptItem item) {
        ReceiptResponseDTO.ItemResponse itemDto = new ReceiptResponseDTO.ItemResponse();
        itemDto.setProductId(item.getProduct().getId());
        itemDto.setProductName(item.getProduct().getName());
        itemDto.setQuantity(item.getQuantity());
        itemDto.setUnitPrice(item.getUnitPrice());
        itemDto.setSubtotal(item.getSubtotal());
        return itemDto;
    }
}