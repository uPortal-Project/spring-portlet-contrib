package org.jasig.springframework.web.portlet.upload;

import java.util.List;

import javax.portlet.ResourceRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.fileupload.FileUploadException;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.portlet.DispatcherPortlet;
import org.springframework.web.portlet.multipart.CommonsPortletMultipartResolver;

/**
 * Since {@link DispatcherPortlet} does not permit multipart request resolving in a resource request phase and tinkering
 * {@link DispatcherPortlet} is a fragile task, typical usage of this resolver should look like this:
 * 
 * <ol>
 * <li>Define it in web application context:
 * 
 * <pre>
 *   &lt;bean id=&quot;portletMultipartResolver&quot; class=&quot;org.springframework.web.multipart.commons.CommonsPortlet2MultipartResolver&quot;&gt;
 *     &lt;property name=&quot;maxUploadSize&quot; value=&quot;5242880&quot;/&gt;
 *   &lt;/bean&gt;</pre>
 * </li>
 * <li>Autowire multipart resolver instance in a portlet controller:
 * 
 * <pre>
 * {@literal @}Autowired 
 * private CommonsPortlet2MultipartResolver multipartResolver;
 * </pre>
 * </li>
 * <li>Use it:
 * 
 * <pre>
 * if (this.multipartResolver.isMultipart(request)) {
 *    MultipartRequest multipartRequest = (MultipartRequest) this.multipartResolver.resolveMultipart(request);
 *    //... do something with multipart request
 * }
 * </pre>
 * </li>
 * </ol>
 * 
 * @author Arvīds Grabovskis
 */
public class CommonsPortlet2MultipartResolver extends CommonsPortletMultipartResolver {

    private boolean resolveLazily = false;

    @Override
    public void setResolveLazily(boolean resolveLazily) {
        this.resolveLazily = resolveLazily;
    }

    public boolean isMultipart(ResourceRequest request) {
        return (request != null && Portlet2FileUpload.isMultipartContent(request));
    }

    public MultipartResourceRequest resolveMultipart(final ResourceRequest request) throws MultipartException {
        Assert.notNull(request, "Request must not be null");
        if (this.resolveLazily) {
            return new DefaultMultipartResourceRequest(request) {
                @Override
                protected void initializeMultipart() {
                    MultipartParsingResult parsingResult = parseRequest(request);
                    setMultipartFiles(parsingResult.getMultipartFiles());
                    setMultipartParameters(parsingResult.getMultipartParameters());
                    setMultipartParameterContentTypes(parsingResult.getMultipartParameterContentTypes());
                }
            };
        } else {
            MultipartParsingResult parsingResult = parseRequest(request);
            return new DefaultMultipartResourceRequest(request, parsingResult.getMultipartFiles(),
                    parsingResult.getMultipartParameters(), parsingResult.getMultipartParameterContentTypes());
        }
    }

    protected MultipartParsingResult parseRequest(ResourceRequest request) throws MultipartException {
        String encoding = determineEncoding(request);
        FileUpload fileUpload = prepareFileUpload(encoding);
        try {
            @SuppressWarnings("unchecked")
            List<FileItem> fileItems = ((Portlet2FileUpload) fileUpload).parseRequest(request);
            return parseFileItems(fileItems, encoding);
        } catch (FileUploadBase.SizeLimitExceededException ex) {
            throw new MaxUploadSizeExceededException(fileUpload.getSizeMax(), ex);
        } catch (FileUploadException ex) {
            throw new MultipartException("Could not parse multipart portlet request", ex);
        }
    }

    protected String determineEncoding(ResourceRequest request) {
        String encoding = request.getCharacterEncoding();
        if (encoding == null) {
            encoding = getDefaultEncoding();
        }
        return encoding;
    }

    public void cleanupMultipart(MultipartResourceRequest request) {
        if (request != null) {
            try {
                cleanupFileItems(request.getMultiFileMap());
            } catch (Throwable ex) {
                logger.warn("Failed to perform multipart cleanup for portlet request", ex);
            }
        }
    }

    @Override
    protected FileUpload newFileUpload(FileItemFactory fileItemFactory) {
        return new Portlet2FileUpload(fileItemFactory);
    }
}
