package ec.epn.ecommerce;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;

@QuarkusTest
public class ProductEndpointTest {

    @ConfigProperty(name = "app.admin.email", defaultValue = "admin@epn.edu.ec")
    String adminEmail;

    @ConfigProperty(name = "app.admin.password", defaultValue = "ChangeMe123!")
    String adminPassword;

    private String adminToken;

    @BeforeEach
    public void loginAsAdmin() {
        Map<String, Object> loginPayload = new HashMap<>();
        loginPayload.put("email", adminEmail);
        loginPayload.put("password", adminPassword);

        var response = given()
                .contentType(ContentType.JSON)
                .body(loginPayload)
                .when()
                .post("/api/auth/login");

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

            response = given()
                    .contentType(ContentType.JSON)
                    .body(loginPayload)
                    .when()
                    .post("/api/auth/login");
        }

        adminToken = response.then()
                .statusCode(200)
                .extract()
                .path("token");
    }

    @Test
    public void testGetAllProductsEsPublico() {
        given()
                .when()
                .get("/api/products")
                .then()
                .statusCode(200);
    }

    @Test
    public void testGetProductByIdNoEncontrado() {
        given()
                .when()
                .get("/api/products/999999")
                .then()
                .statusCode(404)
                .body("error", equalTo("Resource Not Found"));
    }

    @Test
    public void testCrearProductoSinTokenEsRechazado() {
        Map<String, Object> productPayload = new HashMap<>();
        productPayload.put("name", "Producto sin auth");
        productPayload.put("price", 10.0);
        productPayload.put("stock", 5);

        given()
                .contentType(ContentType.JSON)
                .body(productPayload)
                .when()
                .post("/api/products")
                .then()
                .statusCode(401);
    }

    @Test
    public void testCrearProductoExitoso() {
        Map<String, Object> productPayload = new HashMap<>();
        productPayload.put("name", "Laptop Lenovo IdeaPad");
        productPayload.put("price", 750.00);
        productPayload.put("stock", 15);

        given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(productPayload)
                .when()
                .post("/api/products")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("name", equalTo("Laptop Lenovo IdeaPad"))
                .body("price", equalTo(750.00f))
                .body("stock", equalTo(15));
    }

    @Test
    public void testCrearProductoConPrecioNegativoDevuelveBadRequest() {
        Map<String, Object> productPayload = new HashMap<>();
        productPayload.put("name", "Producto Invalido");
        productPayload.put("price", -10.00);
        productPayload.put("stock", 5);

        given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(productPayload)
                .when()
                .post("/api/products")
                .then()
                .statusCode(400)
                .body("error", equalTo("Validation Error"));
    }

    @Test
    public void testCrearYObtenerProducto() {
        Map<String, Object> productPayload = new HashMap<>();
        productPayload.put("name", "Laptop Lenovo ThinkPad");
        productPayload.put("description", "Laptop para desarrollo");
        productPayload.put("price", 899.99);
        productPayload.put("stock", 15);

        int productId = given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(productPayload)
                .when()
                .post("/api/products")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("name", equalTo("Laptop Lenovo ThinkPad"))
                .body("stock", equalTo(15))
                .extract()
                .path("id");

        given()
                .when()
                .get("/api/products/" + productId)
                .then()
                .statusCode(200)
                .body("name", equalTo("Laptop Lenovo ThinkPad"));
    }

    @Test
    public void testCrearProductoConDatosInvalidos() {
        Map<String, Object> productPayload = new HashMap<>();
        productPayload.put("name", "");
        productPayload.put("price", -5.0);
        productPayload.put("stock", -1);

        given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(productPayload)
                .when()
                .post("/api/products")
                .then()
                .statusCode(400)
                .body("error", equalTo("Validation Error"));
    }

    @Test
    public void testBusquedaYPaginacionDeProductos() {
        String tagUnico = "ZZZ" + System.currentTimeMillis();

        for (int i = 1; i <= 3; i++) {
            Map<String, Object> productPayload = new HashMap<>();
            productPayload.put("name", tagUnico + " Producto " + i);
            productPayload.put("price", 10.0 * i);
            productPayload.put("stock", 5);

            given()
                    .header("Authorization", "Bearer " + adminToken)
                    .contentType(ContentType.JSON)
                    .body(productPayload)
                    .when()
                    .post("/api/products")
                    .then()
                    .statusCode(201);
        }

        given()
                .queryParam("search", tagUnico)
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/api/products")
                .then()
                .statusCode(200)
                .body("size()", equalTo(3));
    }
}