/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.toolkit.web;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.opendaylight.controller.sal.utils.ServiceHelper;
import org.opendaylight.controller.usermanager.IUserManager;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/")
public class DaylightWeb {
    @RequestMapping(value = "")
    public String index(Model model, HttpServletRequest request) {
        IUserManager userManager = (IUserManager) ServiceHelper
                .getGlobalInstance(IUserManager.class, this);
        if (userManager == null) {
            return "User Manager is not available";
        }

        String username = request.getUserPrincipal().getName();

        model.addAttribute("username", username);
        model.addAttribute("role", userManager.getUserLevel(username)
                .toNumber());

        return "main";
    }

    @RequestMapping(value = "web.json")
    @ResponseBody
    public Map<String, Map<String, Object>> bundles(HttpServletRequest request) {
        Object[] instances = ServiceHelper.getGlobalInstances(
                IDaylightWeb.class, this, null);
        Map<String, Map<String, Object>> bundles = new HashMap<String, Map<String, Object>>();
        if (instances == null) {
            return bundles;
        }
        Map<String, Object> entry;
        IDaylightWeb bundle;
        String username = request.getUserPrincipal().getName();
        IUserManager userManger = (IUserManager) ServiceHelper
                .getGlobalInstance(IUserManager.class, this);
        for (Object instance : instances) {
            bundle = (IDaylightWeb) instance;
            if (userManger != null
                    && bundle.isAuthorized(userManger.getUserLevel(username))) {
                entry = new HashMap<String, Object>();
                entry.put("name", bundle.getWebName());
                entry.put("order", bundle.getWebOrder());
                bundles.put(bundle.getWebId(), entry);
            }
        }
        return bundles;
    }

    @RequestMapping(value = "logout")
    public String logout(Map<String, Object> model, final HttpServletRequest request) {

        IUserManager userManager = (IUserManager) ServiceHelper
                .getGlobalInstance(IUserManager.class, this);
        if (userManager == null) {
            return "User Manager is not available";
        }
        String username = request.getUserPrincipal().getName();
        HttpSession session = request.getSession(false);
        if (session != null) {
            if (username != null) {
                userManager.userLogout(username);
            }
            session.invalidate();

        }
        return "redirect:" + "/";
    }

    @RequestMapping(value = "login")
    public String login(Model model, final HttpServletRequest request,
            final HttpServletResponse response) {
        // response.setHeader("X-Page-Location", "/login");
        IUserManager userManager = (IUserManager) ServiceHelper
                .getGlobalInstance(IUserManager.class, this);
        if (userManager == null) {
            return "User Manager is not available";
        }

        String username = request.getUserPrincipal().getName();

        model.addAttribute("username", username);
        model.addAttribute("role", userManager.getUserLevel(username)
                .toNumber());
        return "forward:" + "/";
    }

}
