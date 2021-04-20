package org.apereo.cas.authentication;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.model.support.mfa.BaseMultifactorAuthenticationProviderProperties;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.validation.Assertion;
import org.apereo.cas.validation.AuthenticationContextValidationResult;
import org.apereo.cas.validation.RequestedAuthenticationContextValidator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.ApplicationContext;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

/**
 * This is {@link DefaultRequestedAuthenticationContextValidator}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class DefaultRequestedAuthenticationContextValidator implements RequestedAuthenticationContextValidator {
    private final ServicesManager servicesManager;

    private final MultifactorAuthenticationTriggerSelectionStrategy multifactorTriggerSelectionStrategy;

    private final MultifactorAuthenticationContextValidator authenticationContextValidator;

    private final ApplicationContext applicationContext;

    private static AuthenticationContextValidationResult toSuccessfulResult() {
        return AuthenticationContextValidationResult.builder().success(true).build();
    }

    @Override
    public AuthenticationContextValidationResult validateAuthenticationContext(final Assertion assertion, final HttpServletRequest request) {
        LOGGER.trace("Locating the primary authentication associated with this service request [{}]", assertion.getService());
        val registeredService = servicesManager.findServiceBy(assertion.getService());
        val authentication = assertion.getPrimaryAuthentication();
        return validateAuthenticationContext(request, registeredService, authentication, assertion.getService());
    }

    @Override
    public AuthenticationContextValidationResult validateAuthenticationContext(final HttpServletRequest request,
                                                                               final RegisteredService registeredService,
                                                                               final Authentication authentication,
                                                                               final Service service) {
        val requestedContext = multifactorTriggerSelectionStrategy.resolve(request, registeredService, authentication, service);
        if (requestedContext.isEmpty()) {
            LOGGER.debug("No authentication context is required for this request");
            return toSuccessfulResult();
        }

        val providerId = requestedContext.get();
        val providerResult = MultifactorAuthenticationUtils.getMultifactorAuthenticationProviderById(providerId, applicationContext);

        if (providerResult.isPresent()) {
            val provider = providerResult.get();
            if (provider.isAvailable(registeredService)) {
                val bypassEvaluator = provider.getBypassEvaluator();
                if (bypassEvaluator != null) {
                    if (!bypassEvaluator.shouldMultifactorAuthenticationProviderExecute(authentication, registeredService, provider, request)) {
                        LOGGER.debug("MFA provider [{}] should be bypassed for this service request [{}]", providerId, service);
                        bypassEvaluator.rememberBypass(authentication, provider);
                        return toSuccessfulResult();
                    }
                    if (bypassEvaluator.isMultifactorAuthenticationBypassed(authentication, providerId)) {
                        LOGGER.debug("Authentication attempt indicates that MFA is bypassed for this request for [{}]", requestedContext);
                        bypassEvaluator.rememberBypass(authentication, provider);
                        return toSuccessfulResult();
                    }
                }
            } else {
                val failure = provider.getFailureModeEvaluator().evaluate(registeredService, provider);
                if (failure != BaseMultifactorAuthenticationProviderProperties.MultifactorAuthenticationProviderFailureModes.CLOSED) {
                    return toSuccessfulResult();
                }
            }
        }
        val result = authenticationContextValidator.validate(authentication, providerId, Optional.ofNullable(registeredService));
        return AuthenticationContextValidationResult.builder()
            .success(result.isSuccess())
            .providerId(result.getProvider().map(MultifactorAuthenticationProvider::getId))
            .build();
    }
}
