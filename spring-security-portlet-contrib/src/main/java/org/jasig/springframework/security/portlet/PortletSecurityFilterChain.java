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

import java.util.List;

import javax.portlet.PortletRequest;
import javax.portlet.filter.PortletFilter;

/**
 * Defines a filter chain which is capable of being matched against an {@code PortletRequest}.
 * in order to decide whether it applies to that request.
 * <p>
 * Used to configure a {@code PortletFilterChainProxy}.
 *
 * @author Eric Dalquist
 * @version $Id: $Id
 */
public interface PortletSecurityFilterChain {

    /**
     * <p>matches.</p>
     *
     * @param request a {@link javax.portlet.PortletRequest} object.
     * @return a boolean.
     */
    boolean matches(PortletRequest request);

    /**
     * <p>getFilters.</p>
     *
     * @return a {@link java.util.List} object.
     */
    List<PortletFilter> getFilters();
}
