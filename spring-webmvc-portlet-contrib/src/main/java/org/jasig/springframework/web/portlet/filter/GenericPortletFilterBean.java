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
package org.jasig.springframework.web.portlet.filter;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.EventRequest;
import javax.portlet.EventResponse;
import javax.portlet.GenericPortlet;
import javax.portlet.PortletContext;
import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import javax.portlet.filter.ActionFilter;
import javax.portlet.filter.EventFilter;
import javax.portlet.filter.FilterChain;
import javax.portlet.filter.FilterConfig;
import javax.portlet.filter.RenderFilter;
import javax.portlet.filter.ResourceFilter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.springframework.web.portlet.context.PortletApplicationContextUtils2;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceEditor;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.portlet.context.PortletContextAware;
import org.springframework.web.portlet.context.PortletContextResourceLoader;
import org.springframework.web.portlet.context.StandardPortletEnvironment;

/**
 * Simple base implementation of {@link ActionFilter}, {@link EventFilter},
 * {@link RenderFilter}, and {@link ResourceFilter} which treats its config
 * parameters (<code>init-param</code> entries within the <code>filter</code>
 * tag in <code>portlet.xml</code>) as bean properties.
 *
 * <p>A handy superclass for any type of filter. Type conversion of config
 * parameters is automatic, with the corresponding setter method getting
 * invoked with the converted value. It is also possible for subclasses to
 * specify required properties. Parameters without matching bean property
 * setter will simply be ignored.
 *
 * <p>This filter leaves actual filtering to subclasses, which have to
 * override the <code>doFilter</code> method(s) the correspond to the portlet
 * filter interface(s) being used. 
 *
 * <p>This generic filter base class has no dependency on the Spring
 * {@link org.springframework.context.ApplicationContext} concept.
 * Filters usually don't load their own context but rather access service
 * beans from the Spring root application context, accessible via the
 * filter's {@link #getPortletContext() PortletContext} (see
 * {@link PortletApplicationContextUtils2}).
 *
 *
 * @author Eric Dalquist
 * @version $Revision: 23744 $
 */
public abstract class GenericPortletFilterBean implements
        ActionFilter, EventFilter, RenderFilter, ResourceFilter, BeanNameAware, 
        EnvironmentAware, PortletContextAware, InitializingBean, DisposableBean {


    /** Logger available to subclasses */
    protected final Log logger = LogFactory.getLog(getClass());

    /**
     * Set of required properties (Strings) that must be supplied as
     * config parameters to this filter.
     */
    private final Set<String> requiredProperties = new HashSet<String>();

    /* The FilterConfig of this filter */
    private FilterConfig filterConfig;

    private String beanName;
    
    private Environment environment = new StandardPortletEnvironment();

    private PortletContext portletContext;
    
    /**
     * Stores the bean name as defined in the Spring bean factory.
     * <p>Only relevant in case of initialization as bean, to have a name as
     * fallback to the filter name usually provided by a FilterConfig instance.
     * @see org.springframework.beans.factory.BeanNameAware
     * @see #getFilterName()
     */
    public final void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    /**
     * {@inheritDoc}
     * <p>Any environment set here overrides the {@link StandardPortletEnvironment}
     * provided by default.
     * <p>This {@code Environment} object is used only for resolving placeholders in
     * resource paths passed into init-parameters for this filter. If no init-params are
     * used, this {@code Environment} can be essentially ignored.
     * @see #init(FilterConfig)
     */
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    /**
     * Stores the PortletContext that the bean factory runs in.
     * <p>Only relevant in case of initialization as bean, to have a PortletContext
     * as fallback to the context usually provided by a FilterConfig instance.
     * @see PortletContextAware
     * @see #getPortletContext()
     */
    public final void setPortletContext(PortletContext portletContext) {
        this.portletContext = portletContext;
    }

    /**
     * Calls the <code>initFilterBean()</code> method that might
     * contain custom initialization of a subclass.
     * <p>Only relevant in case of initialization as bean, where the
     * standard <code>init(FilterConfig)</code> method won't be called.
     * @see #initFilterBean()
     * @see #init(FilterConfig)
     */
    public void afterPropertiesSet() throws Exception {
        initFilterBean();
    }


    /**
     * Subclasses can invoke this method to specify that this property
     * (which must match a JavaBean property they expose) is mandatory,
     * and must be supplied as a config parameter. This should be called
     * from the constructor of a subclass.
     * <p>This method is only relevant in case of traditional initialization
     * driven by a FilterConfig instance.
     * @param property name of the required property
     */
    protected final void addRequiredProperty(String property) {
        this.requiredProperties.add(property);
    }
    
    @Override
    public void init(FilterConfig filterConfig) throws PortletException {
        Assert.notNull(filterConfig, "FilterConfig must not be null");
        if (logger.isDebugEnabled()) {
            logger.debug("Initializing filter '" + filterConfig.getFilterName() + "'");
        }

        this.filterConfig = filterConfig;

        // Set bean properties from init parameters.
        try {
            PropertyValues pvs = new FilterConfigPropertyValues(filterConfig, this.requiredProperties);
            BeanWrapper bw = PropertyAccessorFactory.forBeanPropertyAccess(this);
            ResourceLoader resourceLoader = new PortletContextResourceLoader(filterConfig.getPortletContext());
            bw.registerCustomEditor(Resource.class, new ResourceEditor(resourceLoader, this.environment));
            initBeanWrapper(bw);
            bw.setPropertyValues(pvs, true);
        }
        catch (BeansException ex) {
            String msg = "Failed to set bean properties on filter '" +
                filterConfig.getFilterName() + "': " + ex.getMessage();
            logger.error(msg, ex);
            throw new PortletException(msg, ex);
        }

        // Let subclasses do whatever initialization they like.
        initFilterBean();

        if (logger.isDebugEnabled()) {
            logger.debug("Filter '" + filterConfig.getFilterName() + "' configured successfully");
        }
    }

    /**
     * Initialize the BeanWrapper for this GenericPortletFilterBean,
     * possibly with custom editors.
     * <p>This default implementation is empty.
     * @param bw the BeanWrapper to initialize
     * @throws BeansException if thrown by BeanWrapper methods
     * @see org.springframework.beans.BeanWrapper#registerCustomEditor
     */
    protected void initBeanWrapper(BeanWrapper bw) throws BeansException {
    }


    /**
     * Make the FilterConfig of this filter available, if any.
     * Analogous to GenericPortlet's <code>getPortletConfig()</code>.
     * <p>Public to resemble the <code>getFilterConfig()</code> method
     * of the Portlet Filter version that shipped with WebLogic 6.1.
     * @return the FilterConfig instance, or <code>null</code> if none available
     * @see javax.portlet.GenericPortlet#getPortletConfig()
     */
    public final FilterConfig getFilterConfig() {
        return this.filterConfig;
    }

    /**
     * Make the name of this filter available to subclasses.
     * Analogous to GenericPortlets's <code>getPortletName()</code>.
     * <p>Takes the FilterConfig's filter name by default.
     * If initialized as bean in a Spring application context,
     * it falls back to the bean name as defined in the bean factory.
     * @return the filter name, or <code>null</code> if none available
     * @see GenericPortlet#getPortletName()
     * @see FilterConfig#getFilterName()
     * @see #setBeanName
     */
    protected final String getFilterName() {
        return (this.filterConfig != null ? this.filterConfig.getFilterName() : this.beanName);
    }

    /**
     * Make the PortletContext of this filter available to subclasses.
     * Analogous to GenericPortlet's <code>getPortletContext()</code>.
     * <p>Takes the FilterConfig's PortletContext by default.
     * If initialized as bean in a Spring application context,
     * it falls back to the PortletContext that the bean factory runs in.
     * @return the PortletContext instance, or <code>null</code> if none available
     * @see javax.portlet.GenericPortlet#getPortletContext()
     * @see javax.portlet.FilterConfig#getPortletContext()
     * @see #setPortletContext
     */
    protected final PortletContext getPortletContext() {
        return (this.filterConfig != null ? this.filterConfig.getPortletContext() : this.portletContext);
    }


    /**
     * Subclasses may override this to perform custom initialization.
     * All bean properties of this filter will have been set before this
     * method is invoked.
     * <p>Note: This method will be called from standard filter initialization
     * as well as filter bean initialization in a Spring application context.
     * Filter name and PortletContext will be available in both cases.
     * <p>This default implementation is empty.
     * @throws PortletException if subclass initialization fails
     * @see #getFilterName()
     * @see #getPortletContext()
     */
    protected void initFilterBean() throws PortletException {
    }
    
    /**
     * Calls {@link #doCommonFilter(PortletRequest, PortletResponse, FilterChain)}
     */
    @Override
    public void doFilter(ActionRequest request, ActionResponse response, FilterChain chain) throws IOException,
            PortletException {
        doCommonFilter(request, response, chain);
    }
    
    /**
     * Calls {@link #doCommonFilter(PortletRequest, PortletResponse, FilterChain)}
     */
    @Override
    public void doFilter(EventRequest request, EventResponse response, FilterChain chain) throws IOException,
            PortletException {
        doCommonFilter(request, response, chain);        
    }
    
    /**
     * Calls {@link #doCommonFilter(PortletRequest, PortletResponse, FilterChain)}
     */
    @Override
    public void doFilter(ResourceRequest request, ResourceResponse response, FilterChain chain) throws IOException,
            PortletException {
        doCommonFilter(request, response, chain);        
    }

    /**
     * Calls {@link #doCommonFilter(PortletRequest, PortletResponse, FilterChain)}
     */
    @Override
    public void doFilter(RenderRequest request, RenderResponse response, FilterChain chain) throws IOException,
            PortletException {
        doCommonFilter(request, response, chain);        
    }

    /**
     * Can be implemented by subclasses to provide filter handling common to all request types. Default
     * implementation just uses {@link PortletFilterUtils#doFilter(PortletRequest, PortletResponse, FilterChain)}
     */
    protected void doCommonFilter(PortletRequest request, PortletResponse response, FilterChain chain) throws IOException, PortletException {
        PortletFilterUtils.doFilter(request, response, chain);
    }

    /**
     * Subclasses may override this to perform custom filter shutdown.
     * <p>Note: This method will be called from standard filter destruction
     * as well as filter bean destruction in a Spring application context.
     * <p>This default implementation is empty.
     */
    public void destroy() {
    }


    /**
     * PropertyValues implementation created from FilterConfig init parameters.
     */
    @SuppressWarnings("serial")
    private static class FilterConfigPropertyValues extends MutablePropertyValues {

        /**
         * Create new FilterConfigPropertyValues.
         * @param config FilterConfig we'll use to take PropertyValues from
         * @param requiredProperties set of property names we need, where
         * we can't accept default values
         * @throws PortletException if any required properties are missing
         */
        public FilterConfigPropertyValues(FilterConfig config, Set<String> requiredProperties)
            throws PortletException {

            Set<String> missingProps = (requiredProperties != null && !requiredProperties.isEmpty()) ?
                    new HashSet<String>(requiredProperties) : null;

            Enumeration<?> en = config.getInitParameterNames();
            while (en.hasMoreElements()) {
                String property = (String) en.nextElement();
                Object value = config.getInitParameter(property);
                addPropertyValue(new PropertyValue(property, value));
                if (missingProps != null) {
                    missingProps.remove(property);
                }
            }

            // Fail if we are still missing properties.
            if (missingProps != null && missingProps.size() > 0) {
                throw new PortletException(
                    "Initialization from FilterConfig for filter '" + config.getFilterName() +
                    "' failed; the following required properties were missing: " +
                    StringUtils.collectionToDelimitedString(missingProps, ", "));
            }
        }
    }
}