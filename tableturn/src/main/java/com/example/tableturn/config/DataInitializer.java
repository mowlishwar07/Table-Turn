package com.example.tableturn.config;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.example.tableturn.model.OrderItem;
import com.example.tableturn.repository.OrderItemRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    private final OrderItemRepository orderItemRepository;

    public DataInitializer(OrderItemRepository orderItemRepository) {
        this.orderItemRepository = orderItemRepository;
    }

    @Override
    public void run(String... args) {
        if (orderItemRepository.count() > 0) {
            return;
        }
        List<OrderItem> defaults = List.of(
                menu("Idli", "2 pieces with sambar and chutney", "35.00"),
                menu("Masala Dosa", "Crispy dosa with potato masala", "75.00"),
                menu("Veg Fried Rice", "Vegetable fried rice", "120.00"),
                menu("Paneer Butter Masala", "Paneer in creamy tomato gravy", "180.00"),
                menu("Lemon Juice", "Fresh lemon juice", "50.00"));
        orderItemRepository.saveAll(defaults);
    }

    private OrderItem menu(String name, String description, String price) {
        OrderItem item = new OrderItem();
        item.setItemName(name);
        item.setDescription(description);
        item.setUnitPrice(new BigDecimal(price));
        item.setActive(true);
        return item;
    }
}
