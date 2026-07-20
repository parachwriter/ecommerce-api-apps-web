package ec.epn.ecommerce;

// Ubicar en: src/test/java/ec/epn/ecommerce/ReceiptEndpointTest.java

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.containsString;

@QuarkusTest
public class ReceiptEndpointTest {

    @ConfigProperty(name = "app.admin.email", defaultValue = "admin@epn.edu.ec")
    String adminEmail;

    @ConfigProperty(name = "app.admin.password", defaultValue = "ChangeMe123!")
    String adminPassword;

    private record Credenciales(String token, long userId) {
    }

    private Credenciales registrarYAutenticar() {
        String email = "receipt.test." + System.currentTimeMillis() + "@epn.edu.ec";

        Map<String, Object> registerPayload = new HashMap<>();
        registerPayload.put("name", "Usuario Receipt Test");
        registerPayload.put("email", email);
        registerPayload.put("password", "password123");

        long userId = given()
                .contentType(ContentType.JSON)
                .body(registerPayload)
                .when()
                .post("/api/users/register")
                .then()
                .statusCode(201)
                .extract()
                .jsonPath()
                .getLong("id");

        Map<String, Object> loginPayload = new HashMap<>();
        loginPayload.put("email", email);
        loginPayload.put("password", "password123");

        String token = given()
                .contentType(ContentType.JSON)
                .body(loginPayload)
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(200)
                .extract().path("token");

        return new Credenciales(token, userId);
    }

    private String obtenerTokenAdmin() {
        Map<String, Object> loginPayload = new HashMap<>();
        loginPayload.put("email", adminEmail);
        loginPayload.put("password", adminPassword);

        // Realizamos el intento inicial de login
        var response = given()
                .contentType(ContentType.JSON)
                .body(loginPayload)
                .when()
                .post("/api/auth/login");

        // Si el seeder de base de datos no se ejecutó, el usuario administrador no existirá
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

            // Reintentamos autenticarnos tras registrar al administrador bajo demanda
            response = given()
                    .contentType(ContentType.JSON)
                    .body(loginPayload)
                    .when()
                    .post("/api/auth/login");
        }

        return response.then()
                .statusCode(200)
                .extract()
                .path("token");
    }

    private int crearProducto(String nombre, double precio, int stock) {
        String adminToken = obtenerTokenAdmin();

        Map<String, Object> productPayload = new HashMap<>();
        productPayload.put("name", nombre);
        productPayload.put("price", precio);
        productPayload.put("stock", stock);

        return given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(productPayload)
                .when()
                .post("/api/products")
                .then()
                .statusCode(201)
                .extract().path("id");
    }

    @Test
    public void testCrearNotaDeVentaExitosaYCalculoDeTotal() {
        Credenciales cred = registrarYAutenticar();
        int productId = crearProducto("Mouse Inalambrico", 25.50, 10);

        Map<String, Object> item = new HashMap<>();
        item.put("productId", productId);
        item.put("quantity", 2);

        Map<String, Object> receiptPayload = new HashMap<>();
        receiptPayload.put("userId", cred.userId());
        receiptPayload.put("items", List.of(item));

        given()
                .header("Authorization", "Bearer " + cred.token())
                .contentType(ContentType.JSON)
                .body(receiptPayload)
                .when()
                .post("/api/receipts")
                .then()
                .statusCode(201)
                .body("total", equalTo(51.0f))
                .body("items[0].subtotal", equalTo(51.0f));

        given()
                .when()
                .get("/api/products/" + productId)
                .then()
                .statusCode(200)
                .body("stock", equalTo(8));
    }

    @Test
    public void testCrearNotaDeVentaConStockInsuficiente() {
        Credenciales cred = registrarYAutenticar();
        int productId = crearProducto("Teclado Mecanico", 45.0, 1);

        Map<String, Object> item = new HashMap<>();
        item.put("productId", productId);
        item.put("quantity", 5);

        Map<String, Object> receiptPayload = new HashMap<>();
        receiptPayload.put("userId", cred.userId());
        receiptPayload.put("items", List.of(item));

        given()
                .header("Authorization", "Bearer " + cred.token())
                .contentType(ContentType.JSON)
                .body(receiptPayload)
                .when()
                .post("/api/receipts")
                .then()
                .statusCode(400)
                .body("error", equalTo("Bad Request"))
                .body("message", containsString("Stock insuficiente"));

        given()
                .when()
                .get("/api/products/" + productId)
                .then()
                .statusCode(200)
                .body("stock", equalTo(1));
    }

    @Test
    public void testCrearNotaDeVentaConUsuarioInexistente() {
        Credenciales cred = registrarYAutenticar();
        int productId = crearProducto("Monitor 24 pulgadas", 150.0, 5);

        Map<String, Object> item = new HashMap<>();
        item.put("productId", productId);
        item.put("quantity", 1);

        Map<String, Object> receiptPayload = new HashMap<>();
        receiptPayload.put("userId", 999999L);
        receiptPayload.put("items", List.of(item));

        given()
                .header("Authorization", "Bearer " + cred.token())
                .contentType(ContentType.JSON)
                .body(receiptPayload)
                .when()
                .post("/api/receipts")
                .then()
                .statusCode(404)
                .body("error", equalTo("Resource Not Found"));
    }

    @Test
    public void testEliminarNotaDeVentaInexistente() {
        Credenciales cred = registrarYAutenticar();

        given()
                .header("Authorization", "Bearer " + cred.token())
                .when()
                .delete("/api/receipts/999999")
                .then()
                .statusCode(404)
                .body("error", equalTo("Resource Not Found"));
    }
}