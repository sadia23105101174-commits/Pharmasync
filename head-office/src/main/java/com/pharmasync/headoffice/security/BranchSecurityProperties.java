package com.pharmasync.headoffice.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Branch device credentials bound from {@code pharmasync.security.branch-credentials}
 * (environment variable {@code BRANCH_DEVICE_CREDENTIALS}).
 * Format: {@code branchId:secret,branchId:secret}.
 */
@Component
@ConfigurationProperties("pharmasync.security")
public class BranchSecurityProperties {

    private List<String> branchCredentials = List.of();

    public List<String> getBranchCredentials() {
        return branchCredentials;
    }

    public void setBranchCredentials(List<String> branchCredentials) {
        this.branchCredentials = branchCredentials != null ? branchCredentials : List.of();
    }
}
