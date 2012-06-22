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

public class DefaultMultipartResourceRequest extends ResourceRequestWrapper implements MultipartResourceRequest {

    private MultiValueMap<String, MultipartFile> multipartFiles;

    private Map<String, String[]> multipartParameters;

    private Map<String, String> multipartParameterContentTypes;

    public DefaultMultipartResourceRequest(ResourceRequest request) {
        super(request);
    }

    public DefaultMultipartResourceRequest(ResourceRequest request, MultiValueMap<String, MultipartFile> mpFiles,
            Map<String, String[]> mpParams, Map<String, String> mpParamContentTypes) {
        super(request);
        setMultipartFiles(mpFiles);
        setMultipartParameters(mpParams);
        setMultipartParameterContentTypes(mpParamContentTypes);
    }

    protected void setMultipartFiles(MultiValueMap<String, MultipartFile> multipartFiles) {
        this.multipartFiles = multipartFiles;
    }

    protected void setMultipartParameterContentTypes(Map<String, String> multipartParameterContentTypes) {
        this.multipartParameterContentTypes = multipartParameterContentTypes;
    }

    protected void setMultipartParameters(Map<String, String[]> multipartParameters) {
        this.multipartParameters = multipartParameters;
    }

    protected MultiValueMap<String, MultipartFile> getMultipartFiles() {
        return multipartFiles;
    }

    protected Map<String, String[]> getMultipartParameters() {
        return multipartParameters;
    }

    @Override
    public Iterator<String> getFileNames() {
        return getMultipartFiles().keySet().iterator();
    }

    @Override
    public MultipartFile getFile(String name) {
        return getMultipartFiles().getFirst(name);
    }

    @Override
    public List<MultipartFile> getFiles(String name) {
        List<MultipartFile> multipartFiles = getMultipartFiles().get(name);
        if (multipartFiles != null) {
            return multipartFiles;
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public Map<String, MultipartFile> getFileMap() {
        return getMultipartFiles().toSingleValueMap();
    }

    @Override
    public MultiValueMap<String, MultipartFile> getMultiFileMap() {
        return getMultipartFiles();
    }

    @Override
    public String getMultipartContentType(String paramOrFileName) {
        MultipartFile file = getFile(paramOrFileName);
        if (file != null) {
            return file.getContentType();
        } else {
            return getMultipartParameterContentTypes().get(paramOrFileName);
        }
    }

    protected Map<String, String> getMultipartParameterContentTypes() {
        if (this.multipartParameterContentTypes == null) {
            initializeMultipart();
        }
        return this.multipartParameterContentTypes;
    }

    protected void initializeMultipart() {
        throw new IllegalStateException("Multipart request not initialized");
    }

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

    @Override
    public String getParameter(String name) {
        String[] values = getMultipartParameters().get(name);
        if (values != null) {
            return (values.length > 0 ? values[0] : null);
        }
        return super.getParameter(name);
    }

    @Override
    public String[] getParameterValues(String name) {
        String[] values = getMultipartParameters().get(name);
        if (values != null) {
            return values;
        }
        return super.getParameterValues(name);
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        Map<String, String[]> paramMap = new HashMap<String, String[]>();
        paramMap.putAll(super.getParameterMap());
        paramMap.putAll(getMultipartParameters());
        return paramMap;
    }
}
