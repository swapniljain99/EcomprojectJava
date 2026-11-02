package com.shop.cart;

import com.shop.cart.CartRepository.CartLine;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/cart/{customerId}")
public class CartController {

    private static final int MAX_QUANTITY = 99;

    public record QuantityUpdate(int quantity) {}

    private final CartRepository cart;

    public CartController(CartRepository cart) {
        this.cart = cart;
    }

    @GetMapping
    public List<CartLine> get(@PathVariable String customerId) {
        return cart.findLines(customerId);
    }

    @PutMapping("/items/{productId}")
    public List<CartLine> setQuantity(
            @PathVariable String customerId,
            @PathVariable long productId,
            @RequestBody QuantityUpdate update) {
        if (update.quantity() < 0 || update.quantity() > MAX_QUANTITY) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantity must be between 0 and " + MAX_QUANTITY);
        }
        if (update.quantity() == 0) {
            cart.removeLine(customerId, productId);
        } else {
            cart.upsertQuantity(customerId, productId, update.quantity());
        }
        return cart.findLines(customerId);
    }

    @DeleteMapping
    public void clear(@PathVariable String customerId) {
        cart.clear(customerId);
    }
}
