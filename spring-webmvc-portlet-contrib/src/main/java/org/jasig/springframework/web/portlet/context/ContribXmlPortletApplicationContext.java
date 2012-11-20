package org.jasig.springframework.web.portlet.context;

import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.util.Assert;
import org.springframework.web.portlet.context.ConfigurablePortletApplicationContext;
import org.springframework.web.portlet.context.XmlPortletApplicationContext;

/**
 * Exists simply to add {@link PortletApplicationContext} into the type hierarchy of
 * XmlPortletApplicationContext. This class should be deleted when this functionality
 * gets added into Spring proper and {@link ConfigurablePortletApplicationContext} 
 * implements {@link PortletApplicationContext}
 * 
 * @author Eric Dalquist
 */
public class ContribXmlPortletApplicationContext extends XmlPortletApplicationContext implements
        PortletApplicationContext {
    

    /**
     * TODO this goes away once ContribStandardPortletEnvironment gets merged into StandardPortletEnvironment
     */
    @Override
    protected ConfigurableEnvironment createEnvironment() {
        return new ContribStandardPortletEnvironment();
    }

    /**
     * TODO this gets moved into ConfigurablePortletApplicationContext
     */
    @Override
    public ConfigurablePortletEnvironment getEnvironment() {
        ConfigurableEnvironment env = super.getEnvironment();
        Assert.isInstanceOf(ConfigurablePortletEnvironment.class, env,
                "ConfigurablePortletApplicationContext environment must be of type " +
                "ConfigurablePortletEnvironment");
        return (ConfigurablePortletEnvironment) env;
    }

    /**
     * {@inheritDoc}
     * <p>Replace {@code Servlet}-related property sources.
     * TODO this gets moved into ConfigurablePortletApplicationContext
     */
    @Override
    protected void initPropertySources() {
        super.initPropertySources();
        this.getEnvironment().initPropertySources(this.getServletContext(), this.getPortletContext(), this.getPortletConfig());
    }
    
}
