package com.fetty.studiooffice.dto.ticket;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UpdateTicketPayedRequest {
    private Long ticketId;
    private Boolean payed;
}
