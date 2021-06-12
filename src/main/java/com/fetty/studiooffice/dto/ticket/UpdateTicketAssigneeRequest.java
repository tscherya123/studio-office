package com.fetty.studiooffice.dto.ticket;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UpdateTicketAssigneeRequest {
    private Long ticketId;
    private Long userId;
    private Boolean isUser;
}
