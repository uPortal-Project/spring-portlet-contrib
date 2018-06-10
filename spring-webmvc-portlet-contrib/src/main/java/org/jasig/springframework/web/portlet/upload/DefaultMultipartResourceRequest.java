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

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.portlet.ResourceRequest;
import javax.portlet.filter.ResourceRequestWrapper;

import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

/**
 * <p>DefaultMultipartResourceRequest class.</p>
 */
public class DefaultMultipartResourceRequest extends ResourceRequestWrapper implements MultipartResourceRequest {

    private MultiValueMap<String, MultipartFile> multipartFiles;

    private Map<String, String[]> multipartParameters;

    private Map<String, String> multipartParameterContentTypes;

    /**
     * <p>Constructor for DefaultMultipartResourceRequest.</p>
     *
     * @param request a {@link javax.portlet.ResourceRequest} object.
     */
    public DefaultMultipartResourceRequest(ResourceRequest request) {
        super(request);
    }

    /**
     * <p>Constructor for DefaultMultipartResourceRequest.</p>
     *
     * @param request a {@link javax.portlet.ResourceRequest} object.
     * @param mpFiles a {@link org.springframework.util.MultiValueMap} object.
     * @param mpParams a {@link java.util.Map} object.
     * @param mpParamContentTypes a {@link java.util.Map} object.
     */
    public DefaultMultipartResourceRequest(ResourceRequest request, MultiValueMap<String, MultipartFile> mpFiles,
            Map<String, String[]> mpParams, Map<String, String> mpParamContentTypes) {
        super(request);
        setMultipartFiles(mpFiles);
        setMultipartParameters(mpParams);
        setMultipartParameterContentTypes(mpParamContentTypes);
    }

    /**
     * <p>Setter for the field <code>multipartFiles</code>.</p>
     *
     * @param multipartFiles a {@link org.springframework.util.MultiValueMap} object.
     */
    protected void setMultipartFiles(MultiValueMap<String, MultipartFile> multipartFiles) {
        this.multipartFiles = multipartFiles;
    }

    /**
     * <p>Setter for the field <code>multipartParameterContentTypes</code>.</p>
     *
     * @param multipartParameterContentTypes a {@link java.util.Map} object.
     */
    protected void setMultipartParameterContentTypes(Map<String, String> multipartParameterContentTypes) {
        this.multipartParameterContentTypes = multipartParameterContentTypes;
    }

    /**
     * <p>Setter for the field <code>multipartParameters</code>.</p>
     *
     * @param multipartParameters a {@link java.util.Map} object.
     */
    protected void setMultipartParameters(Map<String, String[]> multipartParameters) {
        this.multipartParameters = multipartParameters;
    }

    /**
     * <p>Getter for the field <code>multipartFiles</code>.</p>
     *
     * @return a {@link org.springframework.util.MultiValueMap} object.
     */
    protected MultiValueMap<String, MultipartFile> getMultipartFiles() {
        return multipartFiles;
    }

    /**
     * <p>Getter for the field <code>multipartParameters</code>.</p>
     *
     * @return a {@link java.util.Map} object.
     */
    protected Map<String, String[]> getMultipartParameters() {
        return multipartParameters;
    }

    /** {@inheritDoc} */
    @Override
    public Iterator<String> getFileNames() {
        return getMultipartFiles().keySet().iterator();
    }

    /** {@inheritDoc} */
    @Override
    public MultipartFile getFile(String name) {
        return getMultipartFiles().getFirst(name);
    }

    /** {@inheritDoc} */
    @Override
    public List<MultipartFile> getFiles(String name) {
        List<MultipartFile> multipartFiles = getMultipartFiles().get(name);
        if (multipartFiles != null) {
            return multipartFiles;
        } else {
            return Collections.emptyList();
        }
    }

    /** {@inheritDoc} */
    @Override
    public Map<String, MultipartFile> getFileMap() {
        return getMultipartFiles().toSingleValueMap();
    }

    /** {@inheritDoc} */
    @Override
    public MultiValueMap<String, MultipartFile> getMultiFileMap() {
        return getMultipartFiles();
    }

    /** {@inheritDoc} */
    @Override
    public String getMultipartContentType(String paramOrFileName) {
        MultipartFile file = getFile(paramOrFileName);
        if (file != null) {
            return file.getContentType();
        } else {
            return getMultipartParameterContentTypes().get(paramOrFileName);
        }
    }

    /**
     * <p>Getter for the field <code>multipartParameterContentTypes</code>.</p>
     *
     * @return a {@link java.util.Map} object.
     */
    protected Map<String, String> getMultipartParameterContentTypes() {
        if (this.multipartParameterContentTypes == null) {
            initializeMultipart();
        }
        return this.multipartParameterContentTypes;
    }

    /**
     * <p>initializeMultipart.</p>
     */
    protected void initializeMultipart() {
        throw new IllegalStateException("Multipart request not initialized");
    }

    /** {@inheritDoc} */
    @Override
    public Enumeration<String> getParameterNames() {
        Set<String> paramNames = new HashSet<String>();
        Enumeration<String> paramEnum = super.getParameterNames();
        while (paramEnum.hasMoreElements()) {
            paramNames.add((String) paramEnum.nextElement());
        }
        paramNames.addAll(getMultipartParameters().keySet());
        return Collections.enumeration(paramNames);
    }

    /** {@inheritDoc} */
    @Override
    public String getParameter(String name) {
        String[] values = getMultipartParameters().get(name);
        if (values != null) {
            return (values.length > 0 ? values[0] : null);
        }
        return super.getParameter(name);
    }

    /** {@inheritDoc} */
    @Override
    public String[] getParameterValues(String name) {
        String[] values = getMultipartParameters().get(name);
        if (values != null) {
            return values;
        }
        return super.getParameterValues(name);
    }

    /** {@inheritDoc} */
    @Override
    public Map<String, String[]> getParameterMap() {
        Map<String, String[]> paramMap = new HashMap<String, String[]>();
        paramMap.putAll(super.getParameterMap());
        paramMap.putAll(getMultipartParameters());
        return paramMap;
    }
}
