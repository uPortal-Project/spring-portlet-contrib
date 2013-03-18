package org.jasig.springframework.security.portlet.util;

import javax.portlet.PortletRequest;

import org.springframework.security.core.Authentication;

public interface AuthenticationValidator {
	
	public boolean validate(Authentication currentUser, PortletRequest request);

}
