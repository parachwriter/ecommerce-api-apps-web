package ec.epn.ecommerce.mapper;

import ec.epn.ecommerce.dto.product.ProductRequestDTO;
import ec.epn.ecommerce.dto.product.ProductResponseDTO;
import ec.epn.ecommerce.entity.Product;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ProductMapper {

    public Product toEntity(ProductRequestDTO dto) {
        if (dto == null) return null;
        Product product = new Product();
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setStock(dto.getStock());
        return product;
    }

    public void updateEntity(ProductRequestDTO dto, Product product) {
        if (dto == null || product == null) return;
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setStock(dto.getStock());
    }

    public ProductResponseDTO toResponseDTO(Product entity) {
        if (entity == null) return null;
        ProductResponseDTO dto = new ProductResponseDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setPrice(entity.getPrice());
        dto.setStock(entity.getStock());
        return dto;
    }
}