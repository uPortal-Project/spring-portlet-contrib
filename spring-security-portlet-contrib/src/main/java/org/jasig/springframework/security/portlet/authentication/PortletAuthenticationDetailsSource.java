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
package org.jasig.springframework.security.portlet.authentication;

import javax.portlet.PortletRequest;

import org.springframework.security.authentication.AuthenticationDetailsSource;

public class PortletAuthenticationDetailsSource implements AuthenticationDetailsSource<PortletRequest, PortletAuthenticationDetails> {

    //~ Methods ========================================================================================================

    /**
     * @param context the {@code PortletRequest} object.
     * @return the {@code PortletAuthenticationDetails} containing information about the current request
     */
    public PortletAuthenticationDetails buildDetails(PortletRequest context) {
        return new PortletAuthenticationDetails(context);
    }
}
