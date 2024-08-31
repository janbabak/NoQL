package com.janbabak.noqlbackend.authentication;

import org.springframework.security.core.Authentication;

public interface AuthenticationFacadeInterface {

    /**
     * get authentication from spring security context
     * @return authentication
     */
    Authentication getAuthentication();

    /**
     * check if logged user is admin
     * @return true if is admin, otherwise false
     */
    boolean isAdmin();
}
