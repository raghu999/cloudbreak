package com.sequenceiq.caas.controller;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.NotFoundException;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.MacSigner;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sequenceiq.caas.JwtSpecData;
import com.sequenceiq.cloudbreak.client.CaasUser;
import com.sequenceiq.cloudbreak.client.IntrospectResponse;

@RestController
public class MockCaasController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MockCaasController.class);

    static final MacSigner hmac = new MacSigner(JwtSpecData.HMAC_KEY);

    @Value("${username:nousername@example.com}")
    private String userName;

    @GetMapping("/oidc/userinfo")
    public CaasUser getUserInfo(HttpServletRequest request) throws JSONException {
        Cookie[] cookies = request.getCookies();
        for (Cookie cookie : cookies) {
            if ("dps-jwt".equals(cookie.getName())) {
                Jwt token = JwtHelper.decodeAndVerify(cookie.getValue(), hmac);
                JSONObject tokenClaims = new JSONObject(token.getClaims());
                String sub = tokenClaims.getString("sub");
                String aud = tokenClaims.getString("aud");
                CaasUser caasUser = new CaasUser();
                caasUser.setName(sub);
                caasUser.setPreferredUsername(userName);
                caasUser.setTenantId(aud);
                caasUser.setId(aud + '#' + sub);
                return caasUser;
            }
        }
        throw new NotFoundException("Can not retrieve user from token");
    }

    @PostMapping("/oidc/introspect")
    public IntrospectResponse introSpect(String token) {
        IntrospectResponse introspectResponse = new IntrospectResponse();
        introspectResponse.setActive(true);
        introspectResponse.setAud("manage");
        return introspectResponse;
    }

    @GetMapping("/auth/in")
    public void auth(HttpServletRequest request, HttpServletResponse httpServletResponse, @RequestParam("redirect_uri") String redirectUri) {
        LOGGER.info("redirect_uri: " + redirectUri);
        httpServletResponse.setHeader("Location", redirectUri);
        Jwt token = JwtHelper.encode("{\n" +
                "  \"sub\": \"" + userName + "\",\n" +
                "  \"aud\": \"hortonworks\",\n" +
                "  \"iss\": \"KNOXSSO\",\n" +
                "  \"exp\": " + Instant.now().plus(1, ChronoUnit.DAYS).toEpochMilli() + '\n' +
                '}', hmac);
        Cookie cookie = new Cookie("dps-jwt", token.getEncoded());
        String host = request.getHeader("Host");
        cookie.setDomain(host);
        cookie.setPath("/");
        LOGGER.info("cookie domain: " + cookie.getDomain());
        httpServletResponse.addCookie(cookie);
        httpServletResponse.setStatus(302);
    }

    private static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

}
