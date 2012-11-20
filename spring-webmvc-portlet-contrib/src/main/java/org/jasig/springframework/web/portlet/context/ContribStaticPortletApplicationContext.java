package org.jasig.springframework.web.portlet.context;

import org.springframework.web.portlet.context.ConfigurablePortletApplicationContext;
import org.springframework.web.portlet.context.StaticPortletApplicationContext;

/**
 * Exists simply to add {@link PortletApplicationContext} into the type hierarchy of
 * StaticPortletApplicationContext. This class should be deleted when this functionality
 * gets added into Spring proper and {@link ConfigurablePortletApplicationContext} 
 * implements {@link PortletApplicationContext}
 * 
 * @author Eric Dalquist
 */
public class ContribStaticPortletApplicationContext extends StaticPortletApplicationContext implements
        PortletApplicationContext {
    
}
