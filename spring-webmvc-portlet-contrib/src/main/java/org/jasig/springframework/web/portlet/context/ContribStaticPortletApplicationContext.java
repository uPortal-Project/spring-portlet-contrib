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
package org.jasig.springframework.web.portlet.context;

import org.springframework.web.portlet.context.ConfigurablePortletApplicationContext;
import org.springframework.web.portlet.context.StaticPortletApplicationContext;

/**
 * Exists simply to add {@link PortletApplicationContext} into the type hierarchy of
 * StaticPortletApplicationContext. This class should be deleted when this functionality
 * gets added into Spring proper and {@link ConfigurablePortletApplicationContext} 
 * implements {@link PortletApplicationContext}
 * 
 * @author Eric Dalquist
 */
public class ContribStaticPortletApplicationContext extends StaticPortletApplicationContext implements
        PortletApplicationContext {
    
}
