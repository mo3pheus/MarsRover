package space.exploration.mars.rover.dataUplink;

import org.joda.time.DateTime;

public abstract class QueryService implements isSpaceQuery {

    protected String   authenticationKey;
    protected DateTime earthStartDate;
    protected DateTime earthEndDate;
    protected String   earthDate;
}