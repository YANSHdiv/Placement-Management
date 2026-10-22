package edu.jecrc.portal.security;

import com.github.benmanes.caffeine.cache.Cache;
import edu.jecrc.portal.modules.user.entity.User;
import edu.jecrc.portal.modules.user.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    private final UserRepository userRepository;
    private final Cache<UUID, CaffeineCacheConfig.UserSecuritySnapshot> userSecurityCache;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt) && !jwtService.isTokenExpired(jwt)) {
                UUID userId = jwtService.extractUserId(jwt);
                UUID tokenStamp = jwtService.extractSecurityStamp(jwt);

                // Check in-memory high-speed cache or fallback to DB for instantaneous revocation
                CaffeineCacheConfig.UserSecuritySnapshot snapshot = userSecurityCache.get(userId, id -> {
                    return userRepository.findById(id)
                            .map(u -> new CaffeineCacheConfig.UserSecuritySnapshot(
                                    u.getSecurityStamp(),
                                    Boolean.TRUE.equals(u.getIsActive()),
                                    Boolean.TRUE.equals(u.getIsSuspended())))
                            .orElse(null);
                });

                if (snapshot != null && snapshot.isActive() && !snapshot.isSuspended()) {
                    if (tokenStamp != null && tokenStamp.equals(snapshot.getSecurityStamp())) {
                        UserDetails userDetails = userDetailsService.loadUserById(userId);
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    } else {
                        log.warn("Security stamp mismatch for user {}. Token invalidated.", userId);
                    }
                } else {
                    log.warn("User {} is suspended or inactive. Rejecting token.", userId);
                }
            }
        } catch (Exception ex) {
            log.debug("Could not authenticate user from JWT: {}", ex.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
