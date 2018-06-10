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

import javax.portlet.PortletConfig;
import javax.portlet.PortletContext;
import javax.servlet.ServletContext;

import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.web.portlet.context.ConfigurablePortletApplicationContext;

/**
 * Specialization of {@link ConfigurableEnvironment} allowing initialization of
 * portlet-related {@link org.springframework.core.env.PropertySource} objects at the
 * earliest moment the {@link PortletContext} and (optionally) {@link PortletConfig}
 * become available.
 *
 * @author Eric Dalquist
 * @see ConfigurablePortletApplicationContext#getEnvironment()
 */
public interface ConfigurablePortletEnvironment extends ConfigurableEnvironment {

    /**
     * Replace any {@linkplain
     * org.springframework.core.env.PropertySource.StubPropertySource stub property source}
     * instances acting as placeholders with real portlet context/config property sources
     * using the given parameters.
     * @param servletContext the {@link ServletContext} (may not be {@code null})
     * @param portletContext the {@link PortletContext} (may not be {@code null})
     * @param portletConfig the {@link PortletContext} ({@code null} if not available)
     */
    void initPropertySources(ServletContext servletContext, PortletContext portletContext, PortletConfig portletConfig);

}
