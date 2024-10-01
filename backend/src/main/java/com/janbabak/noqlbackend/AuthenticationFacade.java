package com.janbabak.noqlbackend;

import com.janbabak.noqlbackend.authentication.AuthenticationFacadeInterface;
import com.janbabak.noqlbackend.model.Role;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationFacade implements AuthenticationFacadeInterface {

    /**
     * get authentication from spring security context
     * @return authentication
     */
    @Override
    public Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /**
     * check if logged user is admin
     * @return true if is admin, otherwise false
     */
    @Override
    public boolean isAdmin() {
        Authentication auth = getAuthentication();
        return auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals(Role.ROLE_ADMIN.name()));
    }
}
