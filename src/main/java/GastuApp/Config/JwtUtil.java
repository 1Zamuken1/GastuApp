package GastuApp.Config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration-ms}")
    private Long expirationMs;

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generarToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getKey())
                .compact();
    }

    public boolean validarToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getKey())
                    .build()
                    .parseClaimsJws(token);
            System.out.println("DEBUG JwtUtil: Token validado exitosamente");
            return true;
        } catch (io.jsonwebtoken.ExpiredJwtException ex) {
            System.out.println("DEBUG JwtUtil: Token expirado - " + ex.getMessage());
            return false;
        } catch (io.jsonwebtoken.security.SignatureException ex) {
            System.out.println("DEBUG JwtUtil: Firma inválida - " + ex.getMessage());
            return false;
        } catch (Exception ex) {
            System.out.println(
                    "DEBUG JwtUtil: Error validando token - " + ex.getClass().getName() + ": " + ex.getMessage());
            return false;
        }
    }

    public String getUsernameFromToken(String token) {
        Jws<Claims> jwsClaims = Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token);

        Claims claims = jwsClaims.getBody();

        return claims.getSubject();
    }
}
