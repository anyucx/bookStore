package com.bookstore.controller;

import com.bookstore.common.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class LogController {

    private static final Logger log = LoggerFactory.getLogger("frontend");

    @PostMapping("/api/logs/frontend")
    public ApiResponse<Void> receive(@RequestBody Map<String, Object> body) {
        String level = (String) body.getOrDefault("level", "INFO");
        String message = (String) body.getOrDefault("message", "");
        String data = body.containsKey("data") ? body.get("data").toString() : "";

        String line = message + (data.isEmpty() ? "" : " | data=" + data);

        switch (level.toUpperCase()) {
            case "DEBUG":
                log.debug(line);
                break;
            case "WARN":
                log.warn(line);
                break;
            case "ERROR":
                log.error(line);
                break;
            default:
                log.info(line);
                break;
        }
        return ApiResponse.success(null);
    }
}
