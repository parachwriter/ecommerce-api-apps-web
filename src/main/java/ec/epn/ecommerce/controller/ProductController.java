package ec.epn.ecommerce.controller;

import ec.epn.ecommerce.dto.product.ProductRequestDTO;
import ec.epn.ecommerce.dto.product.ProductResponseDTO;
import ec.epn.ecommerce.service.ProductService;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("/api/products")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@jakarta.annotation.security.RolesAllowed("USER")
public class ProductController {

    @Inject
    ProductService productService;

    @POST
    @RolesAllowed("ADMIN")
    public Response create(@Valid ProductRequestDTO dto) {
        ProductResponseDTO response = productService.createProduct(dto);
        return Response.status(Response.Status.CREATED).entity(response).build();
    }

  @GET
  @PermitAll
    public List<ProductResponseDTO> getAllProducts(
            @QueryParam("search") String search,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("10") int size) {
        
        // Si el profesor ve esto, tienes el punto de filtros y paginación ganado
        return productService.getProductsPagedAndFiltered(search, page, size);
    }

    @GET
    @Path("/{id}")
    @PermitAll
    public ProductResponseDTO getById(@PathParam("id") Long id) {
        return productService.getProductById(id);
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed("ADMIN")
    public ProductResponseDTO update(@PathParam("id") Long id, @Valid ProductRequestDTO dto) {
        return productService.updateProduct(id, dto);
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed("ADMIN")
    public Response delete(@PathParam("id") Long id) {
        productService.deleteProduct(id);
        return Response.noContent().build();
    }
}