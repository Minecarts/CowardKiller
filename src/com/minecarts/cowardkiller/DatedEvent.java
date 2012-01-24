package com.minecarts.cowardkiller;

import org.bukkit.event.Event;
import java.util.Date;

public class DatedEvent {
    protected final Event event;
    protected final Date date;
    
    public DatedEvent(Event event) {
        this(event, new Date());
    }
    public DatedEvent(Event event, Date date) {
        this.event = event;
        this.date = date;
    }
    
    public Event getEvent() {
        return event;
    }
    public Date getDate() {
        return date;
    }
    
    public long elapsed() {
        return new Date().getTime() - date.getTime();
    }
}
