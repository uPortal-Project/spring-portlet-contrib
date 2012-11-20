package org.jasig.springframework.web.portlet.context;

import javax.portlet.PortletConfig;
import javax.portlet.PortletContext;
import javax.servlet.ServletContext;

import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.web.portlet.context.ConfigurablePortletApplicationContext;

/**
 * Specialization of {@link ConfigurableEnvironment} allowing initialization of
 * portlet-related {@link org.springframework.core.env.PropertySource} objects at the
 * earliest moment the {@link PortletContext} and (optionally) {@link PortletConfig}
 * become available.
 *
 * @author Eric Dalquist
 * @see ConfigurablePortletApplicationContext#getEnvironment()
 */
public interface ConfigurablePortletEnvironment extends ConfigurableEnvironment {

    /**
     * Replace any {@linkplain
     * org.springframework.core.env.PropertySource.StubPropertySource stub property source}
     * instances acting as placeholders with real portlet context/config property sources
     * using the given parameters.
     * @param servletContext the {@link ServletContext} (may not be {@code null})
     * @param portletContext the {@link PortletContext} (may not be {@code null})
     * @param portletConfig the {@link PortletContext} ({@code null} if not available)
     */
    void initPropertySources(ServletContext servletContext, PortletContext portletContext, PortletConfig portletConfig);

}
