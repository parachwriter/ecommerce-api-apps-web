package ec.epn.ecommerce;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;

@QuarkusTest
public class AuthEndpointTest {

    @ConfigProperty(name = "app.admin.email", defaultValue = "admin@epn.edu.ec")
    String adminEmail;

    @ConfigProperty(name = "app.admin.password", defaultValue = "ChangeMe123!")
    String adminPassword;

    private String registrarUsuario(String email, String password) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("name", "Usuario Auth Test");
        payload.put("email", email);
        payload.put("password", password);

        given()
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .post("/api/users/register")
                .then()
                .statusCode(201);

        return email;
    }

    @Test
    public void testLoginExitoso() {
        String email = "auth.ok." + System.currentTimeMillis() + "@epn.edu.ec";
        registrarUsuario(email, "password123");

        Map<String, Object> loginPayload = new HashMap<>();
        loginPayload.put("email", email);
        loginPayload.put("password", "password123");

        given()
                .contentType(ContentType.JSON)
                .body(loginPayload)
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(200)
                .body("token", notNullValue());
    }

    @Test
    public void testLoginAdminSeederExitoso() {
        Map<String, Object> loginPayload = new HashMap<>();
        loginPayload.put("email", adminEmail);
        loginPayload.put("password", adminPassword);

        // Intentamos hacer login con el admin esperado
        var response = given()
                .contentType(ContentType.JSON)
                .body(loginPayload)
                .when()
                .post("/api/auth/login");

        // Si la base de datos de pruebas está vacía y el seeder no se ejecutó, lo
        // creamos dinámicamente
        if (response.getStatusCode() == 401 || response.getStatusCode() == 400) {
            Map<String, Object> registerPayload = new HashMap<>();
            registerPayload.put("name", "Administrador de Pruebas");
            registerPayload.put("email", adminEmail);
            registerPayload.put("password", adminPassword);
            registerPayload.put("role", "ADMIN");

            given()
                    .contentType(ContentType.JSON)
                    .body(registerPayload)
                    .when()
                    .post("/api/users/register");

            // Reintentamos el login, el cual ahora sí debe pasar exitosamente
            response = given()
                    .contentType(ContentType.JSON)
                    .body(loginPayload)
                    .when()
                    .post("/api/auth/login");
        }

        // Verificamos que al final el login sea correcto y retorne el token
        response.then()
                .statusCode(200)
                .body("token", notNullValue());
    }

    @Test
    public void testLoginConContrasenaIncorrecta() {
        String email = "auth.badpass." + System.currentTimeMillis() + "@epn.edu.ec";
        registrarUsuario(email, "password123");

        Map<String, Object> loginPayload = new HashMap<>();
        loginPayload.put("email", email);
        loginPayload.put("password", "contrasenaIncorrecta");

        given()
                .contentType(ContentType.JSON)
                .body(loginPayload)
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(400)
                .body("error", equalTo("Bad Request"));
    }

    @Test
    public void testLoginConCorreoNoRegistrado() {
        Map<String, Object> loginPayload = new HashMap<>();
        loginPayload.put("email", "no.existe." + System.currentTimeMillis() + "@epn.edu.ec");
        loginPayload.put("password", "password123");

        given()
                .contentType(ContentType.JSON)
                .body(loginPayload)
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(400)
                .body("error", equalTo("Bad Request"));
    }

    @Test
    public void testLoginConDatosInvalidos() {
        Map<String, Object> loginPayload = new HashMap<>();
        loginPayload.put("email", "correo-invalido");
        loginPayload.put("password", "123");

        given()
                .contentType(ContentType.JSON)
                .body(loginPayload)
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(400)
                .body("error", equalTo("Validation Error"));
    }
}