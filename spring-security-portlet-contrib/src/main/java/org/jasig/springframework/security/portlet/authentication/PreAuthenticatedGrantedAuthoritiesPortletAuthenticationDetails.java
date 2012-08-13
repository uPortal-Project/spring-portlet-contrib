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

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString()).append("; ");
        sb.append(authorities);
        return sb.toString();
    }
}
