package com.ecommerce.service;

import com.ecommerce.exception.OrderException;
import com.ecommerce.exception.UserException;
import com.ecommerce.response.PaymentLinkResponse;
import com.razorpay.RazorpayException;

import java.util.Map;

public interface PaymentService {

    PaymentLinkResponse createPaymentLink(Long orderId, String jwt) throws RazorpayException, UserException, OrderException;

    void processPaymentWebhook(String payload) throws Exception;

    void processPaymentCallback(Map<String, String> params);

}
