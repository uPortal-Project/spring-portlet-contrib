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

import java.io.IOException;

import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.portlet.PortletSession;
import javax.portlet.filter.FilterChain;

import org.jasig.springframework.web.portlet.filter.GenericPortletFilterBean;
import org.jasig.springframework.web.portlet.filter.PortletFilterUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

public class PortletSecurityContextPersistenceFilter
        extends GenericPortletFilterBean {

    static final String FILTER_APPLIED = "__spring_security_pscpf_applied";

    private PortletSecurityContextRepository repo;

    private boolean forceEagerSessionCreation = false;

    public PortletSecurityContextPersistenceFilter() {
        this(new PortletSessionSecurityContextRepository());
    }

    public PortletSecurityContextPersistenceFilter(PortletSecurityContextRepository repo) {
        this.repo = repo;
    }
    
    @Override
    protected void doCommonFilter(PortletRequest request, PortletResponse response, FilterChain chain)
            throws IOException, PortletException {
        if (request.getAttribute(FILTER_APPLIED) != null) {
            // ensure that filter is only applied once per request
            PortletFilterUtils.doFilter(request, response, chain);
            return;
        }

        final boolean debug = logger.isDebugEnabled();

        request.setAttribute(FILTER_APPLIED, Boolean.TRUE);

        if (forceEagerSessionCreation) {
            PortletSession session = request.getPortletSession();

            if (debug && session.isNew()) {
                logger.debug("Eagerly created session: " + session.getId());
            }
        }

        PortletRequestResponseHolder holder = new PortletRequestResponseHolder(request, response);
        SecurityContext contextBeforeChainExecution = repo.loadContext(holder);

        try {
            SecurityContextHolder.setContext(contextBeforeChainExecution);

            PortletFilterUtils.doFilter(holder.getRequest(), holder.getResponse(), chain);

        } finally {
            SecurityContext contextAfterChainExecution = SecurityContextHolder.getContext();
            // Crucial removal of SecurityContextHolder contents - do this before anything else.
            SecurityContextHolder.clearContext();
            repo.saveContext(contextAfterChainExecution, holder);
            request.removeAttribute(FILTER_APPLIED);

            if (debug) {
                logger.debug("SecurityContextHolder now cleared, as request processing completed");
            }
        }
    }

    public void setForceEagerSessionCreation(boolean forceEagerSessionCreation) {
        this.forceEagerSessionCreation = forceEagerSessionCreation;
    }
}
