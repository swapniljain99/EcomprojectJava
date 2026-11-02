package com.shop.order;

import com.shop.order.OrderRepository.OrderSummary;
import com.shop.order.OrderService.PlacedOrder;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    public record PlaceOrderRequest(String customerId) {}

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public PlacedOrder place(@RequestBody PlaceOrderRequest request) {
        requireCustomer(request.customerId());
        return orderService.placeOrder(request.customerId());
    }

    @GetMapping
    public List<OrderSummary> list(@RequestParam String customerId) {
        requireCustomer(customerId);
        return orderService.recentOrders(customerId);
    }

    private static void requireCustomer(String customerId) {
        if (customerId == null || customerId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "customerId is required");
        }
    }
}
