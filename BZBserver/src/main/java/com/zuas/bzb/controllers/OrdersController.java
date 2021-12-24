package com.zuas.bzb.controllers;

import com.zuas.bzb.dao.OrdersDAO;
import com.zuas.bzb.model.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrdersController {
    private final OrdersDAO ordersDAO;

    @Autowired
    public OrdersController(OrdersDAO ordersDAO) {
        this.ordersDAO = ordersDAO;
    }

    @PostMapping("/new")
    public OrderResponseDTO addOrder(@RequestBody OrderDTO dto) {
        return ordersDAO.add(dto);
    }
    @GetMapping("/check")
    public OrderDTO orderCheckCoordinates(@RequestBody OrderDTO dto ) {
            return ordersDAO.check(dto);
    }
    @PostMapping("/update")
    public boolean updateOrder(@RequestBody List<OrderToUpdateDTO> dto) {
        return ordersDAO.update(dto);
    }
    @PostMapping("/description")
    public MarkerDescriptionDTO getDescription(@RequestBody MarkerDTO dto) {
        return ordersDAO.getDescription(dto);
    }
    @GetMapping
    public List<MarkerDTO> showOrders() {
        return ordersDAO.showAll();
    }

    @PostMapping("/orders_list")
    public List<UserOrderDTO> getUsersOrders(@RequestBody UserIdDTO someUserId) {
        return ordersDAO.getOrdersUsingUserId(someUserId);
    }
    @PostMapping("/delete")
    public boolean deleteOrder(@RequestBody OrderResponseDTO orderToDelete) {
        return ordersDAO.deleteOrder(orderToDelete);
    }
}
