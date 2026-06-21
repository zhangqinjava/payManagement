package com.al.auth.security;

import com.al.auth.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class JwtTokenProvider {

    public static final String CLAIM_USER_ID = "uid";
    public static final String CLAIM_DISPLAY_NAME = "displayName";
    public static final String CLAIM_ROLES = "roles";
    public static final String CLAIM_PERMISSIONS = "permissions";
    public static final String CLAIM_TOKEN_TYPE = "type";
    public static final String TYPE_ACCESS = "access";
    public static final String TYPE_REFRESH = "refresh";

    @Autowired
    private JwtProperties jwtProperties;

    public TokenPair createTokenPair(LoginUser user) {
        long now = System.currentTimeMillis();
        long accessExpire = now + jwtProperties.getAccessTokenMinutes() * 60L * 1000L;
        long refreshExpire = now + jwtProperties.getRefreshTokenHours() * 3600L * 1000L;
        String refreshTokenId = UUID.randomUUID().toString().replace("-", "");

        String accessToken = buildToken(user, TYPE_ACCESS, refreshTokenId, accessExpire);
        String refreshToken = buildToken(user, TYPE_REFRESH, refreshTokenId, refreshExpire);
        return new TokenPair(accessToken, refreshToken, refreshTokenId, accessExpire, refreshExpire);
    }

    public Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8))
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException e) {
            throw new JwtException("令牌无效或已过期");
        }
    }

    @SuppressWarnings("unchecked")
    public LoginUser toLoginUser(Claims claims) {
        Long userId = Long.valueOf(claims.get(CLAIM_USER_ID).toString());
        String username = claims.getSubject();
        String displayName = claims.get(CLAIM_DISPLAY_NAME, String.class);
        List<String> roles = parseStringList(claims.get(CLAIM_ROLES));
        List<String> permissions = parseStringList(claims.get(CLAIM_PERMISSIONS));
        return new LoginUser(userId, username, "", displayName, true, roles, permissions);
    }

    @SuppressWarnings("unchecked")
    private List<String> parseStringList(Object value) {
        if (value == null) {
            return java.util.Collections.emptyList();
        }
        if (value instanceof List) {
            return ((List<?>) value).stream().map(String::valueOf).collect(Collectors.toList());
        }
        return java.util.Collections.emptyList();
    }

    public String getTokenId(Claims claims) {
        return claims.getId();
    }

    public String getTokenType(Claims claims) {
        return claims.get(CLAIM_TOKEN_TYPE, String.class);
    }

    private String buildToken(LoginUser user, String tokenType, String tokenId, long expireAt) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_USER_ID, user.getUserId());
        claims.put(CLAIM_DISPLAY_NAME, user.getDisplayName());
        claims.put(CLAIM_ROLES, user.getRoles());
        claims.put(CLAIM_PERMISSIONS, user.getPermissions());
        claims.put(CLAIM_TOKEN_TYPE, tokenType);
        return Jwts.builder()
                .setClaims(claims)
                .setId(tokenId)
                .setSubject(user.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(expireAt))
                .signWith(SignatureAlgorithm.HS256, jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8))
                .compact();
    }

    public static class TokenPair {
        private final String accessToken;
        private final String refreshToken;
        private final String refreshTokenId;
        private final long accessExpireAt;
        private final long refreshExpireAt;

        public TokenPair(String accessToken, String refreshToken, String refreshTokenId,
                         long accessExpireAt, long refreshExpireAt) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
            this.refreshTokenId = refreshTokenId;
            this.accessExpireAt = accessExpireAt;
            this.refreshExpireAt = refreshExpireAt;
        }

        public String getAccessToken() {
            return accessToken;
        }

        public String getRefreshToken() {
            return refreshToken;
        }

        public String getRefreshTokenId() {
            return refreshTokenId;
        }

        public long getAccessExpireAt() {
            return accessExpireAt;
        }

        public long getRefreshExpireAt() {
            return refreshExpireAt;
        }
    }
}
