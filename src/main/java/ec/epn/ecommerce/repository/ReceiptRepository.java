package ec.epn.ecommerce.repository;

import ec.epn.ecommerce.entity.Receipt;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class ReceiptRepository implements PanacheRepository<Receipt> {
    
    // Requisito de la Fase 4: Buscar compras por usuario
    public List<Receipt> findByUserId(Long userId) {
        return list("user.id", userId);
    }
}