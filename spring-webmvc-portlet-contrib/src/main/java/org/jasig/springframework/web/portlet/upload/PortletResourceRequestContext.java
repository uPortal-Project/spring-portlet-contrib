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
package org.jasig.springframework.web.portlet.upload;

import java.io.IOException;
import java.io.InputStream;

import javax.portlet.ResourceRequest;

import org.apache.commons.fileupload.RequestContext;

public class PortletResourceRequestContext implements RequestContext {

    private ResourceRequest request;

    public PortletResourceRequestContext(ResourceRequest request) {
        this.request = request;
    }

    @Override
    public String getCharacterEncoding() {
        return request.getCharacterEncoding();
    }

    @Override
    public String getContentType() {
        return request.getContentType();
    }

    @Override
    public int getContentLength() {
        return request.getContentLength();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return request.getPortletInputStream();
    }

}
