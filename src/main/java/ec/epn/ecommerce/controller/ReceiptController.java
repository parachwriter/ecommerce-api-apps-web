package ec.epn.ecommerce.controller;

import ec.epn.ecommerce.dto.receipt.ReceiptCreateDTO;
import ec.epn.ecommerce.dto.receipt.ReceiptResponseDTO;
import ec.epn.ecommerce.service.ReceiptService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("/api/receipts")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@jakarta.annotation.security.RolesAllowed("USER")
public class ReceiptController {

    @Inject
    ReceiptService receiptService;

    @POST
    public Response create(@Valid ReceiptCreateDTO dto) {
        ReceiptResponseDTO response = receiptService.createReceipt(dto);
        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    @GET
    public List<ReceiptResponseDTO> getAll() {
        return receiptService.getAllReceipts();
    }

    @GET
    @RolesAllowed("ADMIN")
    @Path("/{id}")
    public ReceiptResponseDTO getById(@PathParam("id") Long id) {
        return receiptService.getReceiptById(id);
    }

    @GET
    @RolesAllowed("ADMIN")
    @Path("/user/{userId}")
    public List<ReceiptResponseDTO> getByUserId(@PathParam("userId") Long userId) {
        return receiptService.getReceiptsByUserId(userId);
    }

    @DELETE
    @RolesAllowed("ADMIN")
    @Path("/{id}")
    public Response delete(@PathParam("id") Long id) {
        receiptService.deleteReceipt(id);
        return Response.noContent().build();
    }
}