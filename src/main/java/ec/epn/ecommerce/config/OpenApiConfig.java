package ec.epn.ecommerce.config;

import jakarta.ws.rs.core.Application;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.info.Contact;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.info.License;

@OpenAPIDefinition(
    info = @Info(
        title = "ESCUELA POLITÉCNICA NACIONAL - E-commerce API",
        version = "1.0.0",
        description = "Backend modular transaccional desarrollado por el Grupo 1 para la gestión segura de usuarios, inventarios y notas de venta.",
        contact = @Contact(
            name = "Soporte Académico - Grupo 1",
            url = "https://www.epn.edu.ec"
        ),
        license = @License(
            name = "Uso Académico / EPN",
            url = "https://www.epn.edu.ec"
        )
    )
)
public class OpenApiConfig extends Application {
    // Esta clase se deja vacía intencionalmente. 
    // Su único propósito es registrar los metadatos de OpenAPI en el arranque de Quarkus.
}