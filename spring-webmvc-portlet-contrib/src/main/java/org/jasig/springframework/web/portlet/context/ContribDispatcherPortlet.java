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

import org.jasig.springframework.web.portlet.support.PortletRequestContextUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.util.Assert;
import org.springframework.web.portlet.DispatcherPortlet;

/**
 * Extension to {@link org.springframework.web.portlet.DispatcherPortlet} that adds support for:
 * <ul>
 * <li>A portlet application level spring context</li>
 * </ul>
 *
 * @author Eric Dalquist
 * @version $Id: $Id
 */
public class ContribDispatcherPortlet extends DispatcherPortlet {

    /**
     * Request attribute to hold the current portlet application context.
     * Otherwise only the global web portlet context is obtainable by tags etc.
     * @see PortletRequestContextUtils#getPortletApplicationContext
     */
    public static final String PORTLET_APPLICATION_CONTEXT_ATTRIBUTE = DispatcherPortlet.class.getName() + ".CONTEXT";
    

    /**
     * <p>Constructor for ContribDispatcherPortlet.</p>
     */
    public ContribDispatcherPortlet() {
        super();
        
        //Set in constructor so it can be overridden later if needed
        this.setContextClass(ContribXmlPortletApplicationContext.class);
    }

    /**
     * {@inheritDoc}
     *
     * Uses {@link PortletApplicationContextUtils2#getPortletApplicationContext(PortletContext)} to see if
     * the portlet application level context has been loaded. The portlet applications's context is then used
     * as the parent for the portlet's context.
     */
    @Override
    protected ApplicationContext createPortletApplicationContext(ApplicationContext parent) {
        final PortletContext portletContext = this.getPortletContext();
        final PortletApplicationContext parentPortletApplicationContext = PortletApplicationContextUtils2.getPortletApplicationContext(portletContext);
        if (parentPortletApplicationContext != null) {
            //Found the parent PortletApplicationContext, use it as the parent
            return super.createPortletApplicationContext(parentPortletApplicationContext);
        }

        //No parent PortletApplicationContext was found, use the root web application context as the parent
        return super.createPortletApplicationContext(parent);
    }
    
    /** {@inheritDoc} */
    @Override
    protected ConfigurableEnvironment createEnvironment() {
        return new ContribStandardPortletEnvironment();
    }

    /** {@inheritDoc} */
    @Override
    public void setEnvironment(Environment environment) {
        Assert.isInstanceOf(ConfigurablePortletEnvironment.class, environment,
                "ContribDispatcherPortlet environment must be of type " +
                "ConfigurablePortletEnvironment");
        
        super.setEnvironment(environment);
    }
}
