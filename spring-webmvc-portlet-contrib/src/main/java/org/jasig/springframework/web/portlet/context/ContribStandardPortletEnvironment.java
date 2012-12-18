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

import javax.portlet.PortletConfig;
import javax.portlet.PortletContext;
import javax.servlet.ServletContext;

import org.springframework.web.portlet.context.PortletApplicationContextUtils;
import org.springframework.web.portlet.context.StandardPortletEnvironment;

/**
 * Adds support for {@link ConfigurablePortletEnvironment} to initialize the portlet property sources
 * 
 * @author Eric Dalquist
 */
public class ContribStandardPortletEnvironment extends StandardPortletEnvironment implements
        ConfigurablePortletEnvironment {

    @Override
    public void initPropertySources(ServletContext servletContext, PortletContext portletContext, PortletConfig portletConfig) {
        PortletApplicationContextUtils.initPortletPropertySources(this.getPropertySources(), servletContext, portletContext, portletConfig);
    }
}
