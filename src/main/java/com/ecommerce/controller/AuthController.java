package com.ecommerce.controller;

import com.ecommerce.request.ForgotPasswordRequest;
import com.ecommerce.request.RegisterRequest;
import com.ecommerce.request.ResetPasswordRequest;
import com.ecommerce.response.ApiResponse;
import com.ecommerce.service.AuthService;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ecommerce.exception.UserException;
import com.ecommerce.request.LoginRequest;
import com.ecommerce.response.AuthResponse;

import jakarta.validation.Valid;

@RestController
@AllArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse> signupHandler(@Valid @RequestBody RegisterRequest registerRequest)
            throws UserException, MessagingException {

        authService.signUp(registerRequest);
        ApiResponse apiResponse = new ApiResponse("User Registered Successfully! Please verify your email to login!", true);
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);

    }

    @PostMapping("/signin")
    public ResponseEntity<AuthResponse> signinHandler(@Valid @RequestBody LoginRequest loginRequest) {

        AuthResponse authResponse = authService.signIn(loginRequest);
        return new ResponseEntity<>(authResponse, HttpStatus.OK);

    }

    @GetMapping("/verify")
    public ResponseEntity<ApiResponse> verifyHandler(@RequestParam String token) {

        authService.verifyAccount(token);
        ApiResponse apiResponse = new ApiResponse("Email Verified Successfully! Please login to continue..", true);
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);

    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse> forgotPasswordHandler(@RequestBody ForgotPasswordRequest forgotPasswordRequest, HttpServletRequest request)
            throws UserException, MessagingException {

        authService.sendResetPasswordEmail(forgotPasswordRequest.getEmail(), request);
        ApiResponse apiResponse = new ApiResponse("Password reset link sent to your email.", true);
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);

    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse> resetPasswordHandler(@RequestBody ResetPasswordRequest resetPasswordRequest) {

        authService.resetPassword(resetPasswordRequest.getToken(), resetPasswordRequest.getNewPassword());
        ApiResponse apiResponse = new ApiResponse("Password reset successfully! You can now log in with your new password.", true);
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);

    }

}
