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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import javax.portlet.PortletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.mapping.Attributes2GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.MappableAttributesRetriever;
import org.springframework.security.core.authority.mapping.SimpleAttributes2GrantedAuthoritiesMapper;
import org.springframework.security.web.authentication.preauth.j2ee.J2eeBasedPreAuthenticatedWebAuthenticationDetailsSource;
import org.springframework.util.Assert;

/**
 * Implementation of AuthenticationDetailsSource which converts the user's Portlet roles (as obtained by calling
 * {@link javax.portlet.PortletRequest#isUserInRole(String)}) into {@code GrantedAuthority}s and stores these in the authentication
 * details object.
 *
 * @author Ruud Senden
 * @author Eric Dalquist
 * @since 2.0
 * @see J2eeBasedPreAuthenticatedWebAuthenticationDetailsSource
 * @version $Id: $Id
 */
public class PortletPreAuthenticatedAuthenticationDetailsSource implements AuthenticationDetailsSource<PortletRequest, PreAuthenticatedGrantedAuthoritiesPortletAuthenticationDetails> {
    protected final Log logger = LogFactory.getLog(getClass());
    /** The role attributes returned by the configured {@code MappableAttributesRetriever} */
    protected Set<String> portletMappableRoles;
    protected Attributes2GrantedAuthoritiesMapper portletUserRoles2GrantedAuthoritiesMapper =
        new SimpleAttributes2GrantedAuthoritiesMapper();

    /**
     * Check that all required properties have been set.
     *
     * @throws java.lang.Exception if any.
     */
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(portletMappableRoles, "No mappable roles available");
        Assert.notNull(portletUserRoles2GrantedAuthoritiesMapper, "Roles to granted authorities mapper not set");
    }

    /**
     * Obtains the list of user roles based on the current user's Portlet roles. The
     * {@link javax.portlet.PortletRequest#isUserInRole(String)} method is called for each of the values
     * in the {@code portletMappableRoles} set to determine if that role should be assigned to the user.
     *
     * @param request the request which should be used to extract the user's roles.
     * @return The subset of {@code portletMappableRoles} which applies to the current user making the request.
     */
    protected Collection<String> getUserRoles(PortletRequest request) {
        ArrayList<String> portletUserRolesList = new ArrayList<String>();

        for (String role : portletMappableRoles) {
            if (request.isUserInRole(role)) {
                portletUserRolesList.add(role);
            }
        }

        return portletUserRolesList;
    }

    /**
     * Builds the authentication details object.
     *
     * @see org.springframework.security.authentication.AuthenticationDetailsSource#buildDetails(Object)
     * @param context a {@link javax.portlet.PortletRequest} object.
     * @return a {@link org.jasig.springframework.security.portlet.authentication.PreAuthenticatedGrantedAuthoritiesPortletAuthenticationDetails} object.
     */
    public PreAuthenticatedGrantedAuthoritiesPortletAuthenticationDetails buildDetails(PortletRequest context) {

        Collection<? extends GrantedAuthority> userGas = buildGrantedAuthorities(context);

        PreAuthenticatedGrantedAuthoritiesPortletAuthenticationDetails result =
                new PreAuthenticatedGrantedAuthoritiesPortletAuthenticationDetails(context, userGas);

        return result;
    }

    /**
     * <p>buildGrantedAuthorities.</p>
     *
     * @param context a {@link javax.portlet.PortletRequest} object.
     * @return a {@link java.util.Collection} object.
     */
    protected Collection<? extends GrantedAuthority> buildGrantedAuthorities(PortletRequest context) {
        Collection<String> portletUserRoles = getUserRoles(context);
        Collection<? extends GrantedAuthority> userGas = portletUserRoles2GrantedAuthoritiesMapper.getGrantedAuthorities(portletUserRoles);

        if (logger.isDebugEnabled()) {
            logger.debug("Portlet roles [" + portletUserRoles + "] mapped to Granted Authorities: [" + userGas + "]");
        }
        return userGas;
    }

    /**
     * <p>setMappableRolesRetriever.</p>
     *
     * @param portletMappableRolesRetriever
     *            The MappableAttributesRetriever to use
     */
    public void setMappableRolesRetriever(MappableAttributesRetriever portletMappableRolesRetriever) {
        this.portletMappableRoles = Collections.unmodifiableSet(portletMappableRolesRetriever.getMappableAttributes());
    }

    /**
     * <p>setUserRoles2GrantedAuthoritiesMapper.</p>
     *
     * @param mapper
     *            The Attributes2GrantedAuthoritiesMapper to use
     */
    public void setUserRoles2GrantedAuthoritiesMapper(Attributes2GrantedAuthoritiesMapper mapper) {
        portletUserRoles2GrantedAuthoritiesMapper = mapper;
    }
}
