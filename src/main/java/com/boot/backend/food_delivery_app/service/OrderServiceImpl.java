package com.boot.backend.food_delivery_app.service;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.boot.backend.food_delivery_app.entity.OrderEntity;
import com.boot.backend.food_delivery_app.io.OrderRequest;
import com.boot.backend.food_delivery_app.io.OrderResponse;
import com.boot.backend.food_delivery_app.repository.CartRespository;
import com.boot.backend.food_delivery_app.repository.OrderRepository;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService{

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private CartRespository cartRespository;

    @Autowired
    private EmailService emailService;

    @Value("${razorpay.key}")
    private String RAZORPAY_KEY;
    @Value("${razorpay.secret}")
    private String RAZORPAY_SECRET;


    @Override
    public OrderResponse createOrderWithPayment(OrderRequest request) throws RazorpayException {
        // 1️⃣ Convert request to entity & save
        OrderEntity newOrder = convertToEntity(request);
        newOrder = orderRepository.save(newOrder);

        // 2️⃣ Create Razorpay order
        RazorpayClient razorpayClient = new RazorpayClient(RAZORPAY_KEY, RAZORPAY_SECRET);
        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", newOrder.getAmount() * 100); // in paise
        orderRequest.put("currency", "INR");
        orderRequest.put("payment_capture", 1);

        Order razorpayOrder = razorpayClient.orders.create(orderRequest);
        newOrder.setRazorpayOrderId(razorpayOrder.get("id"));

        // 3️⃣ Set user ID
        String loggedInUserId = userService.findByUserId();
        newOrder.setUserId(loggedInUserId);

        // 4️⃣ Save updated order
        newOrder = orderRepository.save(newOrder);

        // 5️⃣ Send confirmation email
        try {
            emailService.sendOrderPlacedEmail(
                    newOrder.getEmail(),
                    newOrder.getUserId(), // replace with actual name if stored
                    newOrder.getId(),
                    newOrder.getOrderedItems().stream()
                            .map(item -> item.getName() + " x " + item.getQuantity())
                            .toList(),
                    newOrder.getAmount()
            );
        } catch (Exception e) {

        }

        // 6️⃣ Return response
        return convertToResponse(newOrder);
    }


    @Override
    public void verifyPayment(Map<String, String> paymentData, String status) {
        String razorpayOrderId = paymentData.get("razorpay_order_id");
        OrderEntity existingOrder = orderRepository.findByRazorpayOrderId(razorpayOrderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        existingOrder.setPaymentStatus(status);
        existingOrder.setRazorpaySignature(paymentData.get("razorpay_signature"));
        existingOrder.setRazorpayPaymentId(paymentData.get("razorpay_payment_id"));
        orderRepository.save(existingOrder);
        if ("paid".equalsIgnoreCase(status)) {
            cartRespository.deleteByUserId(existingOrder.getUserId());
        }
    }

    @Override
    public List<OrderResponse> getUserOrders() {
        String loggedInUserId = userService.findByUserId();
        List<OrderEntity> list = orderRepository.findByUserId(loggedInUserId);
        return list.stream().map(entity -> convertToResponse(entity)).collect(Collectors.toList());
    }

    @Override
    public void removeOrder(String orderId) {
        orderRepository.deleteById(orderId);
    }

    @Override
    public List<OrderResponse> getOrdersOfAllUsers() {
        List<OrderEntity> list = orderRepository.findAll();
        return list.stream().map(entity -> convertToResponse(entity)).collect(Collectors.toList());
    }

    public void updateOrderStatus(String orderId, String status) {
        OrderEntity entity = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        entity.setOrderStatus(status);
        orderRepository.save(entity);

        // Send email if delivered
        if ("Delivered".equalsIgnoreCase(status)) {
            try {
                emailService.sendOrderDeliveredEmail(
                        entity.getEmail(),
                        entity.getUserId(), // replace with name if stored
                        entity.getId(),
                        entity.getOrderedItems().stream()
                                .map(item -> item.getName() + " x " + item.getQuantity())
                                .toList(),
                        entity.getAmount()
                );
            } catch (Exception e) {

            }
        }
    }

    private OrderResponse convertToResponse(OrderEntity newOrder) {
        return OrderResponse.builder()
                .id(newOrder.getId())
                .amount(newOrder.getAmount())
                .userAddress(newOrder.getUserAddress())
                .userId(newOrder.getUserId())
                .razorpayOrderId(newOrder.getRazorpayOrderId())
                .paymentStatus(newOrder.getPaymentStatus())
                .orderStatus(newOrder.getOrderStatus())
                .email(newOrder.getEmail())
                .phoneNumber(newOrder.getPhoneNumber())
                .orderedItems(newOrder.getOrderedItems())
                .build();
    }

    private OrderEntity convertToEntity(OrderRequest request) {
        return OrderEntity.builder()
                .userAddress(request.getUserAddress())
                .amount(request.getAmount())
                .orderedItems(request.getOrderedItems())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .orderStatus(request.getOrderStatus())
                .build();
    }
}
