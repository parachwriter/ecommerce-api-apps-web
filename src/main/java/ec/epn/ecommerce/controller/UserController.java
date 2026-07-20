package ec.epn.ecommerce.controller;

import ec.epn.ecommerce.dto.auth.RegisterDTO;
import ec.epn.ecommerce.dto.user.RoleUpdateDTO;
import ec.epn.ecommerce.dto.user.UserRequestDTO;
import ec.epn.ecommerce.dto.user.UserResponseDTO;
import ec.epn.ecommerce.service.UserService;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;
import jakarta.inject.Inject;

import java.util.List;

@Path("/api/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserController {

    @Inject
    UserService userService;

    @POST
    @Path("/register")
    @PermitAll
    public Response register(@Valid RegisterDTO dto) {
        UserResponseDTO response = userService.registerUser(dto);
        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    @GET
    @RolesAllowed("ADMIN")
    public List<UserResponseDTO> getAll() {
        return userService.getAllUsers();
    }

    @GET
    @Path("/{id}")
    @RolesAllowed({ "USER", "ADMIN" })
    public UserResponseDTO getById(@PathParam("id") Long id) {
        return userService.getUserById(id);
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed({ "USER", "ADMIN" })
    public UserResponseDTO update(@PathParam("id") Long id, @Valid UserRequestDTO dto) {
        return userService.updateUser(id, dto);
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed("ADMIN")
    public Response delete(@PathParam("id") Long id) {
        userService.deleteUser(id);
        return Response.noContent().build();
    }

    @PUT
    @Path("/{id}/role")
    @RolesAllowed("ADMIN")
    public UserResponseDTO updateRole(@PathParam("id") Long id, @Valid RoleUpdateDTO dto) {
        return userService.updateRole(id, dto);
    }
}
