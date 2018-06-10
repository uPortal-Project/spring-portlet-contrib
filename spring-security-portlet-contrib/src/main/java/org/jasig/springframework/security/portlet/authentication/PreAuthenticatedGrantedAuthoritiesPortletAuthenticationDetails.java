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
import java.util.List;

import javax.portlet.PortletRequest;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.SpringSecurityCoreVersion;
import org.springframework.security.core.authority.GrantedAuthoritiesContainer;

/**
 * This PortletAuthenticationDetails implementation allows for storing a list of
 * pre-authenticated Granted Authorities.
 *
 * @author Ruud Senden
 * @author Luke Taylor
 * @since 2.0
 */
public class PreAuthenticatedGrantedAuthoritiesPortletAuthenticationDetails extends PortletAuthenticationDetails
        implements GrantedAuthoritiesContainer {

    private static final long serialVersionUID = SpringSecurityCoreVersion.SERIAL_VERSION_UID;

    private final List<GrantedAuthority> authorities;

    public PreAuthenticatedGrantedAuthoritiesPortletAuthenticationDetails(PortletRequest request,
            Collection<? extends GrantedAuthority> authorities) {
        super(request);

        List<GrantedAuthority> temp = new ArrayList<GrantedAuthority>(authorities);
        this.authorities = Collections.unmodifiableList(temp);
    }

    public List<GrantedAuthority> getGrantedAuthorities() {
        return authorities;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((authorities == null) ? 0 : authorities.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (!(obj instanceof PreAuthenticatedGrantedAuthoritiesPortletAuthenticationDetails))
            return false;
        PreAuthenticatedGrantedAuthoritiesPortletAuthenticationDetails other = (PreAuthenticatedGrantedAuthoritiesPortletAuthenticationDetails) obj;
        if (authorities == null) {
            if (other.authorities != null)
                return false;
        }
        else if (!authorities.equals(other.authorities))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "PreAuthenticatedGrantedAuthoritiesPortletAuthenticationDetails [authorities=" + authorities
                + ", getGrantedAuthorities()=" + getGrantedAuthorities() + ", getRemoteAddress()=" + getRemoteAddress()
                + ", getSessionId()=" + getSessionId() + ", getUserInfo()=" + getUserInfo() + "]";
    }
}
