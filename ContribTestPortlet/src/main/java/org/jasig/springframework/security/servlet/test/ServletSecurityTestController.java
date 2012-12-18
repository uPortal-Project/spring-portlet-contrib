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
package org.jasig.springframework.security.servlet.test;

import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/index.html")
public class ServletSecurityTestController {

    @RequestMapping
    public String displayUserInfo(ModelMap model) {
        model.put("handler", "default");
        return handle(model);
    }
    
    @Secured({"ROLE_EveryoneId"})
    @RequestMapping(params = "preAuth=everyone")
    public String displayUserInfoEveryone(ModelMap model) {
        model.put("handler", "everyone");
        return handle(model);
    }
    
    @PreAuthorize("hasRole('ROLE_PortalAdministratorsName')")
    @RequestMapping(params = "preAuth=admin")
    public String displayUserInfoAdmin(ModelMap model) {
        model.put("handler", "admin");
        return handle(model);
    }

    protected String handle(ModelMap model) {
        final SecurityContext context = SecurityContextHolder.getContext();
        final Authentication authentication = context.getAuthentication();
        if (authentication != null) {
            final Object principal = authentication.getPrincipal();
    
            final String username;
            if (principal instanceof UserDetails) {
                username = ((UserDetails) principal).getUsername();
                model.put("userDetails", principal);
            } else {
                username = principal.toString();
            }
    
            model.put("username", username);
        }

        return "servletDisplayUserInfo";
    }
}
