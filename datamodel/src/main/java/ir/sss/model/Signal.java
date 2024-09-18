package ir.sss.model;

import java.util.Date;

public class Signal extends AbstractFact implements Event {

    public enum Type {
        GC
    }

    private final Date timestamp;
    private final Type type;

    public Signal(Type type, Date timestamp) {
        this.type = type;
        this.timestamp = timestamp;
    }

    public Signal(Type type) {
        this(type, new Date());
    }

    public Type getType() {
        return type;
    }

    @Override
    public Date getTimestamp() {
        return timestamp;
    }
}
