package com.primehr.security;

import org.springframework.security.core.Authentication;

public interface AgencyScopeResolver {

    String resolveAgencyId(Authentication authentication);
}
