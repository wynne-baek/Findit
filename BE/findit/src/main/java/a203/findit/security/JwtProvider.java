package a203.findit.security;

import a203.findit.model.entity.auth.RefreshToken;
import a203.findit.model.repository.RefreshTokenRepository;
import io.jsonwebtoken.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtProvider {

    private static final String HEADER_TOKEN_PREFIX = "Bearer ";

    // Logger Setting
    private static final Logger LOGGER = LoggerFactory.getLogger(JwtProvider.class);

    /*
    redis
    private final RedisService redisService;
     */


    @Value("${jwt.secret-key}")
    private String secretKey;

    @Value("${jwt.access-token-expire-time}")
    private long accessTokenExpireTime;

    @Value("${jwt.refresh-token-expire-time}")
    private long refreshTokenExpireTime;

    @Autowired
    private RefreshTokenRepository refreshTokenRepos;

    private MyUserDetailService myUserDetailService;


    public JwtProvider(MyUserDetailService myUserDetailService) {
        this.myUserDetailService = myUserDetailService;
    }


    /**
     * Key Encryption
     */
    @PostConstruct
    protected void init() {
        secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
    }

    /**
     * generate a AccessToken
     */
    public String generateAccessToken(String username) {
        Claims claims = Jwts.claims().setSubject(username);
        Date now = new Date();
        Date expireTime = new Date(now.getTime() + accessTokenExpireTime);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expireTime)
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    /**
     * Generate a RefreshToken
     * @param username
     * @return
     */
    public String generateRefreshToken(String username) {
        Claims claims = Jwts.claims().setSubject(username);
        Date now = new Date();
        Date expireTime = new Date(now.getTime() + refreshTokenExpireTime);

        String refreshToken = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expireTime)
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
        //redisService.setStringValueAndExpire(refreshToken, id, refreshTokenExpireTime);

        refreshTokenRepos.save(RefreshToken.builder().value(refreshToken).build());

        return refreshToken;
    }

    /**
     * accessToken ???????????? ???????????? ????????????.
     * @param token
     * @return
     */
    public Authentication getAuthentication(String token) {
        UserDetails userDetails = myUserDetailService.loadUserByUsername(this.getUsername(token));
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    /**
     * accessToken ???????????? ?????? ???, ?????? ????????? ????????????.
     * @param token
     * @return
     */
    public String getUsername(String token) {
        return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody().getSubject();
    }

    /**
     * ???????????? ????????? ????????????.
     * @param req
     * @return
     */
    public String resolveToken(HttpServletRequest req) {
        String bearerToken = req.getHeader("Authorization");

        if (bearerToken != null && bearerToken.startsWith(HEADER_TOKEN_PREFIX)) {
            return bearerToken.substring(HEADER_TOKEN_PREFIX.length());
        }
        return null;
    }

    /**
     * ????????? ???????????? ????????????.
     * @param token
     * @return
     */
    public boolean validateToken(ServletRequest request, String token) {
        String attrName = "exception";
        try {
            LOGGER.debug("[JwtProvider.validateToken(token)]");
            Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);
            return true;
        } catch (SignatureException e) {
            LOGGER.error("Invalid JWT Signature", e);
            request.setAttribute(attrName, "SignatureException");
            return false;
        } catch (MalformedJwtException e) {
            LOGGER.error("Invalid Jwt token", e);
            request.setAttribute(attrName, "MalformedJwtException");
            return false;
        } catch (ExpiredJwtException e) {
            LOGGER.error("Expired Jwt token", e);
            request.setAttribute(attrName, "ExpiredJwtException");
            return false;
        } catch (UnsupportedJwtException e) {
            LOGGER.error("Unsupported JWT Token", e);
            request.setAttribute(attrName, "UnsupportedJwtException");
            return false;
        } catch (IllegalArgumentException e) {
            LOGGER.error("JWT claims string is empty", e);
            request.setAttribute(attrName, "IllegalArgumentException");
            return false;
        } catch (Exception e) {
            LOGGER.error("JWT validation Fail", e);
            request.setAttribute(attrName, "Exception");
            return false;
        }
    }

    public long getAccessTokenExpireTime() {
        return accessTokenExpireTime;
    }

}
