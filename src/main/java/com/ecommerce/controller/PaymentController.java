package com.ecommerce.controller;

import com.ecommerce.service.PaymentService;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ecommerce.exception.OrderException;
import com.ecommerce.exception.UserException;
import com.ecommerce.response.ApiResponse;
import com.ecommerce.response.PaymentLinkResponse;
import com.razorpay.RazorpayException;

import java.util.Map;

@RestController
@AllArgsConstructor
@RequestMapping("/api")
public class PaymentController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);
    private final PaymentService paymentService;

    @PostMapping("/payments/{orderId}")
    public ResponseEntity<PaymentLinkResponse> createPaymentLink(@PathVariable @NotNull Long orderId, @RequestHeader("Authorization") String jwt)
            throws RazorpayException, UserException, OrderException {
        PaymentLinkResponse response = paymentService.createPaymentLink(orderId, jwt);
        return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
    }

    @PostMapping("/webhook/payment")
    public ResponseEntity<ApiResponse> handlePaymentWebhook(@RequestBody String payload) {
        try {
            paymentService.processPaymentWebhook(payload);
            return new ResponseEntity<>(new ApiResponse("Webhook processed successfully", true), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error processing webhook: {}", e.getMessage(), e);
            return new ResponseEntity<>(new ApiResponse("Webhook processing failed", false), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/payments/callback")
    public ResponseEntity<Void> handlePaymentCallback(@RequestParam Map<String, String> params) {
        paymentService.processPaymentCallback(params);
        return ResponseEntity.status(HttpStatus.FOUND).build();
    }

}
