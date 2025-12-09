package com.tw.coupang.one_payroll.userauth.service;

import com.tw.coupang.one_payroll.userauth.exception.JwtTokenParsingException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static io.jsonwebtoken.Jwts.*;

@Service
public class JwtService
{
    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    public String generateToken(String userId, String employeeId, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("employeeId", employeeId);
        claims.put("role", role);

        return createToken(claims, userId);
    }

    private String createToken(Map<String, Object> claims, String userId)
    {
        long currentTime = System.currentTimeMillis();

        JwtBuilder jwtBuilder = builder()
                .claims(claims)
                .subject(userId)
                .issuedAt(new Date(currentTime))
                .signWith(getSigningKey(), Jwts.SIG.HS256);

        if (jwtExpiration > 0) {
            jwtBuilder.expiration(new Date(currentTime + jwtExpiration));
        }

        return jwtBuilder.compact();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String extractEmployeeId(String token) {
        return extractClaim(token, claims -> claims.get("employeeId", String.class));
    }

    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token)
    {
        try {
            return parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        }catch (Exception e)
        {
            throw new JwtTokenParsingException("Failed to parse JWT token: " + e);
        }
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
        } catch (JwtTokenParsingException e) {
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        Date expiration = extractExpiration(token);
        return expiration != null && expiration.before(new Date());
    }

}
