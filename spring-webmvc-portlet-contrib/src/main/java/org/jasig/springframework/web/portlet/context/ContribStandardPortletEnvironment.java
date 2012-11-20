package org.jasig.springframework.web.portlet.context;

import javax.portlet.PortletConfig;
import javax.portlet.PortletContext;
import javax.servlet.ServletContext;

import org.springframework.web.portlet.context.PortletApplicationContextUtils;
import org.springframework.web.portlet.context.StandardPortletEnvironment;

/**
 * Adds support for {@link ConfigurablePortletEnvironment} to initialize the portlet property sources
 * 
 * @author Eric Dalquist
 */
public class ContribStandardPortletEnvironment extends StandardPortletEnvironment implements
        ConfigurablePortletEnvironment {

    @Override
    public void initPropertySources(ServletContext servletContext, PortletContext portletContext, PortletConfig portletConfig) {
        PortletApplicationContextUtils.initPortletPropertySources(this.getPropertySources(), servletContext, portletContext, portletConfig);
    }
}
