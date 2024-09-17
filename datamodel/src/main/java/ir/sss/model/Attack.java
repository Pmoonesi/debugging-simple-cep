package ir.sss.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Attack extends AbstractFact implements Event {

    private final int sid;
    private final int rid;
    private final Scenario scenario;
    private final Host source;
    private final Host destination;
    private final Date timestamp;
    private ArrayList<Event> events;

//    public Attack(int sid, int rid, Scenario scenario, Host source, Host destination) {
//        this(sid, rid, scenario, source, destination, new Date());
//    }

    public Attack(int sid, int rid, Scenario scenario, Host source, Host destination, Date timestamp) {
        super();
        this.sid = sid;
        this.rid = rid;
        this.scenario = scenario;
        this.source = source;
        this.destination = destination;
        this.timestamp = timestamp;
    }

    @Override
    public Date getTimestamp() {
        return timestamp;
    }

    public int getSid() {
        return sid;
    }

    public void setEvents(ArrayList<Event> events) {
        this.events = events;
    }

    public void setEvents(List<Event> events) {
        this.events = new ArrayList<>(events);
    }

    public ArrayList<Event> getEvents() {
        return events;
    }

    public Scenario getScenario() {
        return scenario;
    }

    public int getRid() {
        return rid;
    }

    public Host getSource() {
        return source;
    }

    public Host getDestination() {
        return destination;
    }

    public String getSourceIp() {
        if (source != null)
            return source.getIp();
        return "UNDEFINED";
    }

    public String getSourcePort() {
        if (source != null)
            return source.getPort();
        return "UNDEFINED";
    }

    public String getDestinationIp() { return destination.getIp(); }

    public String getDestinationPort() { return destination.getPort(); }
}
