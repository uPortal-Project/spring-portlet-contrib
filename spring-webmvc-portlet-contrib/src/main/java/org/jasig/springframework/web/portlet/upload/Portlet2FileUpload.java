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

import java.util.List;

import javax.portlet.ResourceRequest;

import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.portlet.PortletFileUpload;

/**
 * <p>Portlet2FileUpload class.</p>
 */
public class Portlet2FileUpload extends PortletFileUpload {

    /**
     * <p>Constructor for Portlet2FileUpload.</p>
     */
    public Portlet2FileUpload() {
        super();
    }

    /**
     * <p>Constructor for Portlet2FileUpload.</p>
     *
     * @param fileItemFactory a {@link org.apache.commons.fileupload.FileItemFactory} object.
     */
    public Portlet2FileUpload(FileItemFactory fileItemFactory) {
        super(fileItemFactory);
    }

    /**
     * <p>parseRequest.</p>
     *
     * @param request a {@link javax.portlet.ResourceRequest} object.
     * @return a {@link java.util.List} object.
     * @throws org.apache.commons.fileupload.FileUploadException if any.
     */
    @SuppressWarnings("rawtypes")
    public List parseRequest(ResourceRequest request) throws FileUploadException {
        return parseRequest(new PortletResourceRequestContext(request));
    }

    /**
     * <p>isMultipartContent.</p>
     *
     * @param request a {@link javax.portlet.ResourceRequest} object.
     * @return a boolean.
     */
    public static final boolean isMultipartContent(ResourceRequest request) {
        return FileUploadBase.isMultipartContent(new PortletResourceRequestContext(request));
    }
}
