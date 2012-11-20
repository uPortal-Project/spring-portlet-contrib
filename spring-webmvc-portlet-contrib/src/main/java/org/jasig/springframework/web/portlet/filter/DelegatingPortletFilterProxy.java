/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.springframework.web.portlet.filter;

import java.io.IOException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.EventRequest;
import javax.portlet.EventResponse;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import javax.portlet.filter.ActionFilter;
import javax.portlet.filter.EventFilter;
import javax.portlet.filter.FilterChain;
import javax.portlet.filter.PortletFilter;
import javax.portlet.filter.RenderFilter;
import javax.portlet.filter.ResourceFilter;
import javax.servlet.Filter;

import org.jasig.springframework.web.portlet.context.PortletApplicationContext;
import org.jasig.springframework.web.portlet.context.PortletApplicationContextUtils2;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.portlet.context.PortletApplicationContextUtils;

/**
 * Proxy for a standard Portlet 2.0 Filter, delegating to a Spring-managed
 * bean that implements the Filter interface. Supports a "targetBeanName"
 * filter init-param in {@code portlet.xml}, specifying the name of the
 * target bean in the Spring application context.
 *
 * <p>{@code portlet.xml} will usually contain a {@code DelegatingPortletFilterProxy}
 * definition, with the specified {@code filter-name} corresponding to a bean name in
 * Spring's root portlet application context. All calls to the filter proxy will then
 * be delegated to that bean in the Spring context, which is required to implement
 * the standard Portlet 2.0 Filter interface.
 *
 * <p>This approach is particularly useful for Filter implementation with complex
 * setup needs, allowing to apply the full Spring bean definition machinery to
 * Filter instances. Alternatively, consider standard Filter setup in combination
 * with looking up service beans from the Spring root application context.
 *
 * <p><b>NOTE:</b> The lifecycle methods defined by the Portlet Filter interface
 * will by default <i>not</i> be delegated to the target bean, relying on the
 * Spring application context to manage the lifecycle of that bean. Specifying
 * the "targetFilterLifecycle" filter init-param as "true" will enforce invocation
 * of the {@code PortletFilter.init} and {@code PortletFilter.destroy} lifecycle methods
 * on the target bean, letting the portlet container manage the filter lifecycle.
 *
 * <p>This class was originally inspired by Spring's {@code DelegatingFilterProxy}
 *
 * @author Eric Dalquist
 * @see #setTargetBeanName
 * @see #setTargetFilterLifecycle
 * @see javax.portlet.filter.ActionFilter#doFilter
 * @see javax.portlet.filter.EventFilter#doFilter
 * @see javax.portlet.filter.RenderFilter#doFilter
 * @see javax.portlet.filter.ResourceFilter#doFilter
 * @see javax.portlet.filter.PortletFilter#init
 * @see javax.portlet.filter.PortletFilter#destroy
 * @see #DelegatingFilterProxy(Filter)
 * @see #DelegatingFilterProxy(String)
 * @see #DelegatingFilterProxy(String, WebApplicationContext)
 * @see org.springframework.web.WebApplicationInitializer
 */
public class DelegatingPortletFilterProxy extends GenericPortletFilterBean {

    private String targetBeanName;

    private boolean targetFilterLifecycle = false;
    
    private String contextAttribute;

    private PortletFilter delegate;
    private ActionFilter actionDelegate;
    private EventFilter eventDelegate;
    private RenderFilter renderDelegate;
    private ResourceFilter resourceDelegate;

    private final ReadWriteLock delegateLock = new ReentrantReadWriteLock();
    private final Lock delegateReadLock = this.delegateLock.readLock();
    private final Lock delegateWriteLock = this.delegateLock.writeLock();


    /**
     * Set the name of the ServletContext attribute which should be used to retrieve the
     * {@link PortletApplicationContext} from which to load the delegate {@link PortletFilter} bean.
     */
    public void setContextAttribute(String contextAttribute) {
        this.contextAttribute = contextAttribute;
    }

    /**
     * Return the name of the ServletContext attribute which should be used to retrieve the
     * {@link PortletApplicationContext} from which to load the delegate {@link PortletFilter} bean.
     */
    public String getContextAttribute() {
        return this.contextAttribute;
    }

    /**
     * Set the name of the target bean in the Spring application context.
     * The target bean must implement the standard Portlet 2.0 PortletFilter interface.
     * <p>By default, the <code>filter-name</code> as specified for the
     * DelegatingPortletFilterProxy in <code>portlet.xml</code> will be used.
     */
    public void setTargetBeanName(String targetBeanName) {
        this.targetBeanName = targetBeanName;
    }

    /**
     * Return the name of the target bean in the Spring application context.
     */
    protected String getTargetBeanName() {
        return this.targetBeanName;
    }

    /**
     * Set whether to invoke the <code>PortletFilter.init</code> and
     * <code>PortletFilter.destroy</code> lifecycle methods on the target bean.
     * <p>Default is "false"; target beans usually rely on the Spring application
     * context for managing their lifecycle. Setting this flag to "true" means
     * that the portlet container will control the lifecycle of the target
     * PortletFilter, with this proxy delegating the corresponding calls.
     */
    public void setTargetFilterLifecycle(boolean targetFilterLifecycle) {
        this.targetFilterLifecycle = targetFilterLifecycle;
    }

    /**
     * Return whether to invoke the <code>PortletFilter.init</code> and
     * <code>PortletFilter.destroy</code> lifecycle methods on the target bean.
     */
    protected boolean isTargetFilterLifecycle() {
        return this.targetFilterLifecycle;
    }


    protected void initFilterBean() throws PortletException {
        // If no target bean name specified, use filter name.
        if (this.targetBeanName == null) {
            this.targetBeanName = getFilterName();
        }

        // Fetch Spring root application context and initialize the delegate early,
        // if possible. If the root application context will be started after this
        // filter proxy, we'll have to resort to lazy initialization.
        initDelegate(false);
    }

    
    public void doFilter(ResourceRequest request, ResourceResponse response, FilterChain chain) throws IOException,
            PortletException {
        
        // Lazily initialize the delegate if necessary.
        initDelegate(true);
        
        if (this.resourceDelegate == null) {
            throw new IllegalStateException("The delegate PortletFilter does not implement ResourceFilter but " + this.getFilterName() + " is configured with the RESOURCE_PHASE lifecycle.");
        }

        // Let the delegate perform the actual doFilter operation.
        invokeDelegate(this.resourceDelegate, request, response, chain);
    }

    public void doFilter(EventRequest request, EventResponse response, FilterChain chain) throws IOException,
            PortletException {
        
        // Lazily initialize the delegate if necessary.
        initDelegate(true);
        
        if (this.eventDelegate == null) {
            throw new IllegalStateException("The delegate PortletFilter does not implement EventFilter but " + this.getFilterName() + " is configured with the EVENT_PHASE lifecycle.");
        }

        // Let the delegate perform the actual doFilter operation.
        invokeDelegate(this.eventDelegate, request, response, chain);
    }

    public void doFilter(RenderRequest request, RenderResponse response, FilterChain chain) throws IOException,
            PortletException {
        
        // Lazily initialize the delegate if necessary.
        initDelegate(true);
        
        if (this.renderDelegate == null) {
            throw new IllegalStateException("The delegate PortletFilter does not implement RenderFilter but " + this.getFilterName() + " is configured with the RENDER_PHASE lifecycle.");
        }

        // Let the delegate perform the actual doFilter operation.
        invokeDelegate(this.renderDelegate, request, response, chain);
    }

    public void doFilter(ActionRequest request, ActionResponse response, FilterChain chain) throws IOException,
            PortletException {
        
        // Lazily initialize the delegate if necessary.
        initDelegate(true);
        
        if (this.actionDelegate == null) {
            throw new IllegalStateException("The delegate PortletFilter does not implement ActionFilter but " + this.getFilterName() + " is configured with the ACTION_PHASE lifecycle.");
        }

        // Let the delegate perform the actual doFilter operation.
        invokeDelegate(this.actionDelegate, request, response, chain);
    }

    public void destroy() {
        PortletFilter delegateToUse = null;
        delegateReadLock.lock();
        try {
            delegateToUse = this.delegate;
        }
        finally {
            delegateReadLock.unlock();
        }
        
        if (delegateToUse != null) {
            destroyDelegate(delegateToUse);
        }
    }


    /**
     * Retrieve a <code>WebApplicationContext</code> from the <code>PortletContext</code>. The
     * <code>WebApplicationContext</code> must have already been loaded and stored in the
     * <code>PortletContext</code> before this filter gets initialized (or invoked).
     * <p>Subclasses may override this method to provide a different
     * <code>WebApplicationContext</code> retrieval strategy.
     * @return the WebApplicationContext for this proxy, or <code>null</code> if not found
     */
    protected ApplicationContext findWebApplicationContext() {
        String attrName = getContextAttribute();
        final PortletApplicationContext portletApplicationContext;
        if (attrName != null) {
            portletApplicationContext = PortletApplicationContextUtils2.getPortletApplicationContext(getPortletContext(), attrName);
        }
        else {
            portletApplicationContext = PortletApplicationContextUtils2.getPortletApplicationContext(getPortletContext());
        }
        
        if (portletApplicationContext != null) {
            return portletApplicationContext;
        }
        
        return PortletApplicationContextUtils.getWebApplicationContext(getPortletContext());
    }

    /**
     * Initialize the PortletFilter delegate, defined as bean the given Spring
     * application context.
     * <p>The default implementation fetches the bean from the application context
     * and calls the standard <code>PortletFilter.init</code> method on it, passing
     * in the FilterConfig of this PortletFilter proxy.
     * @param wac the root application context
     * @return the initialized delegate PortletFilter
     * @throws PortletException if thrown by the PortletFilter
     * @see #getTargetBeanName()
     * @see #isTargetFilterLifecycle()
     * @see #getFilterConfig()
     * @see javax.portlet.filter.PortletFilter#init(javax.portlet.filter.FilterConfig)
     */
    protected void initDelegate(boolean require) throws PortletException {
        final ApplicationContext wac = findWebApplicationContext();
        PortletFilter delegate = null;
        
        //Check if initialization is complete
        this.delegateReadLock.lock();
        try {
            delegate = this.delegate;
        }
        finally {
            this.delegateReadLock.unlock();
        }
        
        //Return if the delegate filter was found
        if (delegate != null) {
            return;
        }
        
        this.delegateWriteLock.lock();
        try {
            //Already initialized
            if (this.delegate != null) {
                return;
            }
            
            //Verify app context is available
            if (wac == null) {
                //If required init throw an exception for a missing app context
                if (require) {
                    throw new IllegalStateException("No ApplicationContext found: no ContextLoaderListener registered?");
                }

                //No app context and not required init, just ignore the init request
                return;
            }
    
            //Load and init the delegate filter
            delegate = wac.getBean(getTargetBeanName(), PortletFilter.class);
            if (isTargetFilterLifecycle()) {
                delegate.init(getFilterConfig());
            }
            
            //init local fields
            this.delegate = delegate;
            if (delegate instanceof ActionFilter) {
                actionDelegate = (ActionFilter)delegate;
            }
            if (delegate instanceof EventFilter) {
                eventDelegate = (EventFilter)delegate;
            }
            if (delegate instanceof RenderFilter) {
                renderDelegate = (RenderFilter)delegate;
            }
            if (delegate instanceof ResourceFilter) {
                resourceDelegate = (ResourceFilter)delegate;
            }
        }
        finally {
            this.delegateWriteLock.unlock();
        }
    }

    /**
     * Actually invoke the delegate ActionFilter with the given request and response.
     * @param delegate the delegate ActionFilter
     * @param request the current action request
     * @param response the current action response
     * @param filterChain the current FilterChain
     * @throws PortletException if thrown by the PortletFilter
     * @throws IOException if thrown by the PortletFilter
     */
    protected void invokeDelegate(
            ActionFilter delegate, ActionRequest request, ActionResponse response, FilterChain filterChain)
            throws PortletException, IOException {

        delegate.doFilter(request, response, filterChain);
    }

    /**
     * Actually invoke the delegate EventFilter with the given request and response.
     * @param delegate the delegate EventFilter
     * @param request the current Event request
     * @param response the current Event response
     * @param filterChain the current FilterChain
     * @throws PortletException if thrown by the PortletFilter
     * @throws IOException if thrown by the PortletFilter
     */
    protected void invokeDelegate(
            EventFilter delegate, EventRequest request, EventResponse response, FilterChain filterChain)
            throws PortletException, IOException {

        delegate.doFilter(request, response, filterChain);
    }

    /**
     * Actually invoke the delegate RenderFilter with the given request and response.
     * @param delegate the delegate RenderFilter
     * @param request the current Render request
     * @param response the current Render response
     * @param filterChain the current FilterChain
     * @throws PortletException if thrown by the PortletFilter
     * @throws IOException if thrown by the PortletFilter
     */
    protected void invokeDelegate(
            RenderFilter delegate, RenderRequest request, RenderResponse response, FilterChain filterChain)
            throws PortletException, IOException {

        delegate.doFilter(request, response, filterChain);
    }

    /**
     * Actually invoke the delegate ResourceFilter with the given request and response.
     * @param delegate the delegate ResourceFilter
     * @param request the current Resource request
     * @param response the current Resource response
     * @param filterChain the current FilterChain
     * @throws PortletException if thrown by the PortletFilter
     * @throws IOException if thrown by the PortletFilter
     */
    protected void invokeDelegate(
            ResourceFilter delegate, ResourceRequest request, ResourceResponse response, FilterChain filterChain)
            throws PortletException, IOException {

        delegate.doFilter(request, response, filterChain);
    }

    /**
     * Destroy the PortletFilter delegate.
     * Default implementation simply calls <code>PortletFilter.destroy</code> on it.
     * @param delegate the PortletFilter delegate (never <code>null</code>)
     * @see #isTargetFilterLifecycle()
     * @see javax.portlet.filter.PortletFilter#destroy()
     */
    protected void destroyDelegate(PortletFilter delegate) {
        if (isTargetFilterLifecycle()) {
            delegate.destroy();
        }
    }
}