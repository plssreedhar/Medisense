package com.epam.medisense.mcp;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface McpToolRepository extends JpaRepository<McpTool, Long> {

    Optional<McpTool> findByToolNameAndActiveTrue(String toolName);
}
