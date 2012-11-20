package org.jasig.springframework.mock.web.portlet;

import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.portlet.PortletContext;
import javax.portlet.filter.FilterConfig;
import javax.portlet.filter.PortletFilter;

import org.springframework.mock.web.portlet.MockPortletContext;
import org.springframework.util.Assert;

/**
 * Mock implementation of the {@link FilterConfig} interface.
 *
 * <p>Used for testing the web framework; also useful for testing
 * custom {@link PortletFilter} implementations.
 *
 * @author Eric Dalquist
 */
public class MockFilterConfig implements FilterConfig {

    private final PortletContext portletContext;

    private final String filterName;

    private final Map<String, String> initParameters = new LinkedHashMap<String, String>();

    /**
     * Create a new MockFilterConfig with a default {@link MockPortletContext}.
     */
    public MockFilterConfig() {
        this(null, "");
    }

    /**
     * Create a new MockFilterConfig with a default {@link MockPortletContext}.
     * @param filterName the name of the filter
     */
    public MockFilterConfig(String filterName) {
        this(null, filterName);
    }

    /**
     * Create a new MockFilterConfig.
     * @param portletContext the PortletContext that the portlet runs in
     */
    public MockFilterConfig(PortletContext portletContext) {
        this(portletContext, "");
    }

    /**
     * Create a new MockFilterConfig.
     * @param portletContext the PortletContext that the portlet runs in
     * @param filterName the name of the filter
     */
    public MockFilterConfig(PortletContext portletContext, String filterName) {
        this.portletContext = (portletContext != null ? portletContext : new MockPortletContext());
        this.filterName = filterName;
    }
    
    @Override
    public String getFilterName() {
        return this.filterName;
    }

    @Override
    public PortletContext getPortletContext() {
        return this.portletContext;
    }

    public void addInitParameter(String name, String value) {
        Assert.notNull(name, "Parameter name must not be null");
        this.initParameters.put(name, value);
    }

    @Override
    public String getInitParameter(String name) {
        return this.initParameters.get(name);
    }

    @Override
    public Enumeration<String> getInitParameterNames() {
        return Collections.enumeration(this.initParameters.keySet());
    }

}
