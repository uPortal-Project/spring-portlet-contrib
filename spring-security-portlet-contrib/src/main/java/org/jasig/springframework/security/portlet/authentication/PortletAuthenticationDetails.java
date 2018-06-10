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
package org.jasig.springframework.security.portlet.authentication;

import java.io.Serializable;
import java.util.Map;

import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;

import org.springframework.security.core.SpringSecurityCoreVersion;

/**
 * A holder of selected portlet details related to a web authentication request.
 *
 * @author Eric Dalquist
 * @version $Id: $Id
 */
public class PortletAuthenticationDetails implements Serializable {

    private static final long serialVersionUID = SpringSecurityCoreVersion.SERIAL_VERSION_UID;

    //~ Instance fields ================================================================================================

    private final String remoteAddress;
    private final String sessionId;
    private final Map<String, String> userInfo;

    //~ Constructors ===================================================================================================

    /**
     * Records the remote address and will also set the session Id if a session
     * already exists (it won't create one).
     *
     * @param request that the authentication request was received from
     */
    public PortletAuthenticationDetails(PortletRequest request) {
        this.remoteAddress = request.getProperty("REMOTE_ADDR");

        PortletSession session = request.getPortletSession(false);
        this.sessionId = (session != null) ? session.getId() : null;

        this.userInfo = (Map<String, String>)request.getAttribute(PortletRequest.USER_INFO);
    }

    //~ Methods ========================================================================================================

    /**
     * Indicates the TCP/IP address the authentication request was received from.
     *
     * @return the address, might be null if the portlet container does not support the REMOTE_ADDR request attribute
     */
    public String getRemoteAddress() {
        return remoteAddress;
    }

    /**
     * Indicates the PortletSession id the authentication request was received from.
     *
     * @return the session ID
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * The user info map as returned for the {@link javax.portlet.PortletRequest#USER_INFO} request attribute from
     * the authentication request.
     *
     * @return The user info map
     */
    public Map<String, String> getUserInfo() {
        return userInfo;
    }


    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((remoteAddress == null) ? 0 : remoteAddress.hashCode());
        result = prime * result + ((sessionId == null) ? 0 : sessionId.hashCode());
        result = prime * result + ((userInfo == null) ? 0 : userInfo.hashCode());
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof PortletAuthenticationDetails))
            return false;
        PortletAuthenticationDetails other = (PortletAuthenticationDetails) obj;
        if (remoteAddress == null) {
            if (other.remoteAddress != null)
                return false;
        }
        else if (!remoteAddress.equals(other.remoteAddress))
            return false;
        if (sessionId == null) {
            if (other.sessionId != null)
                return false;
        }
        else if (!sessionId.equals(other.sessionId))
            return false;
        if (userInfo == null) {
            if (other.userInfo != null)
                return false;
        }
        else if (!userInfo.equals(other.userInfo))
            return false;
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "PortletAuthenticationDetails [remoteAddress=" + remoteAddress + ", sessionId=" + sessionId
                + ", userInfo=" + userInfo + "]";
    }
}
