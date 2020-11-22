package org.apereo.cas.custom.config;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceAuthorizer;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.web.flow.login.GenericSuccessViewAction;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.RequestContext;
import java.util.ArrayList;

/**
 * This is {@link CustomConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Configuration
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CustomConfiguration {
    @Autowired
    @Qualifier("centralAuthenticationService")
    private CentralAuthenticationService centralAuthenticationService;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Autowired
    @Qualifier("webApplicationServiceFactory")
    private ServiceFactory webApplicationServiceFactory;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Bean
    @ConditionalOnMissingBean(name = "genericSuccessViewAction")
    public Action genericSuccessViewAction() {
        return new CustomGenericSuccessViewAction(centralAuthenticationService,
            servicesManager, webApplicationServiceFactory,
            casProperties.getView().getDefaultRedirectUrl());
    }

    public class CustomGenericSuccessViewAction extends GenericSuccessViewAction {
        public CustomGenericSuccessViewAction(final CentralAuthenticationService centralAuthenticationService,
                                              final ServicesManager servicesManager,
                                              final ServiceFactory serviceFactory, final String redirectUrl) {
            super(centralAuthenticationService, servicesManager, serviceFactory, redirectUrl);
        }

        @Override
        protected void doPostExecute(final RequestContext requestContext) throws Exception {
            var authorizedServices = new ArrayList<RegisteredService>();
            var tgt = WebUtils.getTicketGrantingTicketId(requestContext);
            getAuthentication(tgt).ifPresent(authentication -> {
                servicesManager.getAllServices().forEach(registeredService -> {
                    if (RegisteredServiceAuthorizer.isAuthorized(registeredService, authentication)) {
                        authorizedServices.add(registeredService);
                    }
                });
            });
            requestContext.getFlowScope().put("authorizedServices", authorizedServices);
            super.doPostExecute(requestContext);
        }
    }
}
