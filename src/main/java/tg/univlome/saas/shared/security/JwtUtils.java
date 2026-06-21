package tg.univlome.saas.shared.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Utilitaire pour la gestion des tokens JWT (génération et validation).
 */
@Slf4j
@Component
public class JwtUtils {

    @Value("${jwt.secret:}")
    private String jwtSecret;

    @Value("${jwt.access.expiration:900000}")
    private long jwtExpirationMs;

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Génère un token JWT pour un utilisateur.
     *
     * @param username Le nom d'utilisateur (email).
     * @return Le token JWT.
     */
    public String generateToken(String username) {
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Extrait le nom d'utilisateur depuis un token JWT.
     *
     * @param token Le token JWT.
     * @return Le nom d'utilisateur.
     */
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.getSubject();
    }

    /**
     * Valide la signature et l'expiration d'un token JWT.
     *
     * @param token Le token JWT.
     * @return true si valide, false sinon.
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Token JWT invalide : {}", e.getMessage());
        }
        return false;
    }
}
