package com.ecommerce.service.impl;

import com.ecommerce.service.PaymentService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import com.ecommerce.user.domain.PaymentMethod;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ecommerce.exception.OrderException;
import com.ecommerce.exception.UserException;
import com.ecommerce.model.Order;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.response.PaymentLinkResponse;
import com.ecommerce.service.OrderService;
import com.ecommerce.user.domain.OrderStatus;
import com.ecommerce.user.domain.PaymentStatus;
import com.razorpay.PaymentLink;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Map;

@Service
public class PaymentServiceImpl implements PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentServiceImpl.class);
    private final OrderService orderService;
    private final OrderRepository orderRepository;
    private final RazorpayClient razorpayClient;
    private final String frontendBaseUrl;
    private final String backendBaseUrl;

    public PaymentServiceImpl(OrderService orderService,
                              OrderRepository orderRepository,
                              RazorpayClient razorpayClient,
                              @Qualifier("frontendBaseUrl") String frontendBaseUrl,
                              @Qualifier("backendBaseUrl") String backendBaseUrl) {
        this.orderService = orderService;
        this.orderRepository = orderRepository;
        this.razorpayClient = razorpayClient;
        this.frontendBaseUrl = frontendBaseUrl;
        this.backendBaseUrl = backendBaseUrl;
    }

    @Override
    public PaymentLinkResponse createPaymentLink(Long orderId, String jwt) throws RazorpayException, UserException, OrderException {

        Order order = orderService.findOrderById(orderId);
        if (order.getOrderStatus() != OrderStatus.PENDING) {
            throw new OrderException("Order already placed! No need to pay again.");
        }

        JSONObject paymentLinkRequest = new JSONObject();
        paymentLinkRequest.put("amount", order.getTotalDiscountedPrice() * 100);
        paymentLinkRequest.put("currency", "INR");

        JSONObject notes = new JSONObject();
        notes.put("orderId", orderId);
        paymentLinkRequest.put("notes", notes);

        JSONObject customer = new JSONObject();
        customer.put("name", order.getUser().getFirstName() + " " + order.getUser().getLastName());
        customer.put("contact", order.getUser().getMobile());
        customer.put("email", order.getUser().getEmail());
        paymentLinkRequest.put("customer", customer);

        JSONObject notify = new JSONObject();
        notify.put("sms", true);
        notify.put("email", true);
        paymentLinkRequest.put("notify", notify);
        paymentLinkRequest.put("reminder_enable", true);

        paymentLinkRequest.put("callback_url", backendBaseUrl + "/api/payments/callback?orderId=" + order.getOrderId());
        paymentLinkRequest.put("callback_method", "get");

        PaymentLink payment = razorpayClient.paymentLink.create(paymentLinkRequest);

        String paymentLinkId = payment.get("id");
        String paymentLinkUrl = payment.get("short_url");
        logger.info("Payment id: {}, Payment link: {}", paymentLinkId, paymentLinkUrl);

        order.getPaymentDetails().setRazorpayPaymentLinkId(paymentLinkId);
        order.getPaymentDetails().setRazorpayPaymentLinkUrl(paymentLinkUrl);
        orderRepository.save(order);

        return new PaymentLinkResponse(paymentLinkUrl, paymentLinkId);
    }

    @Override
    public void processPaymentWebhook(String payload) throws Exception {
        logger.info("Processing payment webhook: {}", payload);

        JSONObject jsonPayload = new JSONObject(payload);
        String eventType = jsonPayload.getString("event");

        if ("payment.captured".equals(eventType)) {
            JSONObject paymentDetails = jsonPayload.getJSONObject("payload").getJSONObject("payment").getJSONObject("entity");

            JSONObject notes = paymentDetails.getJSONObject("notes");
            Long orderId = notes.getLong("orderId");

            Order order = orderService.findOrderById(orderId);
            order.getPaymentDetails().setRazorpayAccountId(jsonPayload.getString("account_id"));
            order.getPaymentDetails().setRazorpayPaymentId(paymentDetails.getString("id"));
            order.setRazorpayOrderId(paymentDetails.getString("order_id"));
            order.getPaymentDetails().setRazorpayPaymentMethod(PaymentMethod.valueOf(paymentDetails.getString("method").toUpperCase()));
            order.getPaymentDetails().setRazorpayPaymentDescription(paymentDetails.getString("description"));
            order.getPaymentDetails().setRazorpayPaymentStatus(PaymentStatus.COMPLETED);
            order.setOrderStatus(OrderStatus.PLACED);

            orderRepository.save(order);
            logger.info("Payment processed successfully for orderId: {}", orderId);

        } else {
            logger.warn("Unsupported event type: {}", eventType);
        }

    }

    @Override
    public void processPaymentCallback(Map<String, String> params) {
        try {
            // Extract necessary parameters
            String orderId = params.get("orderId"); // Razorpay order ID in callback URL

            // Construct the clean redirect URL (exclude unnecessary query parameters)
            String redirectUrl = frontendBaseUrl + "/orders/" + orderId;

            // Perform the redirection
            HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
            if (response != null) {
                response.sendRedirect(redirectUrl);
            }
        } catch (Exception ex) {
            logger.error("Error handling payment callback: {}", ex.getMessage());
            throw new RuntimeException("Failed to process payment callback.", ex);
        }
    }

}
