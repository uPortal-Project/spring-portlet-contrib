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
package org.jasig.springframework.web.portlet.support;

import javax.portlet.PortletContext;
import javax.portlet.PortletRequest;

import org.jasig.springframework.web.portlet.context.ContribDispatcherPortlet;
import org.jasig.springframework.web.portlet.context.PortletApplicationContext;
import org.jasig.springframework.web.portlet.context.PortletApplicationContextUtils2;
import org.springframework.context.ApplicationContext;
import org.springframework.web.portlet.context.PortletApplicationContextUtils;

/**
 * Utility class for easy access to request-specific state which has been
 * set by the {@link org.jasig.springframework.web.portlet.context.ContribDispatcherPortlet}.
 *
 * <p>Supports lookup of current PortletApplicationContext.
 *
 * @author Eric Dalquist
 * @see ContribDispatcherPortlet
 * @version $Id: $Id
 */
public class PortletRequestContextUtils {

    /**
     * Look for the PortletApplicationContext associated with the DispatcherPortlet
     * that has initiated request processing.
     *
     * @param request current portlet request
     * @return the request-specific portlet application context
     * @throws java.lang.IllegalStateException if no portlet-specific context has been found
     */
    public static PortletApplicationContext getPortletApplicationContext(PortletRequest request)
        throws IllegalStateException {
        
        return getPortletApplicationContext(request, null);
    }

    /**
     * Look for the PortletApplicationContext associated with the DispatcherPortlet
     * that has initiated request processing, and for the global context if none
     * was found associated with the current request. This method is useful to
     * allow components outside the framework, such as JSP tag handlers,
     * to access the most specific application context available.
     *
     * @param request current portlet request
     * @param portletContext current portlet context
     * @return the request-specific PortletApplicationContext, or the global one
     * if no request-specific context has been found
     * @throws java.lang.IllegalStateException if neither a portlet-specific nor a
     * global context has been found
     */
    public static PortletApplicationContext getPortletApplicationContext(
            PortletRequest request, PortletContext portletContext) throws IllegalStateException {

        PortletApplicationContext portletApplicationContext = (PortletApplicationContext) request.getAttribute(
                ContribDispatcherPortlet.PORTLET_APPLICATION_CONTEXT_ATTRIBUTE);
        if (portletApplicationContext == null) {
            if (portletContext == null) {
                throw new IllegalStateException("No PortletApplicationContext found: not in a DispatcherPortlet request?");
            }
            portletApplicationContext = PortletApplicationContextUtils2.getRequiredPortletApplicationContext(portletContext);
        }
        
        return portletApplicationContext;
    }

    /**
     * Look for the PortletApplicationContext associated with the DispatcherPortlet
     * that has initiated request processing, for the global portlet context if none
     * was found associated with the current request, and for the global context if no
     * global portlet context was found. This method is useful to
     * allow components outside the framework, such as JSP tag handlers,
     * to access the most specific application context available.
     *
     * @param request current portlet request
     * @param portletContext current portlet context
     * @return the request-specific PortletApplicationContext, or the global one
     * if no request-specific context has been found
     * @throws java.lang.IllegalStateException if neither a portlet-specific nor a
     * global context has been found
     */
    public static ApplicationContext getWebApplicationContext(
            PortletRequest request, PortletContext portletContext) throws IllegalStateException {

        PortletApplicationContext portletApplicationContext = (PortletApplicationContext) request.getAttribute(
                ContribDispatcherPortlet.PORTLET_APPLICATION_CONTEXT_ATTRIBUTE);
        if (portletApplicationContext != null) {
            return portletApplicationContext;
        }
        
        if (portletContext == null) {
            throw new IllegalStateException("No PortletApplicationContext found: not in a DispatcherPortlet request?");
        }
        portletApplicationContext = PortletApplicationContextUtils2.getPortletApplicationContext(portletContext);
        if (portletApplicationContext != null) {
            return portletApplicationContext;
        }

        return PortletApplicationContextUtils.getRequiredWebApplicationContext(portletContext);
    }
}
