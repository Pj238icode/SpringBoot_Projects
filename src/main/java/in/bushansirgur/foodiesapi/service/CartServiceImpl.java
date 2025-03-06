package in.bushansirgur.foodiesapi.service;

import in.bushansirgur.foodiesapi.entity.CartEntity;
import in.bushansirgur.foodiesapi.repository.CartRespository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@AllArgsConstructor
public class CartServiceImpl implements CartService{

    private final CartRespository cartRespository;
    private final UserService userService;
    @Override
    public void addToCart(String foodId) {
        String loggedInUserId = userService.findByUserId();
        Optional<CartEntity> cartOptional = cartRespository.findByUserId(loggedInUserId);
        CartEntity cart = cartOptional.orElseGet(() -> new CartEntity(loggedInUserId, new HashMap<>()));
        Map<String, Integer> cartItems = cart.getItems();
        cartItems.put(foodId, cartItems.getOrDefault(foodId, 0) + 1);
        cart.setItems(cartItems);
        cartRespository.save(cart);
    }
}
