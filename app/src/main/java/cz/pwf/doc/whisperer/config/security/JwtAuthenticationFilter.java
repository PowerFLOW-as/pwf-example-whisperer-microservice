package cz.pwf.whisperer.config.security;

import com.sun.security.auth.UserPrincipal;
import cz.pwf.whisperer.config.Constants;
import cz.pwf.whisperer.model.pwf.TokenVerificationInfo;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Třída zajišťuje verifikaci JWT tokenu vůči backendu systému PowerFLOW. V případě validního tokenu dochází k vygenerování
 * tokenu s technickým uživatelem, který dále komunikuje s REST API rozhraním DWH.
 */
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final RestTemplate restTemplate;
    private final URL tokenVerificationEndpointUrl;
    private final String ldapKpjmAttributeName;


    @SneakyThrows
    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) {
        String authorizationHeader = httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION);

        if (authorizationHeaderIsInvalid(authorizationHeader)) {
            filterChain.doFilter(httpServletRequest, httpServletResponse);
            return;
        }

        MutableHttpServletRequest mutableRequest = new MutableHttpServletRequest(httpServletRequest);
        createTechToken(authorizationHeader, mutableRequest)
                .ifPresent(token -> SecurityContextHolder.getContext().setAuthentication(token));

        filterChain.doFilter(mutableRequest, httpServletResponse);
    }

    private boolean authorizationHeaderIsInvalid(String authorizationHeader) {
        return !StringUtils.hasText(authorizationHeader) || !authorizationHeader.startsWith(Constants.BEARER_PREFIX);
    }

    @SneakyThrows
    private Optional<TokenVerificationInfo> verifyJwtToken(String jwtToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, jwtToken);
        HttpEntity<String> entity = new HttpEntity<>("parameters", headers);

        return Optional.ofNullable(restTemplate.exchange(tokenVerificationEndpointUrl.toURI(), HttpMethod.GET, entity, TokenVerificationInfo.class).getBody());
    }

    private Optional<UsernamePasswordAuthenticationToken> createTechToken(String jwtToken, MutableHttpServletRequest mutableRequest) {

        Optional<TokenVerificationInfo> tokenVerificationInfoOptional = verifyJwtToken(jwtToken);

        if (tokenVerificationInfoOptional.isPresent()) {
            TokenVerificationInfo tokenVerificationInfo = tokenVerificationInfoOptional.get();
            if (tokenVerificationInfo.isValid()) {
                mutableRequest.putHeader(Constants.KPJM_HEADER_NAME, (String) tokenVerificationInfo.getUser().get(ldapKpjmAttributeName));
                return Optional.of(new UsernamePasswordAuthenticationToken(new UserPrincipal(Constants.TECH_USER_NAME), null,
                        Collections.singletonList(new SimpleGrantedAuthority(Constants.TECH_USER_ROLE))));
            }
        }

        return Optional.empty();
    }

    /**
     * Tato třída umžňuje modifikovat request headers.
     */
    private final static class MutableHttpServletRequest extends HttpServletRequestWrapper {
        private final Map<String, String> customHeaders;

        public MutableHttpServletRequest(HttpServletRequest request){
            super(request);
            this.customHeaders = new HashMap<>();
        }

        public void putHeader(String name, String value){
            this.customHeaders.put(name, value);
        }

        @Override
        public String getHeader(String name) {
            String headerValue = customHeaders.get(name);

            if (headerValue != null){
                return headerValue;
            }

            return ((HttpServletRequest) getRequest()).getHeader(name);
        }

        @Override
        public Enumeration<String> getHeaderNames() {
            Set<String> set = new HashSet<>(customHeaders.keySet());

            @SuppressWarnings("unchecked")
            Enumeration<String> e = ((HttpServletRequest) getRequest()).getHeaderNames();
            while (e.hasMoreElements()) {
                String n = e.nextElement();
                set.add(n);
            }

            return Collections.enumeration(set);
        }
    }
}