package com.janbabak.noqlbackend.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

//    @Value("${application.security.jwt.secret-key}") // TODO: load from application properties
    private String secretKey = "secretahojsecretahojsecretahojsecretahojsecretahojsecretahojsecretahojsecretahojsecretahojsecretahojsecretahojsecretahojsecretahojsecretahojsecretahojsecretahojsecretahojsecretahojsecretahojsecretahojsecretahojsecretahojsecretahojsecretahojsecretahojsecretahojsecretahojsecretahoj";
//    @Value("${application.security.jwt.expiration}")
    private long jwtExpiration = 1000 * 60 * 60 * 24;
//    @Value("${application.security.jwt.refresh-token.expiration}")
    private long refreshExpiration = 1000 * 60 * 60 * 24 * 30;

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    public String generateToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails
    ) {
        return buildToken(extraClaims, userDetails, jwtExpiration);
    }

    public String generateRefreshToken(
            UserDetails userDetails
    ) {
        return buildToken(new HashMap<>(), userDetails, refreshExpiration);
    }

    private String buildToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails,
            long expiration
    ) {
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey())
                .compact();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}


//import io.jsonwebtoken.Claims;
//import io.jsonwebtoken.Jwts;
//import io.jsonwebtoken.JwtParser;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.stereotype.Service;
//
//import java.util.Date;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.concurrent.TimeUnit;
//import java.security.Key;
//import java.util.function.Function;
//
//import io.jsonwebtoken.security.Keys;
//import io.jsonwebtoken.io.Decoders;
//
//
//
//@Service
//public class JwtService {
//
////    @Value("${application.security.jwt.secret-key}")
//    private String secretKey = "secret";
//
////    @Value("${application.security.jwt.expiration}")
//    private long expiration = TimeUnit.HOURS.toMillis(24);
//
//    public String extractUsername(String token) {
//        return extractClaim(token, Claims::getSubject);
//    }
//
//    public Date extractExpiration(String token) {
//        return extractClaim(token, Claims::getExpiration);
//    }
//
//    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
//        final Claims claims = extractAllClaims(token);
//        return claimsResolver.apply(claims);
//    }
//    private Claims extractAllClaims(String token) {
////        return Jwts.parser()
////                .verifyWith(getSignKey())
////                .build()
////                .parseSignedClaims(token)
////                .getPayload();
//
//        return Jwts.parserBuilder()
//                .setSigningKey(getSignKey())
//                .build()
//                .parseClaimsJws(token)
//                .getBody();
//    }
//
//    public String generateToken(User user) {
//        Map<String, Object> claims = new HashMap<>();
//        claims.put("id", user.getId());
//        claims.put("email", user.getEmail());
//        claims.put("roles", user.getAuthorities());
//        return createToken(claims, user.getUsername());
//    }
//
//    public Boolean validateToken(String token, UserDetails userDetails) {
//        Date expirationDate = extractExpiration(token);
//        if (expirationDate.before(new Date())) {
//            return false;
//        }
//        String username = extractUsername(token);
//        return userDetails.getUsername().equals(username) && !expirationDate.before(new Date());
//    }
//
//    private String createToken(Map<String, Object> claims, String username) {
//        return Jwts.builder()
//                .claims(claims)
//                .subject(username)
//                .issuedAt(new Date(System.currentTimeMillis()))
//                .expiration(new Date(System.currentTimeMillis() + expiration))
//                .signWith(getSignKey())
//                .compact();
//    }
//
//    private Key getSignKey() {
//        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
//        return Keys.hmacShaKeyFor(keyBytes);
//    }
//}
