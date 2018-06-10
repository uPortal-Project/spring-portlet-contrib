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

import java.io.IOException;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.EventRequest;
import javax.portlet.EventResponse;
import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import javax.portlet.filter.FilterChain;


/**
 * Holds objects associated with a Portlet filter.<P>Guarantees the request and response are instances of
 * <code>PortletRequest</code> and <code>PortletResponse</code>, and that there are no <code>null</code>
 * objects.
 * <p>
 * Required so that security system classes can obtain access to the filter environment, as well as the request
 * and response.
 *
 * @author Eric Dalquist
 * @version $Id: $Id
 */
public class PortletFilterInvocation {
   //~ Static fields ==================================================================================================
    static final FilterChain DUMMY_CHAIN = new FilterChain() {
        @Override
        public void doFilter(ActionRequest request, ActionResponse response) throws IOException, PortletException {
            throw new UnsupportedOperationException("Dummy filter chain");
        }
        @Override
        public void doFilter(EventRequest request, EventResponse response) throws IOException, PortletException {
            throw new UnsupportedOperationException("Dummy filter chain");
        }
        @Override
        public void doFilter(RenderRequest request, RenderResponse response) throws IOException, PortletException {
            throw new UnsupportedOperationException("Dummy filter chain");
        }
        @Override
        public void doFilter(ResourceRequest request, ResourceResponse response) throws IOException, PortletException {
            throw new UnsupportedOperationException("Dummy filter chain");
        }
    };

   //~ Instance fields ================================================================================================

   private FilterChain chain;
   private PortletRequest request;
   private PortletResponse response;

   //~ Constructors ===================================================================================================

   /**
    * <p>Constructor for PortletFilterInvocation.</p>
    *
    * @param request a {@link javax.portlet.PortletRequest} object.
    * @param response a {@link javax.portlet.PortletResponse} object.
    * @param chain a {@link javax.portlet.filter.FilterChain} object.
    */
   public PortletFilterInvocation(PortletRequest request, PortletResponse response, FilterChain chain) {
       if ((request == null) || (response == null) || (chain == null)) {
           throw new IllegalArgumentException("Cannot pass null values to constructor");
       }

       this.request = (PortletRequest) request;
       this.response = (PortletResponse) response;
       this.chain = chain;
   }

   //~ Methods ========================================================================================================

   /**
    * <p>Getter for the field <code>chain</code>.</p>
    *
    * @return a {@link javax.portlet.filter.FilterChain} object.
    */
   public FilterChain getChain() {
       return chain;
   }

   /**
    * <p>Getter for the field <code>request</code>.</p>
    *
    * @return a {@link javax.portlet.PortletRequest} object.
    */
   public PortletRequest getRequest() {
       return request;
   }

   /**
    * <p>Getter for the field <code>response</code>.</p>
    *
    * @return a {@link javax.portlet.PortletResponse} object.
    */
   public PortletResponse getResponse() {
       return response;
   }

   /**
    * <p>toString.</p>
    *
    * @return a {@link java.lang.String} object.
    */
   public String toString() {
       return "PortletFilterInvocation: URL: " + request;
   }
}
