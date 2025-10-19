package pl.cleankod.web.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.cleankod.model.security.LoginRequest;
import pl.cleankod.model.security.TokenResponse;
import pl.cleankod.security.JwtUtil;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    private final JwtUtil jwtUtil;

    @Value("${auth.username}")
    private String configuredUsername;

    @Value("${auth.password}")
    private String configuredPassword;

    public AuthenticationController(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }
    
    @PostMapping("/token")
    @Operation(
            summary = "Generate JWT token",
            description = "Generates a JWT token for a valid developer user."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "JWT token generated successfully"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    public ResponseEntity<TokenResponse> generateToken(@RequestBody LoginRequest loginRequest) {
        if (configuredUsername.equals(loginRequest.username()) && configuredPassword.equals(loginRequest.password())) {
            String token = jwtUtil.generateToken(loginRequest.username(), "developer");
            TokenResponse response = new TokenResponse(token);
            return ResponseEntity.ok(response);
        }
        throw new RuntimeException("Invalid credentials");
    }
    
}
