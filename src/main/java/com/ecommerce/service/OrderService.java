package com.ecommerce.service;

import java.util.List;

import com.ecommerce.exception.OrderException;
import com.ecommerce.exception.UserException;
import com.ecommerce.model.Order;
import com.ecommerce.model.User;
import com.ecommerce.dto.AddressDto;
import com.ecommerce.dto.OrderDto;

public interface OrderService {

    OrderDto createOrder(User user, AddressDto shippingAddress) throws OrderException;

    OrderDto createOrder(User user, Long addressId) throws OrderException;

    Order findOrderById(Long orderId) throws OrderException;

    OrderDto findOrderByOrderId(User user, String orderId) throws UserException, OrderException;

    OrderDto viewOrderById(Long orderId) throws OrderException;

    List<OrderDto> usersOrderHistory(Long userId);

    OrderDto confirmedOrder(Long orderId) throws OrderException;

    OrderDto shippedOrder(Long orderId) throws OrderException;

    OrderDto deliveredOrder(Long orderId) throws OrderException;

    OrderDto cancelOrder(Long orderId) throws OrderException;

    List<OrderDto> getAllOrders();

    void deleteOrder(Long orderId) throws OrderException;

    Order findOrderByRazorpayOrderId(String razorpayOrderId) throws OrderException;

}
