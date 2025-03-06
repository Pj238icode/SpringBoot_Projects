package in.bushansirgur.foodiesapi.controller;

import in.bushansirgur.foodiesapi.service.CartService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/cart")
@AllArgsConstructor
public class CartController {


    private final CartService cartService;
    @PostMapping
    public ResponseEntity<?> addToCart(@RequestBody Map<String, String> request) {
        String foodId = request.get("foodId");
        if (foodId == null || foodId.isEmpty()) {
            return ResponseEntity.badRequest().body("Food Id is required");
        }
        cartService.addToCart(foodId);
        return ResponseEntity.ok().body(null);
    }
}
