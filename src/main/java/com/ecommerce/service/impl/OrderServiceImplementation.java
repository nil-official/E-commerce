package com.ecommerce.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.ecommerce.exception.UserException;
import com.ecommerce.mapper.AddressMapper;
import com.ecommerce.mapper.OrderMapper;
import com.ecommerce.dto.AddressDto;
import com.ecommerce.dto.OrderDto;
import com.ecommerce.model.*;
import com.ecommerce.repository.*;
import com.ecommerce.service.CartService;
import com.ecommerce.service.OrderService;
import com.ecommerce.utility.DtoValidatorUtil;
import com.ecommerce.utility.QuantityCalculatorUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.ecommerce.exception.OrderException;
import com.ecommerce.user.domain.OrderStatus;
import com.ecommerce.user.domain.PaymentStatus;

@Slf4j
@Service
@AllArgsConstructor
public class OrderServiceImplementation implements OrderService {

    private OrderRepository orderRepository;
    private CartService cartService;
    private AddressRepository addressRepository;
    private UserRepository userRepository;
    private OrderItemRepository orderItemRepository;
    private ProductRepository productRepository;

    private OrderDto generateOrder(User user, OrderAddress orderAddress) throws OrderException {
        // Get the cart of the user
        Cart cart = cartService.findCart(user.getId());

        if (cart.getCartItems().isEmpty()) {
            throw new OrderException("Cart is empty");
        }

        // Create a new empty order items list
        List<OrderItem> orderItems = new ArrayList<>();

        // Loop through the cart items and create order items
        for (CartItem item : cart.getCartItems()) {
            OrderItem orderItem = new OrderItem();

            orderItem.setPrice(item.getPrice());
            orderItem.setProduct(item.getProduct());
            orderItem.setQuantity(item.getQuantity());
            orderItem.setSize(item.getSize());
            orderItem.setUserId(item.getUserId());
            orderItem.setDiscountedPrice(item.getDiscountedPrice());

            OrderItem createdOrderItem = orderItemRepository.save(orderItem);
            orderItems.add(createdOrderItem);

            // Get the associated product from the order item
            Product product = item.getProduct();

            // Get the ordered item's size
            Size orderedSize = product.getSizes().stream()
                    .filter(size -> size.getName().toString().equals(item.getSize()))
                    .findFirst()
                    .orElseThrow(() -> new OrderException("Invalid size: " + item.getSize()));

            // Check if there's enough quantity available to fulfill the order
            if (orderedSize.getQuantity() < item.getQuantity()) {
                throw new OrderException("Not enough stock for size: " + orderedSize.getName());
            }

            // Reduce the available quantity of the size
            orderedSize.setQuantity(orderedSize.getQuantity() - item.getQuantity());

            // Save the updated sizes in the product
            product.setSizes(product.getSizes());

            // Update the total quantity of the product
            product.setQuantity(QuantityCalculatorUtil.getTotalQuantity(product.getSizes()));

            // Save the updated product
            productRepository.save(product);

        }

        // Generate a unique order ID
        String orderId = "ORD" + System.currentTimeMillis();

        // Create a new order object
        Order createdOrder = new Order();
        createdOrder.setUser(user);
        createdOrder.setOrderId(orderId);
        createdOrder.setOrderItems(orderItems);
        createdOrder.setDeliveryDate(LocalDateTime.now().plusDays(5));
        createdOrder.setTotalPrice(cart.getTotalPrice());
        createdOrder.setTotalDiscountedPrice(cart.getTotalDiscountedPrice());
        createdOrder.setDiscount(cart.getDiscount());
        createdOrder.setTotalItem(cart.getTotalItem());
        createdOrder.setOrderAddress(orderAddress);
        createdOrder.setOrderDate(LocalDateTime.now());
        createdOrder.setOrderStatus(OrderStatus.PENDING);
        createdOrder.getPaymentDetails().setRazorpayPaymentStatus(PaymentStatus.PENDING);
        createdOrder.setCreatedAt(LocalDateTime.now());

        // Save the order
        Order savedOrder = orderRepository.save(createdOrder);
        // Map the saved order to OrderDto
        OrderDto resOrder = OrderMapper.toOrderDto(savedOrder);
        for (OrderItem item : orderItems) {
            item.setOrder(savedOrder);
            orderItemRepository.save(item);
        }

        return resOrder;
    }

    @Override
    public OrderDto createOrder(User user, AddressDto addressDto) throws OrderException {

        // Validate the address
        DtoValidatorUtil.validate(addressDto);

        OrderAddress orderAddress = new OrderAddress(
                addressDto.getFirstName(),
                addressDto.getLastName(),
                addressDto.getStreetAddress(),
                addressDto.getCity(),
                addressDto.getState(),
                addressDto.getZipCode(),
                addressDto.getMobile()
        );

        Address reqAddress = AddressMapper.toAddress(addressDto);
        reqAddress.setUser(user);
        Address address = addressRepository.save(reqAddress);
        user.getAddresses().add(address);
        userRepository.save(user);

        return generateOrder(user, orderAddress);
    }

    @Override
    public OrderDto createOrder(User user, Long addressId) throws OrderException {
        // Get the address from the addressId
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new OrderException("Address not found with Address ID: " + addressId));

        OrderAddress orderAddress = new OrderAddress(
                address.getFirstName(),
                address.getLastName(),
                address.getStreetAddress(),
                address.getCity(),
                address.getState(),
                address.getZipCode(),
                address.getMobile()
        );

        return generateOrder(user, orderAddress);

    }

    @Override
    public OrderDto findOrderByOrderId(User user, String orderId) throws UserException, OrderException {

        if (user == null) {
            throw new UserException("Invalid User");
        }
        if (orderId.isEmpty()) {
            throw new OrderException("Invalid Order ID");
        }
        return OrderMapper.toOrderDto(orderRepository.findOrderByOrderId(orderId)
                .orElseThrow(() -> new OrderException("Order not found with Order ID: " + orderId)));

    }

    @Override
    public Order findOrderByRazorpayOrderId(String razorpayOrderId) throws OrderException {
        return orderRepository.findByRazorpayOrderId(razorpayOrderId)
                .orElseThrow(() -> new OrderException("Order not found with Razorpay Order ID: " + razorpayOrderId));
    }

    @Override
    public OrderDto confirmedOrder(Long orderId) throws OrderException {
        Order order = findOrderById(orderId);
        order.setOrderStatus(OrderStatus.CONFIRMED);

        Order saveOrder = orderRepository.save(order);
        return OrderMapper.toOrderDto(saveOrder);
    }

    @Override
    public OrderDto shippedOrder(Long orderId) throws OrderException {
        Order order = findOrderById(orderId);
        order.setOrderStatus(OrderStatus.SHIPPED);

        Order saveOrder = orderRepository.save(order);
        return OrderMapper.toOrderDto(saveOrder);
    }

    @Override
    public OrderDto deliveredOrder(Long orderId) throws OrderException {
        Order order = findOrderById(orderId);
        order.setOrderStatus(OrderStatus.DELIVERED);

        Order saveOrder = orderRepository.save(order);
        return OrderMapper.toOrderDto(saveOrder);
    }

    @Override
    public OrderDto cancelOrder(Long orderId) throws OrderException {
        Order order = findOrderById(orderId);
        order.setOrderStatus(OrderStatus.CANCELLED);

        Order saveOrder = orderRepository.save(order);
        return OrderMapper.toOrderDto(saveOrder);
    }

    @Override
    public Order findOrderById(Long orderId) throws OrderException {
        Optional<Order> opt = orderRepository.findById(orderId);

        if (opt.isPresent()) {
            return opt.get();
        }
        throw new OrderException("order not exist with id " + orderId);
    }

    @Override
    public OrderDto viewOrderById(Long orderId) throws OrderException {
        Optional<Order> opt = orderRepository.findById(orderId);
        if (opt.isPresent()) {
            return OrderMapper.toOrderDto(opt.get());
        }
        throw new OrderException("order not exist with id " + orderId);
    }

    @Override
    public List<OrderDto> usersOrderHistory(Long userId) {
        List<Order> saveOrder = orderRepository.getUsersOrders(userId);
        List<OrderDto> orderDto = new ArrayList<>();
        for (Order order : saveOrder) {
            orderDto.add(OrderMapper.toOrderDto(order));
        }
        return orderDto;
        //return orderRepository.getUsersOrders(userId);
    }

    @Override
    public List<OrderDto> getAllOrders() {
        List<Order> saveOrder = orderRepository.findAll();
        List<OrderDto> orderDto = new ArrayList<>();
        for (Order order : saveOrder) {
            orderDto.add(OrderMapper.toOrderDto(order));
        }
        return orderDto;
    }

    @Override
    public void deleteOrder(Long orderId) throws OrderException {
        Order order = findOrderById(orderId);
        if (order == null) {
            throw new OrderException("order not exist with id " + orderId);
        }
        orderRepository.deleteById(orderId);

    }

}
