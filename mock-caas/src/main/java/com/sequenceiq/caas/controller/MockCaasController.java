package com.sequenceiq.caas.controller;

import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sequenceiq.cloudbreak.client.CaasUser;
import com.sequenceiq.cloudbreak.client.IntrospectResponse;

@RestController
public class MockCaasController {

    @GetMapping("/oidc/userinfo")
    public CaasUser getUserInfo() {
        CaasUser caasUser = new CaasUser();
        caasUser.setName("perdos@hortonworks.com");
        caasUser.setPreferredUsername("perdos@hortonworks.com");
        caasUser.setId("1");
        caasUser.setTenantId("manage");
        return caasUser;
    }

    @PostMapping("/oidc/introspect")
    public IntrospectResponse introSpect(String token) {
        IntrospectResponse introspectResponse = new IntrospectResponse();
        introspectResponse.setActive(true);
        introspectResponse.setAud("manage");
        return introspectResponse;
    }

    @GetMapping("/auth/in")
    public void auth(HttpServletResponse httpServletResponse, @RequestParam("redirect_uri") String requestUri) {
        httpServletResponse.setHeader("Location", requestUri);
        httpServletResponse.setStatus(302);
    }

}
