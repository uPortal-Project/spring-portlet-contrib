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
