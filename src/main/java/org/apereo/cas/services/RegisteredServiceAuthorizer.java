package org.apereo.cas.services;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.WebApplicationServiceFactory;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

/**
 * This is {@link RegisteredServiceAuthorizer}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@UtilityClass
@Slf4j
public class RegisteredServiceAuthorizer {
    public static boolean isAuthorized(final RegisteredService registeredService, final Authentication authentication) {
        try {
            RegisteredServiceAccessStrategyUtils.ensureServiceAccessIsAllowed(registeredService);
            val service = new WebApplicationServiceFactory().createService(registeredService.getServiceId());
            RegisteredServiceAccessStrategyUtils.ensurePrincipalAccessIsAllowedForService(service, registeredService, authentication);
            return true;
        } catch (final Exception e) {
            log.trace(e.getMessage());
        }
        return false;
    }
}
