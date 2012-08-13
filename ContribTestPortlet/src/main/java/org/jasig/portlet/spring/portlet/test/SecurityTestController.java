package org.jasig.portlet.spring.portlet.test;

import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.portlet.bind.annotation.RenderMapping;

@Controller
@RequestMapping("VIEW")
public class SecurityTestController {

    @RenderMapping
    public String displayUserInfo(ModelMap model) {
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

        return "displayUserInfo";
    }
    
//    @PreAuthorize("hasRole('ROLE_Everyone')")
    @Secured({"ROLE_Everyone"})
    @RenderMapping(params = "preAuth=everyone")
    public String displayUserInfoEveryone(ModelMap model) {
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

        return "displayUserInfo";
    }
    
//    @PreAuthorize("hasRole('ROLE_Admin')")
    @Secured({"ROLE_Admin"})
    @RenderMapping(params = "preAuth=admin")
    public String displayUserInfoAdmin(ModelMap model) {
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

        return "displayUserInfo";
    }
}
