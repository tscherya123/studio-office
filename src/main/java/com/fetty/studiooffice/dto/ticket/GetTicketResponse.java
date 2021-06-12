package com.fetty.studiooffice.dto.ticket;

import com.fetty.studiooffice.entity.data.CommentResponse;
import com.fetty.studiooffice.entity.data.FindUserResponse;
import com.fetty.studiooffice.entity.file.File;
import com.fetty.studiooffice.entity.ticket.Comment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class GetTicketResponse {
    private Long id;
    private String ticketName;
    private String description;
    private FindUserResponse client;
    private FindUserResponse engineer;
    private String status;
    private String serviceName;
    private Date lastModified;
    private Date created;
    private Integer grnPrice;
    private Integer calcPrice;
    private Integer serviceCount;
    private boolean payed;
    private Integer returnCount;
    private List<File> files;
    private List<CommentResponse> comments;
}
