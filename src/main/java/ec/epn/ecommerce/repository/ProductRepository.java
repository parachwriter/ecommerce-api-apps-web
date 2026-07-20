package ec.epn.ecommerce.repository;

import ec.epn.ecommerce.entity.Product;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class ProductRepository implements PanacheRepository<Product> {
    
    // Ejemplo de consulta personalizada por si deseas buscar productos por coincidencia de nombre
    public List<Product> findByName(String name) {
        return list("lower(name) like lower(?1)", "%" + name + "%");
    }
}