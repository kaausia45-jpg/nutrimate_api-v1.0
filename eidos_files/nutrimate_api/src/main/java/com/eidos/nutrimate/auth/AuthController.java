package com.eidos.nutrimate.auth;

import com.eidos.nutrimate.security.JwtTokenProvider;
import com.eidos.nutrimate.user.User;
// Assume UserService and UserRepository exist for DB operations
// import com.eidos.nutrimate.user.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    // These would be injected. For this example, we'll assume they exist.
    // private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthController(PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider) {
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest request) {
        // In a real app, you would check if the user already exists
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        
        // In a real app: User savedUser = userService.save(user);
        // For now, we simulate success
        System.out.println("Simulating user registration for: " + user.getEmail());

        return ResponseEntity.ok("User registered successfully!");
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest request) {
        // In a real app, you'd find the user by email and check the password
        // User user = userService.findByEmail(request.getEmail());
        // if (user != null && passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            // For demonstration, we'll generate a token assuming credentials are valid
            String token = jwtTokenProvider.createAccessToken("simulated-user-id", request.getEmail());
            return ResponseEntity.ok(Map.of("accessToken", token));
        // } else {
        //     return ResponseEntity.status(401).body("Invalid credentials");
        // }
    }

    // DTOs for request bodies
    static class RegisterRequest {
        private String username; private String email; private String password;
        // getters & setters
        public String getUsername() { return username; } public void setUsername(String u) { this.username = u; }
        public String getEmail() { return email; } public void setEmail(String e) { this.email = e; }
        public String getPassword() { return password; } public void setPassword(String p) { this.password = p; }
    }

    static class LoginRequest {
        private String email; private String password;
        // getters & setters
        public String getEmail() { return email; } public void setEmail(String e) { this.email = e; }
        public String getPassword() { return password; } public void setPassword(String p) { this.password = p; }
    }
}
