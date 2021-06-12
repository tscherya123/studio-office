package com.fetty.studiooffice.controller;

import com.fetty.studiooffice.dto.*;
import com.fetty.studiooffice.dto.ticket.*;
import com.fetty.studiooffice.entity.auth.ERole;
import com.fetty.studiooffice.entity.auth.Role;
import com.fetty.studiooffice.entity.auth.User;
import com.fetty.studiooffice.entity.data.CommentResponse;
import com.fetty.studiooffice.entity.data.FindUserResponse;
import com.fetty.studiooffice.entity.data.JwtResponse;
import com.fetty.studiooffice.entity.data.MessageResponse;
import com.fetty.studiooffice.entity.file.EFileSrc;
import com.fetty.studiooffice.entity.file.File;
import com.fetty.studiooffice.entity.ticket.*;
import com.fetty.studiooffice.exception.UserImageNotFoundException;
import com.fetty.studiooffice.repository.*;
import com.fetty.studiooffice.repository.auth.UserRepository;
import com.fetty.studiooffice.security.jwt.JwtUtils;
import com.fetty.studiooffice.security.service.UserDetailsInfo;
import com.fetty.studiooffice.service.FileStorageService;
import com.fetty.studiooffice.util.PhoneUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/ticket")
public class TicketController {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private FileSrcRepository fileSrcRepository;

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private ServiceTypeRepository serviceTypeRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private TicketRepository ticketRepository;
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private TicketStatusRepository ticketStatusRepository;

    @Autowired
    private PasswordEncoder encoder;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtUtils jwtUtils;

    @PostMapping("/create")
    @PreAuthorize("hasRole('ROLE_BASIC_USER') || hasRole('ROLE_SOUND_ENGINEER') || hasRole('ROLE_ADMIN')")
    @Transactional
    public ResponseEntity<?> createTicket(
            @RequestParam(name = "file", required = false) MultipartFile[] files,
            @RequestParam("serviceCode") String serviceCode,
            @RequestParam("serviceCount") Integer serviceCount,
            @RequestParam("ticketName") String ticketName,
            @RequestParam(name = "description", required = false) String description,
            @RequestParam("calcPrice") Integer calcPrice,
            @RequestParam(name = "isPayed", required = false) boolean isPayed,
            @RequestParam(name = "grnPrice", required = false) Integer grnPrice,
            @RequestParam(name = "engId", required = false) Long engId,
            @RequestParam(name = "userId", required = false) Long userId,
            Principal principal
    ) {
        if (StringUtils.isBlank(ticketName)) {
            return ResponseEntity.badRequest().body(new MessageResponse("Пустое название недопустимо"));
        }
        Optional<User> userOptional = userRepository.findByUsername(principal.getName());
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            boolean isUser = user.getRoles().stream().map(Role::getCode).filter(code -> code.equals(ERole.ROLE_BASIC_USER)).count() == 1l;
            Set <File> ticketFiles = new HashSet<>();
            if (files != null) {
                Arrays.asList(files).stream().forEach(file -> {
                    String fileName = fileStorageService.storeFile(file);
                    String fileNameUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                            .path("api/file/downloadFile/")
                            .path(fileName)
                            .toUriString();
                    File file1 = new File();
                    file1.setName(fileName);
                    file1.setSize(file.getSize());
                    file1.setSource(fileSrcRepository.findByCode(isUser ? EFileSrc.USER_FILE : EFileSrc.ENGINEER_FILE).get());
                    file1.setUrl(fileNameUri);
                    file1.setContentType(file.getContentType());
                    ticketFiles.add(fileRepository.save(file1));
                });
            }
            Service service = new Service();
            service.setApproximateGrnPrice(calcPrice);
            service.setServiceType(serviceTypeRepository.findByCode(EServiceType.valueOf(serviceCode)).get());
            service.setCount(serviceCount);
            service.setName(ticketName);

            userRepository.getById(userId);

            Ticket ticket = new Ticket();
            ticket.setClient(isUser ? user : null);
            ticket.setEngineer(isUser ? null : user);
            if (!userId.equals((long) -1)) {
                ticket.setClient(userRepository.getById(userId));
            }
            if (!engId.equals((long) -1)) {
                ticket.setEngineer(userRepository.getById(engId));
            }

            ticket.setCreated(new Date());
            ticket.setLastModified(ticket.getCreated());
            ticket.setFiles(ticketFiles);
            ticket.setPayed(isPayed);
            ticket.setName(ticketName);
            ticket.setDescription(description);
            ticket.setService(serviceRepository.save(service));
            ticket.setGrnPrice(grnPrice.longValue() == -1l ? null : grnPrice);
            ticket.setReturnCount(0);
            ticket.setTicketStatus(ticketStatusRepository.findByCode(ETicketStatus.NEW).get());

            return ResponseEntity.ok(ticketRepository.save(ticket));
        }
        return ResponseEntity.badRequest().body(new MessageResponse("failed"));
    }

    @PostMapping("/getAllTickets")
    @PreAuthorize("hasRole('ROLE_BASIC_USER') || hasRole('ROLE_SOUND_ENGINEER') || hasRole('ROLE_ADMIN')")
    public List<TicketListResponse> getAllTickets(@RequestBody TicketAllFilterRequest request, Principal principal) {
        Optional<User> userOptional = userRepository.findByUsername(principal.getName());
        List<TicketListResponse> result = new ArrayList<>();
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            List<Ticket> resultTickets = ticketRepository.findAll().stream()
                    .filter(ticket -> ticket.getCreated().after(request.getFrom())
                                    && ticket.getCreated().before(request.getTo())
                                    && (request.isFinished() && ticket.getTicketStatus().getCode().equals(ETicketStatus.FINISHED)
                                    || request.isInProgress() && ticket.getTicketStatus().getCode().equals(ETicketStatus.IN_PROGRESS)
                                    || request.isReady() && ticket.getTicketStatus().getCode().equals(ETicketStatus.RETURNED)
                                    || request.isNewTicket() && ticket.getTicketStatus().getCode().equals(ETicketStatus.NEW)
                            )
                                    && (request.isRecording() && ticket.getService().getServiceType().getCode().equals(EServiceType.SOUND_RECORDING)
                                    || request.isMixing() && ticket.getService().getServiceType().getCode().equals(EServiceType.MIXING)
                                    || request.isMastering() && ticket.getService().getServiceType().getCode().equals(EServiceType.MASTERING)
                            )
                    ).collect(Collectors.toList());
            resultTickets = resultTickets.stream().filter(ticket -> {
                if(request.getUserId() == -1l && request.getEngineerId() == -1l) {
                    return true;
                }
                if (request.getUserId() != -1l && request.getEngineerId() != -1l) {
                    boolean canPassUser = true;
                    if (request.getUserId() != -1l) {
                        if (ticket.getClient() == null) {
                            canPassUser = false;
                        } else {
                            canPassUser = ticket.getClient().getId().equals(request.getUserId());
                        }
                    }
                    boolean canPassEngineer = true;
                    if (request.getEngineerId() != -1l) {
                        if (ticket.getEngineer() == null) {
                            canPassEngineer = false;
                        } else {
                            canPassEngineer = ticket.getEngineer().getId().equals(request.getEngineerId());

                        }
                    }
                    return canPassUser && canPassEngineer;
                }
                if(request.getUserId() == -1l) {
                    boolean canPassEngineer = true;
                    if (request.getEngineerId() != -1l) {
                        if (ticket.getEngineer() == null) {
                            canPassEngineer = false;
                        } else {
                            canPassEngineer = ticket.getEngineer().getId().equals(request.getEngineerId());

                        }
                    }
                    return canPassEngineer;
                }
                if(request.getEngineerId() == -1l) {
                    boolean canPassUser = true;
                    if (request.getUserId() != -1l) {
                        if (ticket.getClient() == null) {
                            canPassUser = false;
                        } else {
                            canPassUser = ticket.getClient().getId().equals(request.getUserId());
                        }
                    }
                    return canPassUser;
                }
                return false;
            }).collect(Collectors.toList());
            if (request.isLastFirst()) {
                resultTickets.sort(Comparator.comparing(Ticket::getLastModified).reversed());
            }
            if (request.isNewFirst()) {
                resultTickets.sort(Comparator.comparing(Ticket::getCreated).reversed());
            }
            if (request.isOldFirst()) {
                resultTickets.sort(Comparator.comparing(Ticket::getCreated));
            }
            result = resultTickets.stream().map(item -> {
                TicketListResponse response = new TicketListResponse();
                response.setId(item.getId());
                response.setStatus(item.getTicketStatus().getCode().name());
                response.setTicketName(item.getName());
                response.setCreated(item.getCreated());
                response.setLastModified(item.getLastModified());
                String serviceName = "";
                switch (item.getService().getServiceType().getCode())   {
                    case MIXING:
                        serviceName = "Сведение";
                        break;
                    case MASTERING:
                        serviceName = "Мастеринг";
                        break;
                    case SOUND_RECORDING:
                        serviceName = "Запись";
                        break;
                }
                response.setServiceName(serviceName);
                User client = item.getClient();
                if (client != null) {
                    response.setClient(formatUserResponse(client));
                }
                User engineer = item.getEngineer();
                if (engineer != null) {
                    response.setEngineer(formatUserResponse(engineer));
                }
                return response;
            }).collect(Collectors.toList());
        }
        return result;
//        return new CheckPasswordResponse(false);
    }

    @PostMapping("/getUserTickets")
    @PreAuthorize("hasRole('ROLE_BASIC_USER') || hasRole('ROLE_SOUND_ENGINEER') || hasRole('ROLE_ADMIN')")
    public List<TicketListResponse> getTicketsForListCurrentUser(@RequestBody TicketFilterRequest request, Principal principal) {
        Optional<User> userOptional = userRepository.findByUsername(principal.getName());
        List<TicketListResponse> result = new ArrayList<>();
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            boolean isUser = user.getRoles().stream().map(Role::getCode).filter(code -> code.equals(ERole.ROLE_BASIC_USER)).count() == 1l;
            boolean isEnginner = user.getRoles().stream().map(Role::getCode).filter(code -> code.equals(ERole.ROLE_SOUND_ENGINEER)).count() == 1l;

            List<Ticket> resultTickets = ticketRepository.findAll().stream()
                    .filter(ticket ->
                            (ticket.getClient() != null && ticket.getClient().getId().equals(user.getId()) || ticket.getEngineer() != null && !isUser && ticket.getEngineer().getId().equals(user.getId()))
                            && ticket.getCreated().after(request.getFrom())
                            && ticket.getCreated().before(request.getTo())
                            && (request.isFinished() && ticket.getTicketStatus().getCode().equals(ETicketStatus.FINISHED)
                                || request.isInProgress() && ticket.getTicketStatus().getCode().equals(ETicketStatus.IN_PROGRESS)
                                || request.isReady() && ticket.getTicketStatus().getCode().equals(ETicketStatus.RETURNED)
                                || request.isNewTicket() && ticket.getTicketStatus().getCode().equals(ETicketStatus.NEW)
                            )
                            && (request.isRecording() && ticket.getService().getServiceType().getCode().equals(EServiceType.SOUND_RECORDING)
                                    || request.isMixing() && ticket.getService().getServiceType().getCode().equals(EServiceType.MIXING)
                                    || request.isMastering() && ticket.getService().getServiceType().getCode().equals(EServiceType.MASTERING)
                            )
                    ).collect(Collectors.toList());
            if (request.isLastFirst()) {
                resultTickets.sort(Comparator.comparing(Ticket::getLastModified).reversed());
            }
            if (request.isNewFirst()) {
                resultTickets.sort(Comparator.comparing(Ticket::getCreated).reversed());
            }
            if (request.isOldFirst()) {
                resultTickets.sort(Comparator.comparing(Ticket::getCreated));
            }
            result = resultTickets.stream().map(item -> {
                TicketListResponse response = new TicketListResponse();
                response.setId(item.getId());
                response.setStatus(item.getTicketStatus().getCode().name());
                response.setTicketName(item.getName());
                response.setCreated(item.getCreated());
                response.setLastModified(item.getLastModified());
                String serviceName = "";
                switch (item.getService().getServiceType().getCode())   {
                    case MIXING:
                        serviceName = "Сведение";
                        break;
                    case MASTERING:
                        serviceName = "Мастеринг";
                        break;
                    case SOUND_RECORDING:
                        serviceName = "Запись";
                        break;
                }
                response.setServiceName(serviceName);
                User client = item.getClient();
                if (client != null) {
                    response.setClient(formatUserResponse(client));
                }
                User engineer = item.getEngineer();
                if (engineer != null) {
                    response.setEngineer(formatUserResponse(engineer));
                }
                return response;
            }).collect(Collectors.toList());
        }
        return result;
//        return new CheckPasswordResponse(false);
    }

    @PostMapping("/getFreeTickets")
    @PreAuthorize("hasRole('ROLE_BASIC_USER') || hasRole('ROLE_SOUND_ENGINEER') || hasRole('ROLE_ADMIN')")
    public List<TicketListResponse> getFreeTicketsForList(@RequestBody TicketFilterRequest request, Principal principal) {
        Optional<User> userOptional = userRepository.findByUsername(principal.getName());
        List<TicketListResponse> result = new ArrayList<>();
        if (userOptional.isPresent()) {
            User user = userOptional.get();

            List<Ticket> resultTickets = ticketRepository.findAll().stream()
                    .filter(ticket -> ticket.getEngineer() == null
                                    && ticket.getCreated().after(request.getFrom())
                                    && ticket.getCreated().before(request.getTo())
                                    && (request.isFinished() && ticket.getTicketStatus().getCode().equals(ETicketStatus.FINISHED)
                                    || request.isInProgress() && ticket.getTicketStatus().getCode().equals(ETicketStatus.IN_PROGRESS)
                                    || request.isReady() && ticket.getTicketStatus().getCode().equals(ETicketStatus.RETURNED)
                                    || request.isNewTicket() && ticket.getTicketStatus().getCode().equals(ETicketStatus.NEW)
                            )
                                    && (request.isRecording() && ticket.getService().getServiceType().getCode().equals(EServiceType.SOUND_RECORDING)
                                    || request.isMixing() && ticket.getService().getServiceType().getCode().equals(EServiceType.MIXING)
                                    || request.isMastering() && ticket.getService().getServiceType().getCode().equals(EServiceType.MASTERING)
                            )
                    ).collect(Collectors.toList());
            if (request.isLastFirst()) {
                resultTickets.sort(Comparator.comparing(Ticket::getLastModified).reversed());
            }
            if (request.isNewFirst()) {
                resultTickets.sort(Comparator.comparing(Ticket::getCreated).reversed());
            }
            if (request.isOldFirst()) {
                resultTickets.sort(Comparator.comparing(Ticket::getCreated));
            }
            result = resultTickets.stream().map(item -> {
                TicketListResponse response = new TicketListResponse();
                response.setId(item.getId());
                response.setStatus(item.getTicketStatus().getCode().name());
                response.setTicketName(item.getName());
                response.setCreated(item.getCreated());
                response.setLastModified(item.getLastModified());
                String serviceName = "";
                switch (item.getService().getServiceType().getCode())   {
                    case MIXING:
                        serviceName = "Сведение";
                        break;
                    case MASTERING:
                        serviceName = "Мастеринг";
                        break;
                    case SOUND_RECORDING:
                        serviceName = "Запись";
                        break;
                }
                response.setServiceName(serviceName);
                User client = item.getClient();
                if (client != null) {
                    response.setClient(formatUserResponse(client));
                }
                User engineer = item.getEngineer();
                if (engineer != null) {
                    response.setEngineer(formatUserResponse(engineer));
                }
                return response;
            }).collect(Collectors.toList());
        }
        return result;
//        return new CheckPasswordResponse(false);
    }

    @PostMapping("/updateTicketAssignee")
    @PreAuthorize("hasRole('ROLE_BASIC_USER') || hasRole('ROLE_SOUND_ENGINEER') || hasRole('ROLE_ADMIN')")
    @Transactional
    public ResponseEntity<?> updateTicketAssignee(@RequestBody UpdateTicketAssigneeRequest request, Principal principal) {
        Optional<User> userOptional = userRepository.findByUsername(principal.getName());
        if (userOptional.isPresent()) {
            User user = userRepository.getById(request.getUserId());
            Ticket ticket = ticketRepository.getById(request.getTicketId());
            if (request.getIsUser()) {
                ticket.setClient(user);
            } else {
                ticket.setEngineer(user);
            }
            ticketRepository.save(ticket);
            return ResponseEntity.ok().body(new MessageResponse("ok"));
        }
        return ResponseEntity.badRequest().body(new MessageResponse("failed"));
    }

    @PostMapping("/updateTicketStatus")
    @PreAuthorize("hasRole('ROLE_BASIC_USER') || hasRole('ROLE_SOUND_ENGINEER') || hasRole('ROLE_ADMIN')")
    @Transactional
    public ResponseEntity<?> updateTicketStatus(@RequestBody UpdateTicketStatusRequest request, Principal principal) {
        Optional<User> userOptional = userRepository.findByUsername(principal.getName());
        if (userOptional.isPresent()) {
            Ticket ticket = ticketRepository.getById(request.getTicketId());
            Optional<TicketStatus> status = ticketStatusRepository.findByCode(ETicketStatus.valueOf(request.getNewStatus()));
            ticket.setTicketStatus(status.get());
            if (ETicketStatus.valueOf(request.getNewStatus()).equals(ETicketStatus.RETURNED)) {
                ticket.setReturnCount(ticket.getReturnCount() + 1);
            }
            ticketRepository.save(ticket);
            return ResponseEntity.ok().body(new MessageResponse("ok"));
        }
        return ResponseEntity.badRequest().body(new MessageResponse("failed"));
    }

    @PostMapping("/updateTicketPayed")
    @PreAuthorize("hasRole('ROLE_BASIC_USER') || hasRole('ROLE_SOUND_ENGINEER') || hasRole('ROLE_ADMIN')")
    @Transactional
    public ResponseEntity<?> updateTicketPayed(@RequestBody UpdateTicketPayedRequest request, Principal principal) {
        Optional<User> userOptional = userRepository.findByUsername(principal.getName());
        if (userOptional.isPresent()) {
            Ticket ticket = ticketRepository.getById(request.getTicketId());
            ticket.setPayed(request.getPayed().booleanValue());
            ticketRepository.save(ticket);
            return ResponseEntity.ok().body(new MessageResponse("ok"));
        }
        return ResponseEntity.badRequest().body(new MessageResponse("failed"));
    }

    @PostMapping("/updateTicketPayedSum")
    @PreAuthorize("hasRole('ROLE_BASIC_USER') || hasRole('ROLE_SOUND_ENGINEER') || hasRole('ROLE_ADMIN')")
    @Transactional
    public ResponseEntity<?> updateTicketPayedSum(@RequestBody UpdatePayedSumRequest request, Principal principal) {
        Optional<User> userOptional = userRepository.findByUsername(principal.getName());
        if (userOptional.isPresent()) {
            Ticket ticket = ticketRepository.getById(request.getTicketId());
            ticket.setGrnPrice(request.getPayedSum());
            ticketRepository.save(ticket);
            return ResponseEntity.ok().body(new MessageResponse("ok"));
        }
        return ResponseEntity.badRequest().body(new MessageResponse("failed"));
    }

    @PostMapping("/getTicketById")
    @PreAuthorize("hasRole('ROLE_BASIC_USER') || hasRole('ROLE_SOUND_ENGINEER') || hasRole('ROLE_ADMIN')")
    public GetTicketResponse getTicketById(@RequestBody GetTicketRequest request, Principal principal) {
        Optional<User> userOptional = userRepository.findByUsername(principal.getName());
        GetTicketResponse response = new GetTicketResponse();
        if (userOptional.isPresent()) {
            Optional<Ticket> ticketOpt = ticketRepository.findById(request.getId());
            if (ticketOpt.isPresent()) {
                Ticket ticket = ticketOpt.get();
                response.setId(ticket.getId());
                response.setTicketName(ticket.getName());
                response.setDescription(ticket.getDescription());
                response.setPayed(ticket.isPayed());
                response.setStatus(ticket.getTicketStatus().getCode().name());
                response.setFiles(ticket.getFiles().stream().collect(Collectors.toList()));
                response.setGrnPrice(ticket.getGrnPrice());
                response.setCreated(ticket.getCreated());
                response.setLastModified(ticket.getLastModified());
                response.setReturnCount(ticket.getReturnCount());
                response.setCalcPrice(ticket.getService().getApproximateGrnPrice());
                response.setServiceCount(ticket.getService().getCount());
                String serviceName = "";
                switch (ticket.getService().getServiceType().getCode())   {
                    case MIXING:
                        serviceName = "Сведение";
                        break;
                    case MASTERING:
                        serviceName = "Мастеринг";
                        break;
                    case SOUND_RECORDING:
                        serviceName = "Запись";
                        break;
                }
                response.setServiceName(serviceName);

                User client = ticket.getClient();
                if (client != null) {
                    response.setClient(formatUserResponse(client));
                }
                User engineer = ticket.getEngineer();
                if (engineer != null) {
                    response.setEngineer(formatUserResponse(engineer));
                }
                List<CommentResponse> commentResponses = ticket.getComments().stream().map(comment -> {
                    CommentResponse response1 = new CommentResponse();
                    response1.setId(comment.getId());
                    response1.setFiles(comment.getFiles());
                    response1.setComment(comment.getComment());
                    response1.setCreated(comment.getCreated());
                    response1.setUser(formatUserResponse(comment.getUser()));
                    return response1;
                }).collect(Collectors.toList());
                commentResponses.sort(Comparator.comparing(CommentResponse::getCreated).reversed());
                response.setComments(commentResponses);
            }
        }
        return response;
    }

    private FindUserResponse formatUserResponse(User user) {
        FindUserResponse userResponse = new FindUserResponse();
        userResponse.setId(user.getId());
        userResponse.setImgLink(user.getProfileImg() != null ? user.getProfileImg().getUrl() : null);
        userResponse.setUsername(user.getUsername());
        userResponse.setLastName(user.getLastName());
        userResponse.setFirstName(user.getFirstName());
        return userResponse;
    }


    @PostMapping("/createComment")
    @PreAuthorize("hasRole('ROLE_BASIC_USER') || hasRole('ROLE_SOUND_ENGINEER') || hasRole('ROLE_ADMIN')")
    @Transactional
    public ResponseEntity<?> createComment(
            @RequestParam(name = "files", required = false) MultipartFile[] files,
            @RequestParam("comment") String comment,
            @RequestParam("idTicket") Long idTicket,
            Principal principal
    ) {
        if (StringUtils.isBlank(comment)) {
            return ResponseEntity.badRequest().body(new MessageResponse("Пустой комментарий"));
        }
        Optional<User> userOptional = userRepository.findByUsername(principal.getName());
        if (userOptional.isPresent()) {
            Optional<Ticket> ticketOpt = ticketRepository.findById(idTicket);
            if (ticketOpt.isPresent()) {
                Comment comment1 = new Comment();
                comment1.setComment(comment);
                comment1.setUser(userOptional.get());
                Set <File> commentFiles = new HashSet<>();
                if (files != null) {
                    Arrays.asList(files).stream().forEach(file -> {
                        String fileName = fileStorageService.storeFile(file);
                        String fileNameUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                                .path("api/file/downloadFile/")
                                .path(fileName)
                                .toUriString();
                        File file1 = new File();
                        file1.setName(fileName);
                        file1.setSize(file.getSize());
                        file1.setSource(fileSrcRepository.findByCode(EFileSrc.OTHER).get());
                        file1.setUrl(fileNameUri);
                        file1.setContentType(file.getContentType());
                        commentFiles.add(fileRepository.save(file1));
                    });
                }
                comment1.setFiles(commentFiles);
                comment1.setCreated(new Date());
                Ticket ticket = ticketOpt.get();

                Set<Comment> comments = ticket.getComments();
                comments.add(commentRepository.save(comment1));
                ticket.setComments(comments);
                ticketRepository.save(ticket);
                return ResponseEntity.ok(new MessageResponse("ok"));
            }
        }
        return ResponseEntity.badRequest().body(new MessageResponse("failed"));
    }
}
