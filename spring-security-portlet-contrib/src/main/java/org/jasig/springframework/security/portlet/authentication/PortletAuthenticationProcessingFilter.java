/**
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.springframework.security.portlet.authentication;

import java.io.IOException;
import java.security.Principal;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.portlet.PortletSession;

import org.jasig.springframework.security.portlet.util.AuthenticationValidator;
import org.jasig.springframework.web.portlet.filter.GenericPortletFilterBean;
import org.jasig.springframework.web.portlet.filter.PortletFilterUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.util.Assert;

/**
 * Based on {@link org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter} and the PortletProcessingInterceptor from
 * Spring Security 2.0. The {@link org.springframework.security.core.Authentication} implementation used is {@link org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken}
 * as all portlet requests are pre-authenticated by the container.
 *
 * <p>This filter is responsible for processing portlet authentication requests.  This
 * is the portlet equivalent of the AbstractPreAuthenticatedProcessingFilter used for
 * traditional servlet-based web applications. It is applied to all portlet lifecycle requests.
 * If authentication is successful, the resulting {@link org.springframework.security.core.Authentication} object will be placed
 * into the SecurityContext, which is guaranteed to have already been created by an
 * earlier filter.
 *
 *  <p>Some portals do not properly provide the identity of the current user via the
 * getRemoteUser() or getUserPrincipal() methods of the
 * PortletRequest.  In these cases they sometimes make it available in the
 * USER_INFO map provided as one of the attributes of the request.  If this is
 * the case in your portal, you can specify a list of USER_INFO attributes
 * to check for the username via the userNameAttributes property of this bean.</p>
 *
 * <p>This filter will put the PortletRequest object into the
 * details property of the Authentication object that is sent
 * as a request to the AuthenticationManager.
 * <p>
 * The purpose is then only to extract the necessary information on the principal from the incoming request, rather
 * than to authenticate them.
 *
 * <p>
 * If the security context already contains an {@code Authentication} object (either from a invocation of the
 * filter or because of some other authentication mechanism), the filter will do nothing by default. You can force
 * it to check for a change in the principal by setting the {@link #setCheckForPrincipalChanges(boolean)
 * checkForPrincipalChanges} property.
 * <p>
 * By default, the filter chain will proceed when an authentication attempt fails in order to allow other
 * authentication mechanisms to process the request. To reject the credentials immediately, set the
 * <tt>continueFilterChainOnUnsuccessfulAuthentication</tt> flag to false. The exception raised by the
 * <tt>AuthenticationManager</tt> will the be re-thrown. Note that this will not affect cases where the principal
 * returned by {@link #getPreAuthenticatedPrincipal} is null, when the chain will still proceed as normal.
 *
 * @author Eric Dalquist
 * @since 2.0
 * @version $Id: $Id
 */
public class PortletAuthenticationProcessingFilter
        extends GenericPortletFilterBean
        implements ApplicationEventPublisherAware {

    private ApplicationEventPublisher eventPublisher = null;
    private AuthenticationDetailsSource<PortletRequest, ?> authenticationDetailsSource;
    private AuthenticationManager authenticationManager = null;
    private AuthenticationValidator authenticationValidator =null;
    private boolean continueFilterChainOnUnsuccessfulAuthentication = true;
    private boolean checkForPrincipalChanges;
    private boolean invalidateSessionOnPrincipalChange = true;

    private List<String> userNameAttributes;
    private boolean useAuthTypeAsCredentials = false;

    /**
     * {@inheritDoc}
     *
     * Check whether all required properties have been set.
     */
    @Override
    public void afterPropertiesSet() {
        Assert.notNull(authenticationManager, "An AuthenticationManager must be set");
        Assert.notNull(authenticationDetailsSource, "An AuthenticationDetailsSource must be set");
    }

    /**
     * {@inheritDoc}
     *
     * Try to authenticate a pre-authenticated user with Spring Security if the user has not yet been authenticated.
     */
    @Override
    protected void doCommonFilter(PortletRequest request, PortletResponse response,
            javax.portlet.filter.FilterChain chain) throws IOException, PortletException {

        if (logger.isDebugEnabled()) {
            logger.debug("Checking secure context token: " + SecurityContextHolder.getContext().getAuthentication());
        }

        if (requiresAuthentication((PortletRequest) request)) {
            doAuthenticate((PortletRequest) request, (PortletResponse) response);
        }

        PortletFilterUtils.doFilter(request, response, chain);
    }

    /**
    * Do the actual authentication for a pre-authenticated user.
    */
    private void doAuthenticate(PortletRequest request, PortletResponse response) {
        Authentication authResult;

        Object principal = getPreAuthenticatedPrincipal(request);
        Object credentials = getPreAuthenticatedCredentials(request);

        if (principal == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("No pre-authenticated principal found in request");
            }

            return;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("preAuthenticatedPrincipal = " + principal + ", trying to authenticate");
        }

        try {
            PreAuthenticatedAuthenticationToken authRequest = new PreAuthenticatedAuthenticationToken(principal,
                    credentials);
            authRequest.setDetails(authenticationDetailsSource.buildDetails(request));
            authResult = authenticationManager.authenticate(authRequest);
            successfulAuthentication(request, response, authResult);
        }
        catch (AuthenticationException failed) {
            unsuccessfulAuthentication(request, response, failed);

            if (!continueFilterChainOnUnsuccessfulAuthentication) {
                throw failed;
            }
        }
    }

    private boolean requiresAuthentication(PortletRequest request) {
        Authentication currentUser = SecurityContextHolder.getContext().getAuthentication();

        if (currentUser == null) {
            return true;
        }

        if (!checkForPrincipalChanges) {
            return false;
        }

        Object principal = getPreAuthenticatedPrincipal(request);

        if (currentUser.getName().equals(principal)) {
            return (authenticationValidator != null && !authenticationValidator.validate(currentUser, request));
        } else {

	        logger.debug("Pre-authenticated principal has changed to " + principal + " and will be reauthenticated");

	        if (invalidateSessionOnPrincipalChange) {
	            PortletSession session = request.getPortletSession(false);

	            if (session != null) {
	                logger.debug("Invalidating existing session");
	                session.invalidate();
	                request.getPortletSession();
	            }
	        }
        }

        return true;
    }

    /**
     * Puts the Authentication instance returned by the
     * authentication manager into the secure context.
     *
     * @param request a {@link javax.portlet.PortletRequest} object.
     * @param response a {@link javax.portlet.PortletResponse} object.
     * @param authResult a {@link org.springframework.security.core.Authentication} object.
     */
    protected void successfulAuthentication(PortletRequest request, PortletResponse response,
            Authentication authResult) {
        if (logger.isDebugEnabled()) {
            logger.debug("Authentication success: " + authResult);
        }
        SecurityContextHolder.getContext().setAuthentication(authResult);
        // Fire event
        if (this.eventPublisher != null) {
            eventPublisher.publishEvent(new InteractiveAuthenticationSuccessEvent(authResult, this.getClass()));
        }
    }

    /**
     * Ensures the authentication object in the secure context is set to null when authentication fails.
     * <p>
     * Caches the failure exception as a request attribute
     *
     * @param request a {@link javax.portlet.PortletRequest} object.
     * @param response a {@link javax.portlet.PortletResponse} object.
     * @param failed a {@link org.springframework.security.core.AuthenticationException} object.
     */
    protected void unsuccessfulAuthentication(PortletRequest request, PortletResponse response,
            AuthenticationException failed) {
        SecurityContextHolder.clearContext();

        if (logger.isDebugEnabled()) {
            logger.debug("Cleared security context due to exception", failed);
        }
        request.setAttribute(WebAttributes.AUTHENTICATION_EXCEPTION, failed);
    }

    /** {@inheritDoc} */
    public void setApplicationEventPublisher(ApplicationEventPublisher anApplicationEventPublisher) {
        this.eventPublisher = anApplicationEventPublisher;
    }

    /**
     * <p>Setter for the field authenticationDetailsSource.</p>
     *
     * @param authenticationDetailsSource
     *            The AuthenticationDetailsSource to use
     */
    public void setAuthenticationDetailsSource(
            AuthenticationDetailsSource<PortletRequest, ?> authenticationDetailsSource) {
        Assert.notNull(authenticationDetailsSource, "AuthenticationDetailsSource required");
        this.authenticationDetailsSource = authenticationDetailsSource;
    }

    /**
     * <p>Getter for the field authenticationDetailsSource.</p>
     *
     * @return a {@link org.springframework.security.authentication.AuthenticationDetailsSource} object.
     */
    protected AuthenticationDetailsSource<PortletRequest, ?> getAuthenticationDetailsSource() {
        return authenticationDetailsSource;
    }

    /**
     * <p>Setter for the field authenticationManager.</p>
     *
     * @param authenticationManager
     *            The AuthenticationManager to use
     */
    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    /**
     * If set to {@code true}, any {@code AuthenticationException} raised by the {@code AuthenticationManager} will be
     * swallowed, and the request will be allowed to proceed, potentially using alternative authentication mechanisms.
     * If {@code false} (the default), authentication failure will result in an immediate exception.
     *
     * @param shouldContinue set to {@code true} to allow the request to proceed after a failed authentication.
     */
    public void setContinueFilterChainOnUnsuccessfulAuthentication(boolean shouldContinue) {
        continueFilterChainOnUnsuccessfulAuthentication = shouldContinue;
    }

    /**
     * If set, the pre-authenticated principal will be checked on each request and compared
     * against the name of the current <tt>Authentication</tt> object. If a change is detected,
     * the user will be reauthenticated.
     *
     * @param checkForPrincipalChanges a boolean.
     */
    public void setCheckForPrincipalChanges(boolean checkForPrincipalChanges) {
        this.checkForPrincipalChanges = checkForPrincipalChanges;
    }

    /**
     * If <tt>checkForPrincipalChanges</tt> is set, and a change of principal is detected, determines whether
     * any existing session should be invalidated before proceeding to authenticate the new principal.
     *
     * @param invalidateSessionOnPrincipalChange <tt>false</tt> to retain the existing session. Defaults to <tt>true</tt>.
     */
    public void setInvalidateSessionOnPrincipalChange(boolean invalidateSessionOnPrincipalChange) {
        this.invalidateSessionOnPrincipalChange = invalidateSessionOnPrincipalChange;
    }

    /**
     * Extracts the principal information by checking the following in order:
     * {@link javax.portlet.PortletRequest#getRemoteUser()}
     * {@link javax.portlet.PortletRequest#getUserPrincipal()}
     * {@link javax.portlet.PortletRequest#USER_INFO}
     *
     * Returns null if no principal is found
     *
     * @param request a {@link javax.portlet.PortletRequest} object.
     * @return a {@link java.lang.Object} object.
     */
    @SuppressWarnings("unchecked")
    protected Object getPreAuthenticatedPrincipal(PortletRequest request) {

        // first try getRemoteUser()
        String remoteUser = request.getRemoteUser();
        if (remoteUser != null) {
            return remoteUser;
        }

        // next try getUserPrincipal()
        Principal userPrincipal = request.getUserPrincipal();
        if (userPrincipal != null) {
            String userPrincipalName = userPrincipal.getName();
            if (userPrincipalName != null) {
                return userPrincipalName;
            }
        }

        // last try entries in USER_INFO if any attributes were defined
        if (this.userNameAttributes != null) {
            Map<String, String> userInfo = null;
            try {
                userInfo = (Map<String, String>)request.getAttribute(PortletRequest.USER_INFO);
            } catch (Exception e) {
                logger.warn("unable to retrieve USER_INFO map from portlet request", e);
            }
            if (userInfo != null) {
                Iterator<String> i = this.userNameAttributes.iterator();
                while(i.hasNext()) {
                    Object principal = (String)userInfo.get(i.next());
                    if (principal != null) {
                        return principal;
                    }
                }
            }
        }

        // none found so return null
        return null;
    }

    /**
     * If {@link #setUseAuthTypeAsCredentials(boolean)} is true then {@link javax.portlet.PortletRequest#getAuthType()} is used
     * otherwise a dummy value is returned.
     *
     * @param request a {@link javax.portlet.PortletRequest} object.
     * @return a {@link java.lang.Object} object.
     */
    protected Object getPreAuthenticatedCredentials(PortletRequest request) {
        if (useAuthTypeAsCredentials) {
            return request.getAuthType();
        }

        return "N/A";
    }

    /**
     * The user attributes from the {@link javax.portlet.PortletRequest#USER_INFO} map to try and use as the userName for
     * portals that don't support the {@link javax.portlet.PortletRequest#getRemoteUser()} or {@link javax.portlet.PortletRequest#getUserPrincipal()}
     * methods.
     *
     * @param userNameAttributes a {@link java.util.List} object.
     */
    public void setUserNameAttributes(List<String> userNameAttributes) {
        this.userNameAttributes = userNameAttributes;
    }

    /**
     * It true, the "authType" proerty of the <tt>PortletRequest</tt> will be used as the credentials.
     * Defaults to false.
     *
     * @param useAuthTypeAsCredentials a boolean.
     */
    public void setUseAuthTypeAsCredentials(boolean useAuthTypeAsCredentials) {
        this.useAuthTypeAsCredentials = useAuthTypeAsCredentials;
    }

    /**
     * <p>Setter for the field authenticationValidator.</p>
     *
     * @param value a {@link org.jasig.springframework.security.portlet.util.AuthenticationValidator} object.
     */
    public void setAuthenticationValidator(AuthenticationValidator value) {
    	this.authenticationValidator = value;
    }
}
