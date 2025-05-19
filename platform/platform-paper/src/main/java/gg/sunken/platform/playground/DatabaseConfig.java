package gg.sunken.platform.playground;

import lombok.Getter;

@Getter
public class DatabaseConfig {
    private String host;
    private int port;
    private Credentials credentials;

    @Override
    public String toString() {
        return "DatabaseConfig{" +
                "host='" + host + '\'' +
                ", port=" + port +
                ", credentials=" + credentials +
                '}';
    }
}

