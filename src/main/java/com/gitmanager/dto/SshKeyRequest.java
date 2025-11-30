package com.gitmanager.dto;

import jakarta.validation.constraints.NotBlank;

public class SshKeyRequest {

    @NotBlank(message = "Private key content is required")
    private String privateKey;

    private String passphrase;
    private String knownHosts;

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public String getPassphrase() {
        return passphrase;
    }

    public void setPassphrase(String passphrase) {
        this.passphrase = passphrase;
    }

    public String getKnownHosts() {
        return knownHosts;
    }

    public void setKnownHosts(String knownHosts) {
        this.knownHosts = knownHosts;
    }
}
