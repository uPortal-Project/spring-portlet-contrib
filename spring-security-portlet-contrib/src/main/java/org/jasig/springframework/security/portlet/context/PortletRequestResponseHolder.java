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
package org.jasig.springframework.security.portlet.context;

import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;

/**
 * Used to pass the incoming request to {@link PortletSecurityContextRepository#loadContext(PortletRequestResponseHolder)}
 *
 * @author Eric Dalquist
 * @since 3.0
 */
public final class PortletRequestResponseHolder {
    private final PortletRequest request;
    private final PortletResponse response;
    
    private boolean portletSessionExistedAtStartOfRequest;
    private SecurityContext contextBeforeExecution;
    private Authentication authBeforeExecution;


    public PortletRequestResponseHolder(PortletRequest request, PortletResponse response) {
        this.request = request;
        this.response = response;
    }

    public PortletRequest getRequest() {
        return request;
    }

    public PortletResponse getResponse() {
        return response;
    }

    
    boolean isPortletSessionExistedAtStartOfRequest() {
        return portletSessionExistedAtStartOfRequest;
    }

    void setPortletSessionExistedAtStartOfRequest(boolean httpSessionExistedAtStartOfRequest) {
        this.portletSessionExistedAtStartOfRequest = httpSessionExistedAtStartOfRequest;
    }

    SecurityContext getContextBeforeExecution() {
        return contextBeforeExecution;
    }

    void setContextBeforeExecution(SecurityContext contextBeforeExecution) {
        this.contextBeforeExecution = contextBeforeExecution;
    }

    Authentication getAuthBeforeExecution() {
        return authBeforeExecution;
    }

    void setAuthBeforeExecution(Authentication authBeforeExecution) {
        this.authBeforeExecution = authBeforeExecution;
    }
}
