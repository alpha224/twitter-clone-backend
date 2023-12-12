package com.twitterclone.demo.auth

import com.twitterclone.demo.repo.UsersRepo
import com.twitterclone.demo.service.AuthService
import groovy.util.logging.Slf4j
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
@Slf4j
class JwtTokenFilter extends OncePerRequestFilter {

    private final UsersRepo usersRepo
    private final AuthService authService

    JwtTokenFilter(UsersRepo usersRepo, AuthService authService) {
        this.usersRepo = usersRepo
        this.authService = authService
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String path = request.getRequestURI()

        if (isPublicPath(path)) {
            filterChain.doFilter(request, response)
            return
        }

        String header = request.getHeader(HttpHeaders.AUTHORIZATION)
        if (header == null || !header.startsWith("Bearer ")) {
            denyAccess(response, "No 'Bearer' token. Who do you think you are?", 403)
            return
        }

        String token = header.substring(7).trim()
        if (!JwtTokenUtils.isTokenValid(token)) {
            denyAccess(response, "Invalid token. Try again, but better.", 403)
            return
        }

        if (!authService.existsInCache(token)) {
            denyAccess(response, "Unauthorized", 401)
            return
        }

        UserDetails userDetails = usersRepo.findByUsername(JwtTokenUtils.getUsernameFromToken(token))
                .orElseThrow(() -> new UsernameNotFoundException("User not found. Are you a ghost?"))

        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request))
        SecurityContextHolder.getContext().setAuthentication(authentication)

        filterChain.doFilter(request, response)
    }

    private static boolean isPublicPath(String path) {
        List<String> publicPaths = Arrays.asList("/registration", "/login")
        return publicPaths.contains(path)
    }

    private static void denyAccess(HttpServletResponse response, String message, int status) throws IOException {
        response.setStatus(status)
        response.setContentType("application/json")
        response.getWriter().write("{\"error\": \"$message\"}")
        log.warn(message)
    }
}
