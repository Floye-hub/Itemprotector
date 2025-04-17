package com.floye.itemprotector.config;

import java.util.List;

public class ToolsConfig {
    private List<String> protectedTools;
    private String repairMessage;

    public ToolsConfig(List<String> protectedTools, String repairMessage) {
        this.protectedTools = protectedTools;
        this.repairMessage = repairMessage;
    }

    public List<String> getProtectedTools() {
        return protectedTools;
    }

    public String getRepairMessage() {
        return repairMessage;
    }
}