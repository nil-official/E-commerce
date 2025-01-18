package com.ecommerce.service.impl;

import com.ecommerce.config.JwtTokenProvider;
import com.ecommerce.exception.InvalidTokenException;
import com.ecommerce.exception.TokenExpiredException;
import com.ecommerce.exception.UserException;
import com.ecommerce.exception.UserNotVerifiedException;
import com.ecommerce.model.Role;
import com.ecommerce.model.User;
import com.ecommerce.model.VerifyToken;
import com.ecommerce.repository.RoleRepository;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.repository.VerifyTokenRepository;
import com.ecommerce.request.LoginRequest;
import com.ecommerce.request.RegisterRequest;
import com.ecommerce.response.AuthResponse;
import com.ecommerce.security.CustomUserDetailsService;
import com.ecommerce.service.AuthService;
import com.ecommerce.service.CartService;
import com.ecommerce.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
public class AuthServiceImplementation implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final CartService cartService;
    private final VerifyTokenRepository verifyTokenRepository;
    private final EmailService emailService;
    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService customUserDetails;
    private final String frontendBaseUrl;

    public AuthServiceImplementation(UserRepository userRepository,
                                     PasswordEncoder passwordEncoder,
                                     RoleRepository roleRepository,
                                     CartService cartService,
                                     VerifyTokenRepository verifyTokenRepository,
                                     EmailService emailService,
                                     JwtTokenProvider jwtTokenProvider,
                                     CustomUserDetailsService customUserDetails,
                                     @Qualifier("frontendBaseUrl") String frontendBaseUrl) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
        this.cartService = cartService;
        this.verifyTokenRepository = verifyTokenRepository;
        this.emailService = emailService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.customUserDetails = customUserDetails;
        this.frontendBaseUrl = frontendBaseUrl;
    }

    @Override
    public void signUp(RegisterRequest registerRequest) throws UserException, MessagingException {

        // Check if user with the given email already exists
        User isEmailExist = userRepository.findByEmail(registerRequest.getEmail());
        if (isEmailExist != null) {
            throw new UserException("Email Is Already Used With Another Account");
        }

        // Creating a User object
        User user = new User();
        user.setFirstName(registerRequest.getFirstName());
        user.setLastName(registerRequest.getLastName());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));

        // Adding roles
        Set<Role> roles = new HashSet<>();
        Role userRole = roleRepository.findByName("ROLE_USER").orElseThrow(() -> new RuntimeException("Role not found"));
        roles.add(userRole);
        user.setRoles(roles);

        // Saving the object into DB
        User savedUser = userRepository.save(user);

        // Creating a cart for the new user
        cartService.createCart(savedUser);

        // Creating an email verification token and saving it into DB
        String token = UUID.randomUUID().toString();
        VerifyToken verifyToken = new VerifyToken();
        verifyToken.setToken(token);
        verifyToken.setUser(savedUser);
        verifyToken.setExpiryDate(LocalDateTime.now().plusHours(24));
        verifyTokenRepository.save(verifyToken);

        // Construct verify email link using the injected frontendBaseUrl
        String verifyLink = frontendBaseUrl + "/verify-email?token=" + token;

        // Sending the email verification link
        emailService.sendVerificationEmail(savedUser.getEmail(), verifyLink);

    }

    @Override
    public AuthResponse signIn(LoginRequest loginRequest) {

        String email = loginRequest.getEmail();
        String password = loginRequest.getPassword();

        User existingUser = userRepository.findByEmail(loginRequest.getEmail());

        if (!existingUser.isVerified()) {
            throw new UserNotVerifiedException("Authentication failed. User is not verified with email: " + email);
        }

        Authentication authentication = authenticate(email, password);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        return new AuthResponse(
                jwtTokenProvider.generateToken(authentication),
                existingUser.getRoles().iterator().next().getName(),
                true
        );

    }

    private Authentication authenticate(String email, String password) {

        UserDetails userDetails = customUserDetails.loadUserByUsername(email);
        if (userDetails == null) {
            throw new BadCredentialsException("Authentication failed. User not found with email: " + email);
        }
        if (!passwordEncoder.matches(password, userDetails.getPassword())) {
            throw new BadCredentialsException("Authentication failed. Password is wrong!.");
        }
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

    }

    @Override
    public void verifyAccount(String token) {

        // Finding the provided token from DB
        VerifyToken verificationToken = verifyTokenRepository.findByToken(token);

        // Handling the exception when no token found
        if (verificationToken == null) {
            throw new InvalidTokenException("Token not found.");
        }

        // Handling the exception when token expired
        if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new TokenExpiredException("Token got expired.");
        }

        // Marking the user as verified and deleting the token from DB
        User user = verificationToken.getUser();
        user.setVerified(true);
        userRepository.save(user);
        verifyTokenRepository.delete(verificationToken);

    }

    @Override
    public void sendResetPasswordEmail(String email, HttpServletRequest request) throws UserException, MessagingException {

        // Check if user exists
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new UserException("No account found with this email!");
        }

        // Generate reset token
        String token = UUID.randomUUID().toString();
        VerifyToken resetToken = new VerifyToken();
        resetToken.setToken(token);
        resetToken.setExpiryDate(LocalDateTime.now().plusHours(1));
        resetToken.setUser(user);
        verifyTokenRepository.save(resetToken);

        // Construct reset password link using the injected frontendBaseUrl
        String resetLink = frontendBaseUrl + "/reset-password?token=" + token;

        // Send reset password email
        emailService.sendResetPasswordEmail(user.getEmail(), resetLink);

    }

    @Override
    public void resetPassword(String token, String newPassword) {

        // Find token in DB
        VerifyToken resetToken = verifyTokenRepository.findByToken(token);
        if (resetToken == null) {
            throw new InvalidTokenException("Invalid password reset token.");
        }

        // Check token expiry
        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new TokenExpiredException("Password reset token has expired.");
        }

        // Reset password
        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Delete the token
        verifyTokenRepository.delete(resetToken);

    }

}
