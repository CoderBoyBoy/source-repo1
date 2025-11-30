package com.gitmanager.service;

import com.gitmanager.config.SshConfig;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.eclipse.jgit.transport.ssh.jsch.JschConfigSessionFactory;
import org.eclipse.jgit.transport.ssh.jsch.OpenSshConfig;
import org.eclipse.jgit.util.FS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class SshService {

    private static final Logger logger = LoggerFactory.getLogger(SshService.class);

    private final SshConfig sshConfig;
    private SshSessionFactory sshSessionFactory;
    private String customPrivateKey;
    private String customPassphrase;
    private String customKnownHosts;

    public SshService(SshConfig sshConfig) {
        this.sshConfig = sshConfig;
        initializeSshSessionFactory();
    }

    private void initializeSshSessionFactory() {
        this.sshSessionFactory = new JschConfigSessionFactory() {
            @Override
            protected void configure(OpenSshConfig.Host host, Session session) {
                session.setConfig("StrictHostKeyChecking", "no");
            }

            @Override
            protected JSch createDefaultJSch(FS fs) throws JSchException {
                JSch jsch = super.createDefaultJSch(fs);
                
                // Add custom private key if configured
                if (customPrivateKey != null && !customPrivateKey.isEmpty()) {
                    try {
                        Path tempKeyFile = Files.createTempFile("ssh_key", null);
                        Files.writeString(tempKeyFile, customPrivateKey);
                        tempKeyFile.toFile().deleteOnExit();
                        
                        if (customPassphrase != null && !customPassphrase.isEmpty()) {
                            jsch.addIdentity(tempKeyFile.toString(), customPassphrase);
                        } else {
                            jsch.addIdentity(tempKeyFile.toString());
                        }
                    } catch (IOException e) {
                        logger.error("Failed to load custom SSH key: {}", e.getMessage());
                    }
                } else if (sshConfig.getPrivateKeyPath() != null) {
                    // Use configured private key path
                    File keyFile = new File(sshConfig.getPrivateKeyPath());
                    if (keyFile.exists()) {
                        if (sshConfig.getPassphrase() != null && !sshConfig.getPassphrase().isEmpty()) {
                            jsch.addIdentity(keyFile.getAbsolutePath(), sshConfig.getPassphrase());
                        } else {
                            jsch.addIdentity(keyFile.getAbsolutePath());
                        }
                    }
                }
                
                // Add custom known hosts if configured
                if (customKnownHosts != null && !customKnownHosts.isEmpty()) {
                    try {
                        Path tempKnownHostsFile = Files.createTempFile("known_hosts", null);
                        Files.writeString(tempKnownHostsFile, customKnownHosts);
                        tempKnownHostsFile.toFile().deleteOnExit();
                        jsch.setKnownHosts(tempKnownHostsFile.toString());
                    } catch (IOException e) {
                        logger.error("Failed to load custom known hosts: {}", e.getMessage());
                    }
                } else if (sshConfig.getKnownHostsPath() != null) {
                    File knownHostsFile = new File(sshConfig.getKnownHostsPath());
                    if (knownHostsFile.exists()) {
                        jsch.setKnownHosts(knownHostsFile.getAbsolutePath());
                    }
                }
                
                return jsch;
            }
        };
    }

    public SshSessionFactory getSshSessionFactory() {
        return sshSessionFactory;
    }

    public void configureSshKey(String privateKey, String passphrase, String knownHosts) {
        this.customPrivateKey = privateKey;
        this.customPassphrase = passphrase;
        this.customKnownHosts = knownHosts;
        initializeSshSessionFactory();
        logger.info("SSH key configuration updated");
    }

    public boolean testSshConnection(String host, int port) {
        try {
            JSch jsch = new JSch();
            
            if (customPrivateKey != null && !customPrivateKey.isEmpty()) {
                Path tempKeyFile = Files.createTempFile("ssh_key", null);
                Files.writeString(tempKeyFile, customPrivateKey);
                tempKeyFile.toFile().deleteOnExit();
                
                if (customPassphrase != null && !customPassphrase.isEmpty()) {
                    jsch.addIdentity(tempKeyFile.toString(), customPassphrase);
                } else {
                    jsch.addIdentity(tempKeyFile.toString());
                }
            } else if (sshConfig.getPrivateKeyPath() != null) {
                File keyFile = new File(sshConfig.getPrivateKeyPath());
                if (keyFile.exists()) {
                    if (sshConfig.getPassphrase() != null) {
                        jsch.addIdentity(keyFile.getAbsolutePath(), sshConfig.getPassphrase());
                    } else {
                        jsch.addIdentity(keyFile.getAbsolutePath());
                    }
                }
            }

            Session session = jsch.getSession("git", host, port);
            session.setConfig("StrictHostKeyChecking", "no");
            session.setTimeout(5000);
            session.connect();
            session.disconnect();
            
            logger.info("SSH connection test successful to {}:{}", host, port);
            return true;
        } catch (JSchException | IOException e) {
            logger.error("SSH connection test failed to {}:{} - {}", host, port, e.getMessage());
            return false;
        }
    }

    public SshKeyInfo getSshKeyInfo() {
        SshKeyInfo info = new SshKeyInfo();
        
        if (customPrivateKey != null && !customPrivateKey.isEmpty()) {
            info.setConfigured(true);
            info.setSource("custom");
        } else if (sshConfig.getPrivateKeyPath() != null) {
            File keyFile = new File(sshConfig.getPrivateKeyPath());
            info.setConfigured(keyFile.exists());
            info.setSource("file:" + sshConfig.getPrivateKeyPath());
        } else {
            info.setConfigured(false);
            info.setSource("none");
        }
        
        return info;
    }

    public static class SshKeyInfo {
        private boolean configured;
        private String source;

        public boolean isConfigured() {
            return configured;
        }

        public void setConfigured(boolean configured) {
            this.configured = configured;
        }

        public String getSource() {
            return source;
        }

        public void setSource(String source) {
            this.source = source;
        }
    }
}
