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
package org.jasig.springframework.web.portlet.context;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.springframework.web.context.ContextCleanupListener;
import org.springframework.web.context.ContextLoaderListener;

/**
 * Bootstrap listener to start up and shut down Spring's root {@link PortletApplicationContext}.
 * Simply delegates to {@link PortletContextLoader} as well as to {@link ContextCleanupListener}.
 * 
 * <p>This class is configured in the <code>web.xml</code> of the application and MUST be
 * registered after the {@link ContextLoaderListener}
 *
 * @author Eric Dalquist
 */
public class PortletContextLoaderListener implements ServletContextListener {
    private PortletContextLoader contextLoader;
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        final ServletContext servletContext = sce.getServletContext();
        if (servletContext.getAttribute(PortletApplicationContextUtils2.ROOT_PORTLET_APPLICATION_CONTEXT_LOADER_ATTRIBUTE) != null) {
            throw new IllegalStateException(
                    "Cannot initialize root portlet ContextLoader context because there is already a root PortletContextLoader present - " +
                    "check whether you have multiple PortletContextLoaderListener definitions in your web.xml!");
        }
        
        //Register the portlet context loader with the servlet context
        contextLoader = new PortletContextLoader(servletContext);
        servletContext.setAttribute(PortletApplicationContextUtils2.ROOT_PORTLET_APPLICATION_CONTEXT_LOADER_ATTRIBUTE, contextLoader);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        //destroy the root portlet app context
        final ServletContext servletContext = sce.getServletContext();
        contextLoader.closeWebApplicationContext(servletContext);
        
        servletContext.removeAttribute(PortletApplicationContextUtils2.ROOT_PORTLET_APPLICATION_CONTEXT_LOADER_ATTRIBUTE);
        contextLoader = null;
    }
}
