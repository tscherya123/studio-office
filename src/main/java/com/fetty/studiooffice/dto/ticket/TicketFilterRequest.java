package com.fetty.studiooffice.dto.ticket;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class TicketFilterRequest {
    private boolean newTicket;
    private boolean inProgress;
    private boolean ready;
    private boolean finished;
    private boolean recording;
    private boolean mixing;
    private boolean mastering;
    private Date from;
    private Date to;
    private boolean lastFirst;
    private boolean newFirst;
    private boolean oldFirst;
}
