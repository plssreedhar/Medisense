package com.epam.medisense.mcp;

import org.springframework.stereotype.Component;

@Component
public class McpToolRegistry {

    private final McpToolRepository repository;

    public McpToolRegistry(McpToolRepository repository) {
        this.repository = repository;
    }

    public McpTool getTool(String toolName) {
        return repository.findByToolNameAndActiveTrue(toolName)
                .orElseThrow(() -> new IllegalStateException("MCP tool not found or inactive: " + toolName));
    }
}
