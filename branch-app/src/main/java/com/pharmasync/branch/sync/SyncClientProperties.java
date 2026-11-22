package com.pharmasync.branch.sync;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("pharmasync.sync")
public class SyncClientProperties {

    private String serverUrl = "http://localhost:8080";
    private int batchSize = 50;
    private long initialBackoffMs = 1000;
    private long maxBackoffMs = 30000;
    private int maxRetries = 5;
    private long fixedDelayMs = 30000;
    private String deviceSecret = "";

    public String getServerUrl() { return serverUrl; }
    public void setServerUrl(String serverUrl) { this.serverUrl = serverUrl; }
    public int getBatchSize() { return batchSize; }
    public void setBatchSize(int batchSize) { this.batchSize = batchSize; }
    public long getInitialBackoffMs() { return initialBackoffMs; }
    public void setInitialBackoffMs(long initialBackoffMs) { this.initialBackoffMs = initialBackoffMs; }
    public long getMaxBackoffMs() { return maxBackoffMs; }
    public void setMaxBackoffMs(long maxBackoffMs) { this.maxBackoffMs = maxBackoffMs; }
    public int getMaxRetries() { return maxRetries; }
    public void setMaxRetries(int maxRetries) { this.maxRetries = maxRetries; }
    public long getFixedDelayMs() { return fixedDelayMs; }
    public void setFixedDelayMs(long fixedDelayMs) { this.fixedDelayMs = fixedDelayMs; }
    public String getDeviceSecret() { return deviceSecret; }
    public void setDeviceSecret(String deviceSecret) { this.deviceSecret = deviceSecret; }
}
