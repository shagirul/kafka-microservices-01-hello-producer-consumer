
package com.example.orders;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
public class OrderController {
    private final OrderPublisher publisher;

    public OrderController(OrderPublisher publisher) {
        this.publisher = publisher;
    }

    @PostMapping
    public ResponseEntity<String> createOrder(@RequestBody OrderEvent event) {
        publisher.publish(event);
        return ResponseEntity.ok("Published order " + event.getOrderId());
    }
}
