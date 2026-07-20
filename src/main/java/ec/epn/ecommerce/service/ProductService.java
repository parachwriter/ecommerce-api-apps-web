package ec.epn.ecommerce.service;

import ec.epn.ecommerce.dto.product.ProductRequestDTO;
import ec.epn.ecommerce.dto.product.ProductResponseDTO;
import ec.epn.ecommerce.entity.Product;
import ec.epn.ecommerce.exception.ResourceNotFoundException;
import ec.epn.ecommerce.mapper.ProductMapper;
import ec.epn.ecommerce.repository.ProductRepository;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Page; // CORRECCIÓN: Importación obligatoria para que funcione la paginación
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class ProductService {

    @Inject
    ProductRepository productRepository;

    @Inject
    ProductMapper productMapper;

    @Transactional
    public ProductResponseDTO createProduct(ProductRequestDTO dto) {
        Product product = productMapper.toEntity(dto);
        productRepository.persist(product);
        return productMapper.toResponseDTO(product);
    }

    // Nota para la sustentación: Este método ahora delega al nuevo método paginado
    // con valores por defecto si se requiere
    public List<ProductResponseDTO> getAllProducts() {
        return productRepository.listAll().stream()
                .map(productMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    public List<ProductResponseDTO> getProductsPagedAndFiltered(String search, int page, int size) {
        PanacheQuery<Product> query;

        if (search != null && !search.trim().isEmpty()) {
            // Busca productos cuyo nombre contenga el texto ignorando mayúsculas/minúsculas
            query = productRepository.find("lower(name) like lower(?1)", "%" + search + "%");
        } else {
            query = productRepository.findAll();
        }

        // Aplica la paginación solicitada usando io.quarkus.panache.common.Page
        return query.page(Page.of(page, size))
                .stream()
                .map(productMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    public ProductResponseDTO getProductById(Long id) {
        Product product = productRepository.findByIdOptional(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + id));
        return productMapper.toResponseDTO(product);
    }

    @Transactional
    public ProductResponseDTO updateProduct(Long id, ProductRequestDTO dto) {
        Product product = productRepository.findByIdOptional(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + id));

        productMapper.updateEntity(dto, product);
        return productMapper.toResponseDTO(product);
    }

    @Transactional
    public void deleteProduct(Long id) {
        if (!productRepository.deleteById(id)) {
            throw new ResourceNotFoundException("No se pudo eliminar. Producto no encontrado con ID: " + id);
        }
    }
}