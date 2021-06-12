package com.fetty.studiooffice.dto.ticket;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UpdateTicketStatusRequest {
    private Long ticketId;
    private String newStatus;
}
