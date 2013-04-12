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
     * TODO this gets moved into ConfigurablePortletApplicationContext
     */
    @Override
    public void setEnvironment(ConfigurableEnvironment environment) {
        Assert.isInstanceOf(ConfigurablePortletEnvironment.class, environment,
                "ContribDispatcherPortlet environment must be of type " +
                "ConfigurablePortletEnvironment");
        
        super.setEnvironment(environment);
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
