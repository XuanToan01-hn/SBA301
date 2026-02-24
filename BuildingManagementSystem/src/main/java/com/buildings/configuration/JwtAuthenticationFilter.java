package com.buildings.configuration;//package com.buildings.configuration;
//
//import jakarta.annotation.Nonnull;
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
//import org.springframework.stereotype.Component;
//import org.springframework.web.filter.OncePerRequestFilter;
//import org.springframework.web.servlet.HandlerExceptionResolver;
//
//import java.io.IOException;
//
//@Component
//public class JwtAuthenticationFilter extends OncePerRequestFilter {
//
//    private final HandlerExceptionResolver handlerExceptionResolver;
//    private final JwtTokenProvider jwtTokenProvider;
//    private final UserDetailsService userDetailsService;
//
//    public JwtAuthenticationFilter(
//            HandlerExceptionResolver handlerExceptionResolver,
//            JwtTokenProvider jwtTokenProvider,
//            UserDetailsService userDetailsService
//    ) {
//        this.handlerExceptionResolver = handlerExceptionResolver;
//        this.jwtTokenProvider = jwtTokenProvider;
//        this.userDetailsService = userDetailsService;
//    }
//
//    @Override
//    protected void doFilterInternal(
//            @Nonnull HttpServletRequest request,
//            @Nonnull HttpServletResponse response,
//            @Nonnull FilterChain filterChain
//    ) throws ServletException, IOException {
//
//        try {
//
//            final String authHeader = request.getHeader("Authorization");
//            String path = request.getServletPath();
//            // Bỏ qua auth endpoint
//            if (path.startsWith("/api/auth/")) {
//                filterChain.doFilter(request, response);
//                return;
//            }
//            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
//                filterChain.doFilter(request, response);
//                return;
//            }
//            String token = authHeader.substring(7);
//
//            if (jwtTokenProvider.validateToken(token)) {
//
//                String userEmail = jwtTokenProvider.getUsernameFromToken(token);
//
//                if (userEmail != null &&
//                        SecurityContextHolder.getContext().getAuthentication() == null) {
//
//                    UserDetails userDetails =
//                            userDetailsService.loadUserByUsername(userEmail);
//
//                    UsernamePasswordAuthenticationToken authentication =
//                            new UsernamePasswordAuthenticationToken(
//                                    userDetails,
//                                    null,
//                                    userDetails.getAuthorities()
//                            );
//
//                    authentication.setDetails(
//                            new WebAuthenticationDetailsSource()
//                                    .buildDetails(request)
//                    );
//
//                    SecurityContextHolder.getContext()
//                            .setAuthentication(authentication);
//                }
//            }
//
//            filterChain.doFilter(request, response);
//
//        } catch (Exception e) {
//            handlerExceptionResolver.resolveException(request, response, null, e);
//        }
//    }
//}