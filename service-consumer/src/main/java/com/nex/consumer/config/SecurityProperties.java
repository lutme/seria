package com.nex.consumer.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.security")
public class SecurityProperties {

    private JwtProperties jwt = new JwtProperties();

    public JwtProperties getJwt() {
        return jwt;
    }

    public void setJwt(JwtProperties jwt) {
        this.jwt = jwt;
    }

    public static class JwtProperties {

        private String secretBase64;

        private long expirationMinutes;

        public String getSecretBase64() {
            return secretBase64;
        }

        public void setSecretBase64(String secretBase64) {
            this.secretBase64 = secretBase64;
        }

        public Long getExpirationMinutes() {
            return expirationMinutes;
        }

        public void setExpirationMinutes(Long expirationMinutes) {
            this.expirationMinutes = expirationMinutes;
        }
    }
}
