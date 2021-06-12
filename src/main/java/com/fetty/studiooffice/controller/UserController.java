package com.fetty.studiooffice.controller;

import com.fetty.studiooffice.dto.ticket.GetTicketRequest;
import com.fetty.studiooffice.dto.ticket.TicketFilterRequest;
import com.fetty.studiooffice.dto.ticket.TicketListResponse;
import com.fetty.studiooffice.dto.ticket.UpdateBlockedUserRequest;
import com.fetty.studiooffice.entity.auth.ERole;
import com.fetty.studiooffice.entity.auth.Role;
import com.fetty.studiooffice.entity.auth.User;
import com.fetty.studiooffice.entity.data.*;
import com.fetty.studiooffice.entity.file.EFileSrc;
import com.fetty.studiooffice.entity.file.File;
import com.fetty.studiooffice.entity.ticket.EServiceType;
import com.fetty.studiooffice.entity.ticket.ETicketStatus;
import com.fetty.studiooffice.entity.ticket.Service;
import com.fetty.studiooffice.entity.ticket.Ticket;
import com.fetty.studiooffice.repository.*;
import com.fetty.studiooffice.repository.auth.UserRepository;
import com.fetty.studiooffice.security.jwt.JwtUtils;
import com.fetty.studiooffice.service.FileStorageService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
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
@RequestMapping("/api/users")
public class UserController {
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
    private TicketStatusRepository ticketStatusRepository;

    @Autowired
    private PasswordEncoder encoder;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtUtils jwtUtils;

    @PostMapping("/findUsers")
    @PreAuthorize("hasRole('ROLE_BASIC_USER') || hasRole('ROLE_SOUND_ENGINEER') || hasRole('ROLE_ADMIN')")
    public List<FindUserResponse> findUsers(@RequestBody FindUserRequest request, Principal principal) {
        Optional<User> userOptional = userRepository.findByUsername(principal.getName());
        List<FindUserResponse> result = new ArrayList<>();
        String searchParam = request.getSearchQuery();

        if (userOptional.isPresent()) {
            List<User> users = userRepository.findAll();
            users = users.stream().filter(user ->
                    (request.isUsers() && user.getRoles().stream().map(Role::getCode).collect(Collectors.toList()).contains(ERole.ROLE_BASIC_USER)
                            || request.isAdmins() && user.getRoles().stream().map(Role::getCode).collect(Collectors.toList()).contains(ERole.ROLE_ADMIN)
                            || request.isEngineers() && user.getRoles().stream().map(Role::getCode).collect(Collectors.toList()).contains(ERole.ROLE_SOUND_ENGINEER))).collect(Collectors.toList());

            if (StringUtils.isNotBlank(searchParam)) {
                users = users.stream().filter(user ->
                        user.getUsername().contains(searchParam)
                                || user.getFirstName().contains(searchParam)
                                || user.getLastName().contains(searchParam)
                                || user.getEmail().contains(searchParam)).collect(Collectors.toList());
            }
            result = users.stream().limit(5).map(user -> {
                FindUserResponse response = new FindUserResponse();
                response.setId(user.getId());
                response.setFirstName(user.getFirstName());
                response.setLastName(user.getLastName());
                response.setUsername(user.getUsername());
                response.setImgLink(user.getProfileImg() != null ? user.getProfileImg().getUrl() : null);
                return response;
            }).collect(Collectors.toList());
        }
        return result;
    }

    @PostMapping("/updateBlockUser")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Transactional
    public ResponseEntity<?> updateBlockUser(@RequestBody UpdateBlockedUserRequest userR, Principal principal) {
        User user = userRepository.getById(userR.getId());
        user.setBanned(!user.isBanned());
        userRepository.save(user);
        return ResponseEntity.ok(new MessageResponse("ok"));
    }

    @PostMapping("/getUsers")
    @PreAuthorize("hasRole('ROLE_BASIC_USER') || hasRole('ROLE_SOUND_ENGINEER') || hasRole('ROLE_ADMIN')")
    public List<GetUsersResponse> getUsers(@RequestBody GetUsersRequest request, Principal principal) {
        List<User> users = userRepository.findAll();

        users = users.stream()
                .filter(user1 -> !user1.getId().equals(userRepository.findByUsername(principal.getName()).get().getId()))
                .collect(Collectors.toList());

        if (request.getId() != null) {
            users = users.stream().filter(user -> user.getId().equals(request.getId())).collect(Collectors.toList());
        }
        if (StringUtils.isNotBlank(request.getSearchQuery())) {
            users = users.stream().filter(user ->
                    user.getUsername().contains(request.getSearchQuery())
                            || user.getFirstName().contains(request.getSearchQuery())
                            || user.getLastName().contains(request.getSearchQuery())
                            || user.getEmail().contains(request.getSearchQuery())).collect(Collectors.toList());
        }
        if (StringUtils.isNotBlank(request.getUserType()) && !request.getUserType().equals("ALL")) {
            String selectedRole = "";
            if (request.getUserType().equals("ADMIN")) {
                selectedRole = ERole.ROLE_ADMIN.name();
            } else if (request.getUserType().equals("USER")) {
                selectedRole = ERole.ROLE_BASIC_USER.name();
            } else if (request.getUserType().equals("ENGINEER")) {
                selectedRole = ERole.ROLE_SOUND_ENGINEER.name();
            }
            String finalSelectedRole = selectedRole;
            users = users.stream()
                    .filter(user -> user
                            .getRoles()
                            .stream()
                            .map(Role::getCode)
                            .map(ERole::name)
                            .collect(Collectors.toList())
                            .contains(finalSelectedRole))
                    .collect(Collectors.toList());
        }
        return users.stream().map((user) -> {
            GetUsersResponse usersResponse = new GetUsersResponse();
            usersResponse.setId(user.getId());
            usersResponse.setUsername(user.getUsername());
            usersResponse.setFirstName(user.getFirstName());
            usersResponse.setLastName(user.getLastName());
            usersResponse.setEmail(user.getEmail());
            usersResponse.setPhone(user.getPhone());
            usersResponse.setBirthDate(user.getBirthDate());
            usersResponse.setCreated(user.getAccountCreated());
            usersResponse.setBanned(user.isBanned());
            usersResponse.setImgLink(user.getProfileImg() != null ? user.getProfileImg().getUrl() : null);
            long countOfTickets = ticketRepository.findAll().stream().filter(ticket -> {
                return ticket.getClient() != null && ticket.getClient().getId().equals(user.getId());
            }).count();
            usersResponse.setTicketCount((int) countOfTickets);

            int sum = ticketRepository.findAll().stream().filter(ticket -> {
                return ticket.getClient() != null && ticket.getClient().getId().equals(user.getId())
                        && ticket.getTicketStatus().getCode().equals(ETicketStatus.FINISHED)
                        && ticket.isPayed();
            }).mapToInt(Ticket::getGrnPrice).sum();
            usersResponse.setBringMoney(sum);
            return usersResponse;
        }).collect(Collectors.toList());
    }
}
