package com.pharmasync.branch.config;

import com.pharmasync.branch.adapter.printer.EscPosThermalPrinterDevice;
import com.pharmasync.domain.operation.LamportClock;
import com.pharmasync.domain.strategy.FefoPickingStrategy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BranchConfig {

    @Value("${branch.id:BRANCH-001}")
    private String branchId;

    @Value("${branch.name:Downtown Central Pharmacy}")
    private String branchName;

    @Bean
    public String branchId() {
        return branchId;
    }

    @Bean
    public String branchName() {
        return branchName;
    }

    @Bean
    public LamportClock lamportClock() {
        return new LamportClock();
    }

    @Bean
    public FefoPickingStrategy fefoPickingStrategy() {
        return new FefoPickingStrategy();
    }

    @Bean
    public EscPosThermalPrinterDevice escPosThermalPrinterDevice() {
        return new EscPosThermalPrinterDevice();
    }
}
