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

import org.springframework.web.context.ServletContextAware;
import org.springframework.web.portlet.context.ConfigurablePortletApplicationContext;
import org.springframework.web.portlet.context.PortletContextAware;


/**
 * Interface to provide configuration for a web application. This is read-only while
 * the application is running, but may be reloaded if the implementation supports this.
 *
 * <p>This interface adds a <code>getServletContext()</code> method to the generic
 * ApplicationContext interface, and defines a well-known application attribute name
 * that the root context must be bound to in the bootstrap process.
 *
 * <p>Like generic application contexts, web application contexts are hierarchical.
 * There is a single root context per application, while each servlet in the application
 * (including a dispatcher servlet in the MVC framework) has its own child context.
 *
 * <p>In addition to standard application context lifecycle capabilities,
 * WebApplicationContext implementations need to detect {@link org.springframework.web.context.ServletContextAware}
 * beans and invoke the <code>setServletContext</code> method accordingly.
 *
 * @author Eric Dalquist
 * @see PortletContextAware#setPortletContext(PortletContext)
 *
 * TODO make {@link org.springframework.web.portlet.context.ConfigurablePortletApplicationContext} implement this interface
 * @version $Id: $Id
 */
public interface PortletApplicationContext extends ConfigurablePortletApplicationContext {
    /**
     * Context attribute to bind root PortletApplicationContext to on successful startup.
     * <p>Note: If the startup of the root portlet context fails, this attribute can contain
     * an exception or error as value. Use PortletApplicationContextUtils2 for convenient
     * lookup of the root portlet PortletApplicationContext.
     * @see PortletApplicationContextUtils2#getPortletApplicationContext(PortletContext)
     * @see PortletApplicationContextUtils2#getRequiredPortletApplicationContext(PortletContext)
     */
    String ROOT_PORTLET_APPLICATION_CONTEXT_ATTRIBUTE = PortletApplicationContext.class.getName() + ".ROOT";
    
    /**
     * Prefix for ApplicationContext ids that refer to portlet name.
     * 
     * TODO move from ConfigurablePortletApplicationContext when this becomes part of spring
     */
    String APPLICATION_CONTEXT_ID_PREFIX = ConfigurablePortletApplicationContext.APPLICATION_CONTEXT_ID_PREFIX;

    /**
     * Name of the PortletContext environment bean in the factory.
     * @see javax.portlet.PortletContext
     * 
     * TODO move from ConfigurablePortletApplicationContext when this becomes part of spring
     */
    String PORTLET_CONTEXT_BEAN_NAME = ConfigurablePortletApplicationContext.PORTLET_CONTEXT_BEAN_NAME;

    /**
     * Return the standard Portlet API PortletContext for this application.
     *
     * @return a {@link javax.portlet.PortletContext} object.
     */
    PortletContext getPortletContext();
}
