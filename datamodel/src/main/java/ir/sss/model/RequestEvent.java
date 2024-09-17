package ir.sss.model;

import org.kie.api.definition.type.Expires;
import org.kie.api.definition.type.Role;
import org.kie.api.definition.type.Timestamp;

import java.util.Date;

/**
 * Event fired when a request is logged.
 */
@Role(Role.Type.EVENT)
@Timestamp("timestamp")
@Expires("5d")
public class RequestEvent extends AbstractFact implements Event {

    private boolean considered;

    private final Date timestamp;

    private final int pluginId;
    private final int pluginSid;

    private final Host source;
    private final Host destination;

    public RequestEvent(int pluginId, int pluginSid, String srcIp, String srcPort, String dstIp, String dstPort){
        this(pluginId, pluginSid, srcIp, srcPort, dstIp, dstPort, new Date());
    }

    public RequestEvent(int pluginId, int pluginSid, String srcIp, String srcPort, String dstIp, String dstPort, Date eventTimestamp) {
        super();
        this.source = new Host(srcIp, srcPort);
        this.destination = new Host(dstIp, dstPort);
        this.timestamp = eventTimestamp;
        this.pluginId = pluginId;
        this.pluginSid = pluginSid;
        this.considered = false;
    }

    public boolean getConsidered() {
        return considered;
    }

    public void setConsidered(boolean considered) {
        this.considered = considered;
    }

    @Override
    public Date getTimestamp() {
        return timestamp;
    }

    public int getPluginId() {
        return pluginId;
    }

    public int getPluginSid() {
        return pluginSid;
    }

    public Host getDestination() {
        return destination;
    }

    public Host getSource() {
        return source;
    }

    public String getSourceIp() { return source.getIp(); }

    public String getSourcePort() { return source.getPort(); }

    public String getDestinationIp() { return destination.getIp(); }

    public String getDestinationPort() { return destination.getPort(); }

}
