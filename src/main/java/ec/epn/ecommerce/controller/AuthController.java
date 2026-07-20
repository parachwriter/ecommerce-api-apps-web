package ec.epn.ecommerce.controller;

import ec.epn.ecommerce.dto.auth.LoginDTO;
import ec.epn.ecommerce.dto.auth.TokenDTO;
import ec.epn.ecommerce.service.AuthService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthController {

    @Inject
    AuthService authService;

    @POST
    @Path("/login")
    public Response login(@Valid LoginDTO dto) {
        TokenDTO token = authService.login(dto);
        return Response.ok(token).build();
    }
}