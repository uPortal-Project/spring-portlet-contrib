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
package org.jasig.springframework.web.portlet.context;

import javax.portlet.PortletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;
import org.springframework.web.portlet.DispatcherPortlet;
import org.springframework.web.portlet.FrameworkPortlet;
import org.springframework.web.portlet.context.ConfigurablePortletApplicationContext;

/**
 * Convenience methods for retrieving the root
 * {@link org.jasig.springframework.web.portlet.context.PortletApplicationContext} for a given
 * <code>PortletContext</code>. This is e.g. useful for accessing a Spring
 * context from within custom web views or Struts actions.
 *
 * <p>Note that there are more convenient ways of accessing the root context for
 * many portlet frameworks, either part of Spring or available as external library.
 * This helper class is just the most generic way to access the root context.
 *
 * @author Juergen Hoeller
 * @author Eric Dalquist
 * @see FrameworkPortlet
 * @see DispatcherPortlet
 * @version $Id: $Id
 */
public class PortletApplicationContextUtils2 {
    /**
     * Context attribute to bind root {@link PortletContextLoader}
     */
    public static final String ROOT_PORTLET_APPLICATION_CONTEXT_LOADER_ATTRIBUTE = ConfigurablePortletApplicationContext.class.getName() + ".ROOT_LOADER";

    private static final Log LOGGER = LogFactory.getLog(PortletApplicationContextUtils2.class);

    /**
     * Find the root PortletApplicationContext for this portlet application, which is
     * typically loaded via ContextLoaderFilter.
     * <p>Will rethrow an exception that happened on root context startup,
     * to differentiate between a failed context startup and no context at all.
     *
     * @param pc PortletContext to find the portlet application context for
     * @return the root PortletApplicationContext for this portlet app
     * @throws java.lang.IllegalStateException if the root PortletApplicationContext could not be found
     */
    public static PortletApplicationContext getRequiredPortletApplicationContext(PortletContext pc)
            throws IllegalStateException {

        PortletApplicationContext wac = getPortletApplicationContext(pc);
        if (wac == null) {
            throw new IllegalStateException("No PortletApplicationContext found: no PortletContextLoaderListener registered?");
        }
        return wac;
    }

    /**
     * Find the root PortletApplicationContext for this portlet application, which is
     * typically loaded via {@link org.jasig.springframework.web.portlet.context.PortletContextLoaderListener}.
     * <p>Will rethrow an exception that happened on root context startup,
     * to differentiate between a failed context startup and no context at all.
     *
     * @param pc PortletContext to find the web application context for
     * @return the root PortletApplicationContext for this portlet app, or <code>null</code> if none
     */
    public static PortletApplicationContext getPortletApplicationContext(PortletContext pc) {
        //First check if the parent PortletApplicationContext has been set
        PortletApplicationContext parentPortletApplicationContext = getPortletApplicationContext(pc, PortletApplicationContext.ROOT_PORTLET_APPLICATION_CONTEXT_ATTRIBUTE);
        if (parentPortletApplicationContext != null) {
            return parentPortletApplicationContext;
        }

        //Next look to see if a PortletContextLoader exists in the PortletContext, if not we
        //can't create the context in the next step
        final PortletContextLoader portletContextLoader = (PortletContextLoader)pc.getAttribute(PortletApplicationContextUtils2.ROOT_PORTLET_APPLICATION_CONTEXT_LOADER_ATTRIBUTE);
        if (portletContextLoader == null) {
            LOGGER.info("No PortletContextLoader found, skipping load of portlet-app level context. See org.jasig.springframework.web.portlet.context.PortletContextLoaderListener for more information");
            return null;
        }

        //Since a loader was found use it to get/create the root PortletApplicationContext in a thread-safe manner
        //The create is done in this lazy fashion as the portlet API provides nothing like a ServletContextListener that
        //can be used to create the root PortletApplicationContext before portlets/filters are initialized
        synchronized (portletContextLoader) {
            parentPortletApplicationContext = getPortletApplicationContext(pc, PortletApplicationContext.ROOT_PORTLET_APPLICATION_CONTEXT_ATTRIBUTE);
            if (parentPortletApplicationContext == null) {
                parentPortletApplicationContext = portletContextLoader.initWebApplicationContext(pc);
            }
        }

        return parentPortletApplicationContext;
    }

    /**
     * Find a custom PortletApplicationContext for this web application.
     *
     * @param pc PortletContext to find the web application context for
     * @param attrName the name of the PortletContext attribute to look for
     * @return the desired PortletApplicationContext for this web app, or <code>null</code> if none
     */
    public static PortletApplicationContext getPortletApplicationContext(PortletContext pc, String attrName) {
        Assert.notNull(pc, "PortletContext must not be null");
        Object attr = pc.getAttribute(attrName);
        if (attr == null) {
            return null;
        }
        if (attr instanceof RuntimeException) {
            throw (RuntimeException) attr;
        }
        if (attr instanceof Error) {
            throw (Error) attr;
        }
        if (attr instanceof Exception) {
            throw new IllegalStateException((Exception) attr);
        }
        if (!(attr instanceof PortletApplicationContext)) {
            throw new IllegalStateException("Context attribute is not of type PortletApplicationContext: " + attr.getClass() + " - " + attr);
        }
        return (PortletApplicationContext) attr;
    }
}
