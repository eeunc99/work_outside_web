package com.app.work.config;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * ponytail: LoginController's class-level "/*" mapping swallows every single-segment
 * GET path (e.g. /manifest.json, /sw.js), shadowing the static resource handler.
 * These explicit mappings take precedence over that wildcard because Spring picks
 * the more specific pattern when multiple @RequestMapping methods match a URL.
 */
@Controller
public class PwaResourceController {

    @GetMapping(value = "/manifest.json", produces = "application/manifest+json")
    public ResponseEntity<Resource> manifest() {
        return ResponseEntity.ok(new ClassPathResource("static/manifest.json"));
    }

    @GetMapping(value = "/sw.js")
    public ResponseEntity<Resource> serviceWorker() {
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/javascript"))
                .body(new ClassPathResource("static/sw.js"));
    }
}
