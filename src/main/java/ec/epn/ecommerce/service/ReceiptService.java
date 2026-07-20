package ec.epn.ecommerce.service;

import ec.epn.ecommerce.dto.receipt.ReceiptCreateDTO;
import ec.epn.ecommerce.dto.receipt.ReceiptItemDTO;
import ec.epn.ecommerce.dto.receipt.ReceiptResponseDTO;
import ec.epn.ecommerce.entity.Product;
import ec.epn.ecommerce.entity.Receipt;
import ec.epn.ecommerce.entity.ReceiptItem;
import ec.epn.ecommerce.entity.User;
import ec.epn.ecommerce.exception.BadRequestException;
import ec.epn.ecommerce.exception.OutOfStockException;
import ec.epn.ecommerce.exception.ResourceNotFoundException;
import ec.epn.ecommerce.mapper.ReceiptMapper;
import ec.epn.ecommerce.repository.ProductRepository;
import ec.epn.ecommerce.repository.ReceiptRepository;
import ec.epn.ecommerce.repository.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class ReceiptService {

    @Inject
    ReceiptRepository receiptRepository;

    @Inject
    UserRepository userRepository;

    @Inject
    ProductRepository productRepository;

    @Inject
    ReceiptMapper receiptMapper;

    
    @Transactional
    public ReceiptResponseDTO createReceipt(ReceiptCreateDTO dto) {
        User user = userRepository.findByIdOptional(dto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + dto.getUserId()));

        Receipt receipt = new Receipt();
        receipt.setUser(user);
        
        BigDecimal totalReceipt = BigDecimal.ZERO;

        for (ReceiptItemDTO itemDto : dto.getItems()) {
            Product product = productRepository.findByIdOptional(itemDto.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + itemDto.getProductId()));

            // Regla obligatoria de negocio: Validar Stock usando tu nueva excepción
            if (product.getStock() < itemDto.getQuantity()) {
                throw new OutOfStockException(product.getName());
            }

            // Regla obligatoria de negocio: Descontar Stock
            product.setStock(product.getStock() - itemDto.getQuantity());

            // Regla obligatoria de negocio: Calcular Subtotal con precisión decimal
            BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(itemDto.getQuantity()));

            ReceiptItem item = new ReceiptItem();
            item.setProduct(product);
            item.setQuantity(itemDto.getQuantity());
            item.setUnitPrice(product.getPrice());
            item.setSubtotal(subtotal);

            // Vincula el ítem de forma bidireccional
            receipt.addItem(item);

            // Regla obligatoria de negocio: Calcular Total acumulado de forma interna
            totalReceipt = totalReceipt.add(subtotal);
        }

        receipt.setTotal(totalReceipt);
        receiptRepository.persist(receipt);

        return receiptMapper.toResponseDTO(receipt);
    }

    public List<ReceiptResponseDTO> getAllReceipts() {
        return receiptRepository.listAll().stream()
                .map(receiptMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    public ReceiptResponseDTO getReceiptById(Long id) {
        Receipt receipt = receiptRepository.findByIdOptional(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nota de venta no encontrada con ID: " + id));
        return receiptMapper.toResponseDTO(receipt);
    }

    public List<ReceiptResponseDTO> getReceiptsByUserId(Long userId) {
        if (userRepository.findByIdOptional(userId).isEmpty()) {
            throw new ResourceNotFoundException("Usuario no encontrado con ID: " + userId);
        }
        return receiptRepository.findByUserId(userId).stream()
                .map(receiptMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteReceipt(Long id) {
        if (!receiptRepository.deleteById(id)) {
            throw new ResourceNotFoundException("No se pudo eliminar. Nota de venta no encontrada con ID: " + id);
        }
    }
}