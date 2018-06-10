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
package org.jasig.springframework.security.portlet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.portlet.PortletRequest;
import javax.portlet.filter.PortletFilter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.springframework.security.portlet.util.RequestMatcher;

/**
 * Standard implementation of {@code PortletSecurityFilterChain}.
 *
 * @author Eric Dalquist
 * @version $Id: $Id
 */
public class DefaultPortletSecurityFilterChain implements PortletSecurityFilterChain {
    private static final Log logger = LogFactory.getLog(DefaultPortletSecurityFilterChain.class);
    private final RequestMatcher requestMatcher;
    private final List<PortletFilter> filters;

    /**
     * <p>Constructor for DefaultPortletSecurityFilterChain.</p>
     *
     * @param requestMatcher a {@link org.jasig.springframework.security.portlet.util.RequestMatcher} object.
     * @param filters a {@link javax.portlet.filter.PortletFilter} object.
     */
    public DefaultPortletSecurityFilterChain(RequestMatcher requestMatcher, PortletFilter... filters) {
        this(requestMatcher, Arrays.asList(filters));
    }

    /**
     * <p>Constructor for DefaultPortletSecurityFilterChain.</p>
     *
     * @param requestMatcher a {@link org.jasig.springframework.security.portlet.util.RequestMatcher} object.
     * @param filters a {@link java.util.List} object.
     */
    public DefaultPortletSecurityFilterChain(RequestMatcher requestMatcher, List<PortletFilter> filters) {
        logger.info("Creating filter chain: " + requestMatcher + ", " + filters);
        this.requestMatcher = requestMatcher;
        this.filters = new ArrayList<PortletFilter>(filters);
    }

    /**
     * <p>Getter for the field <code>requestMatcher</code>.</p>
     *
     * @return a {@link org.jasig.springframework.security.portlet.util.RequestMatcher} object.
     */
    public RequestMatcher getRequestMatcher() {
        return requestMatcher;
    }

    /**
     * <p>Getter for the field <code>filters</code>.</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<PortletFilter> getFilters() {
        return filters;
    }

    /** {@inheritDoc} */
    public boolean matches(PortletRequest request) {
        return requestMatcher.matches(request);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "[ " + requestMatcher + ", " + filters + "]";
    }
}
