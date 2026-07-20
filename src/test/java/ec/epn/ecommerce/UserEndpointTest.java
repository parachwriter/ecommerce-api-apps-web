package ec.epn.ecommerce;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;

@QuarkusTest
public class UserEndpointTest {

  @Test
  public void testRegistroUsuarioExitoso() {
    String randomEmail = "user.ok." + System.currentTimeMillis() + "@epn.edu.ec";

    Map<String, Object> userPayload = new HashMap<>();
    userPayload.put("name", "Test User");
    userPayload.put("email", randomEmail);
    userPayload.put("password", "password123");

    given()
        .contentType(ContentType.JSON)
        .body(userPayload)
        .when()
        .post("/api/users/register")
        .then()
        .statusCode(201)
        .body("id", notNullValue())
        .body("email", equalTo(randomEmail))
        .body("name", equalTo("Test User"));
  }

  @Test
  public void testRegistroUsuarioDuplicado() {
    String duplicateEmail = "user.dup." + System.currentTimeMillis() + "@epn.edu.ec";

    Map<String, Object> userPayload = new HashMap<>();
    userPayload.put("name", "Primer Usuario");
    userPayload.put("email", duplicateEmail);
    userPayload.put("password", "password123");

    // Primer registro exitoso
    given()
        .contentType(ContentType.JSON)
        .body(userPayload)
        .when()
        .post("/api/users/register")
        .then()
        .statusCode(201);

    // Segundo intento con las mismas credenciales de correo electrónico
    given()
        .contentType(ContentType.JSON)
        .body(userPayload)
        .when()
        .post("/api/users/register")
        .then()
        .statusCode(400)
        .body("error", equalTo("Bad Request"));
    // Nota: Ajusta a "Conflict" o 409 si tu backend maneja una excepción de
    // unicidad específica
  }

  @Test
  public void testRegistroConDatosInvalidos() {
    Map<String, Object> invalidPayload = new HashMap<>();
    invalidPayload.put("name", ""); // Nombre vacío
    invalidPayload.put("email", "correo-invalido-sin-arroba"); // Email mal formado
    invalidPayload.put("password", "123"); // Contraseña muy corta o débil

    given()
        .contentType(ContentType.JSON)
        .body(invalidPayload)
        .when()
        .post("/api/users/register")
        .then()
        .statusCode(400)
        .body("error", equalTo("Validation Error"));
  }

  @Test
  public void testRegistroCamposFaltantes() {
    Map<String, Object> incompletePayload = new HashMap<>();
    incompletePayload.put("name", "Usuario Incompleto");
    // Dejamos fuera el email y el password de forma deliberada

    given()
        .contentType(ContentType.JSON)
        .body(incompletePayload)
        .when()
        .post("/api/users/register")
        .then()
        .statusCode(400)
        .body("error", equalTo("Validation Error"));
  }
}