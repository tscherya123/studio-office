package com.fetty.studiooffice.dto.ticket;

import com.fetty.studiooffice.entity.data.FindUserResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class TicketListResponse {
    private Long id;
    private FindUserResponse client;
    private FindUserResponse engineer;
    private String ticketName;
    private String status;
    private String serviceName;
    private Date lastModified;
    private Date created;
}
