package ir.sss.model;

import java.util.Objects;

public class Host{

    private final String ip;
    private final String port;

    public Host(String ip, String port) {
        this.ip = ip;
        this.port = port;
    }

    public String getIp() {
        return ip;
    }

    public String getPort() {
        return port;
    }

    @Override
    public int hashCode() {
        return Objects.hash(ip, port);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Host host = (Host) o;
        return Objects.equals(ip, host.ip) && Objects.equals(port, host.port);
    }

    @Override
    public String toString(){
        return ip + ':' + port;
    }
}
