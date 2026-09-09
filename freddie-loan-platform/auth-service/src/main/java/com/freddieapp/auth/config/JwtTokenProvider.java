package com.freddieapp.auth.config;

import com.freddieapp.auth.dto.UserDto;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secret:FreddieMacOAuth2SuperSecretKeyForJWTTokenGeneration2026Secure}")
    private String secretKey;

    @Value("${jwt.expiration:86400000}")
    private long jwtExpirationInMs;

    @Value("${jwt.issuer:http://localhost:8080/realms/freddie-platform}")
    private String issuer;

    public String generateToken(UserDto user) {
        try {
            Date now = new Date();
            Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

            Map<String, Object> realmAccess = new HashMap<>();
            realmAccess.put("roles", user.getRoles());

            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                    .subject(user.getUsername())
                    .issuer(issuer)
                    .issueTime(now)
                    .expirationTime(expiryDate)
                    .claim("preferred_username", user.getUsername())
                    .claim("email", user.getEmail())
                    .claim("name", user.getFullName())
                    .claim("user_id", user.getId())
                    .claim("roles", user.getRoles())
                    .claim("realm_access", realmAccess)
                    .build();

            JWSSigner signer = new MACSigner(secretKey.getBytes(StandardCharsets.UTF_8));
            SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet);
            signedJWT.sign(signer);

            return signedJWT.serialize();
        } catch (Exception e) {
            throw new RuntimeException("Error generating JWT token", e);
        }
    }

    public boolean validateToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            JWSVerifier verifier = new MACVerifier(secretKey.getBytes(StandardCharsets.UTF_8));
            if (!signedJWT.verify(verifier)) {
                return false;
            }
            Date expiration = signedJWT.getJWTClaimsSet().getExpirationTime();
            return expiration != null && expiration.after(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    public String getUsernameFromToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            return signedJWT.getJWTClaimsSet().getSubject();
        } catch (Exception e) {
            throw new RuntimeException("Could not extract username from token", e);
        }
    }

    @SuppressWarnings("unchecked")
    public List<String> getRolesFromToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            Object rolesObj = signedJWT.getJWTClaimsSet().getClaim("roles");
            if (rolesObj instanceof List) {
                return (List<String>) rolesObj;
            }
            return List.of();
        } catch (Exception e) {
            return List.of();
        }
    }

    public long getExpirationInMs() {
        return jwtExpirationInMs;
    }
}
