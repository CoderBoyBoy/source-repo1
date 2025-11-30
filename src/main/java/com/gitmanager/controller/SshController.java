package com.gitmanager.controller;

import com.gitmanager.dto.ApiResponse;
import com.gitmanager.dto.SshKeyRequest;
import com.gitmanager.service.SshService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ssh")
public class SshController {

    private final SshService sshService;

    public SshController(SshService sshService) {
        this.sshService = sshService;
    }

    @GetMapping("/status")
    public ResponseEntity<ApiResponse<SshService.SshKeyInfo>> getSshKeyStatus() {
        SshService.SshKeyInfo info = sshService.getSshKeyInfo();
        return ResponseEntity.ok(ApiResponse.success(info));
    }

    @PostMapping("/configure")
    public ResponseEntity<ApiResponse<Void>> configureSshKey(
            @Valid @RequestBody SshKeyRequest request) {
        sshService.configureSshKey(
                request.getPrivateKey(),
                request.getPassphrase(),
                request.getKnownHosts()
        );
        return ResponseEntity.ok(ApiResponse.success("SSH key configured successfully", null));
    }

    @PostMapping("/test")
    public ResponseEntity<ApiResponse<Boolean>> testSshConnection(
            @RequestParam String host,
            @RequestParam(defaultValue = "22") int port) {
        boolean success = sshService.testSshConnection(host, port);
        String message = success ? "SSH connection successful" : "SSH connection failed";
        return ResponseEntity.ok(ApiResponse.success(message, success));
    }
}
