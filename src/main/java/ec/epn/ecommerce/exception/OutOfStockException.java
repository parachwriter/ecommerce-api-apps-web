package ec.epn.ecommerce.exception;

public class OutOfStockException extends BadRequestException {
    public OutOfStockException(String productName) {
        super("Stock insuficiente para el producto: " + productName);
    }
}