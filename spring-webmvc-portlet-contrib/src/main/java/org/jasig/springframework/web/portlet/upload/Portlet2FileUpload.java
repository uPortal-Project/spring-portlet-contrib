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
package org.jasig.springframework.web.portlet.upload;

import java.util.List;

import javax.portlet.ResourceRequest;

import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.portlet.PortletFileUpload;

public class Portlet2FileUpload extends PortletFileUpload {

    public Portlet2FileUpload() {
        super();
    }

    public Portlet2FileUpload(FileItemFactory fileItemFactory) {
        super(fileItemFactory);
    }

    @SuppressWarnings("rawtypes")
    public List parseRequest(ResourceRequest request) throws FileUploadException {
        return parseRequest(new PortletResourceRequestContext(request));
    }

    public static final boolean isMultipartContent(ResourceRequest request) {
        return FileUploadBase.isMultipartContent(new PortletResourceRequestContext(request));
    }
}
