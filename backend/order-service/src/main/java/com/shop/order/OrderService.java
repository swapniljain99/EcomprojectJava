package com.shop.order;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.shop.order.OrderRepository.OrderLine;
import com.shop.order.OrderRepository.OrderSummary;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

@Service
public class OrderService {

    private static final ParameterizedTypeReference<List<CartLine>> CART_LINES = new ParameterizedTypeReference<>() {};

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CartLine(long productId, int quantity) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ProductPrice(long id, int priceCents) {}

    public record CreditCheckRequest(String customerId, long amountCents) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CreditDecision(boolean approved, long creditLimitCents) {}

    public record PaymentRequest(String customerId, long amountCents) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PaymentResult(long id, String status) {}

    public record PlacedOrder(long orderId, String status, long totalCents) {}

    private final RestClient http;
    private final OrderRepository orders;
    private final TransactionTemplate transactions;
    private final String cartUrl;
    private final String productUrl;
    private final String creditUrl;
    private final String paymentUrl;

    public OrderService(
            RestClient.Builder restClientBuilder,
            OrderRepository orders,
            TransactionTemplate transactions,
            @Value("${services.cart-url}") String cartUrl,
            @Value("${services.product-url}") String productUrl,
            @Value("${services.credit-url}") String creditUrl,
            @Value("${services.payment-url}") String paymentUrl) {
        this.http = restClientBuilder.build();
        this.orders = orders;
        this.transactions = transactions;
        this.cartUrl = cartUrl;
        this.productUrl = productUrl;
        this.creditUrl = creditUrl;
        this.paymentUrl = paymentUrl;
    }

    public PlacedOrder placeOrder(String customerId) {
        List<CartLine> cart = http.get()
                .uri(cartUrl + "/api/cart/{customerId}", customerId)
                .retrieve()
                .body(CART_LINES);
        if (cart == null || cart.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Your cart is empty");
        }

        List<OrderLine> lines = new ArrayList<>();
        for (CartLine item : cart) {
            ProductPrice product = http.get()
                    .uri(productUrl + "/api/products/{id}", item.productId())
                    .retrieve()
                    .body(ProductPrice.class);
            if (product == null) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Product service returned no product");
            }
            lines.add(new OrderLine(product.id(), item.quantity(), product.priceCents()));
        }
        long total = lines.stream().mapToLong(line -> (long) line.unitPriceCents() * line.quantity()).sum();

        CreditDecision credit = http.post()
                .uri(creditUrl + "/api/credit/check")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new CreditCheckRequest(customerId, total))
                .retrieve()
                .body(CreditDecision.class);
        if (credit == null || !credit.approved()) {
            throw new ResponseStatusException(HttpStatus.PAYMENT_REQUIRED, "Credit check declined this order");
        }

        PaymentResult payment = http.post()
                .uri(paymentUrl + "/api/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new PaymentRequest(customerId, total))
                .retrieve()
                .body(PaymentResult.class);
        if (payment == null || !"APPROVED".equals(payment.status())) {
            throw new ResponseStatusException(HttpStatus.PAYMENT_REQUIRED, "Payment was declined");
        }

        long orderId = transactions.execute(status -> orders.save(customerId, total, lines));

        http.delete()
                .uri(cartUrl + "/api/cart/{customerId}", customerId)
                .retrieve()
                .toBodilessEntity();

        return new PlacedOrder(orderId, "PLACED", total);
    }

    public List<OrderSummary> recentOrders(String customerId) {
        return orders.findRecentByCustomer(customerId);
    }
}
