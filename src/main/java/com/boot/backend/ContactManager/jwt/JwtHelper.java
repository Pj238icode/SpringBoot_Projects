package com.boot.backend.ContactManager.jwt;

import com.boot.backend.ContactManager.entities.User;
import com.boot.backend.ContactManager.repositories.UserRespository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.function.Function;

@Component
public class JwtHelper {

    private final Key signingKey;
    private final long expirationMs;

    @Autowired
    private UserRespository userRepository;

    public JwtHelper(@Value("${app.jwt.secret}") String secret,
                     @Value("${app.jwt.expiration}") long expirationMs) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    /**
     * Generate JWT for user with versioning
     */
    public String generateToken(String userEmail) {
        User user = userRepository.findByEmail(userEmail);
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        return Jwts.builder()
                .setSubject(userEmail)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(signingKey)
                .claim("version", user.getTokenVersion())
                .compact();
    }

    /**
     * Validate token against username and version
     */
    public boolean isTokenValid(String token, String userEmail) {
        if (isTokenExpired(token)) return false;

        String usernameFromToken = extractUsername(token);
        if (!usernameFromToken.equals(userEmail)) return false;

        Integer versionFromToken = extractClaim(token, claims -> claims.get("version", Integer.class));
        User user = userRepository.findByEmail(userEmail);
        if (user == null) return false;

        return versionFromToken != null && versionFromToken.equals(user.getTokenVersion());
    }

    /**
     * Extract username (email) from token
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Check if token has expired
     */
    public boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    /**
     * Generic method to extract any claim
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = parseClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Parse token and return claims
     */
    public Claims parseClaims(String token) {
        return Jwts.parser()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Increment token version for logout-all-devices
     */
    public void logoutAllDevices(String userEmail) {
        User user = userRepository.findByEmail(userEmail);
        if (user != null) {
            user.setTokenVersion(user.getTokenVersion() + 1); // invalidate all previous tokens
            userRepository.save(user);
        }
    }
}
