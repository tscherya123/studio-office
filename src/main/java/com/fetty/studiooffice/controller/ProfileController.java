package com.fetty.studiooffice.controller;

import com.fetty.studiooffice.dto.*;
import com.fetty.studiooffice.entity.auth.User;
import com.fetty.studiooffice.entity.data.JwtResponse;
import com.fetty.studiooffice.entity.data.MessageResponse;
import com.fetty.studiooffice.entity.file.EFileSrc;
import com.fetty.studiooffice.entity.file.File;
import com.fetty.studiooffice.entity.file.FileSrc;
import com.fetty.studiooffice.exception.UserImageNotFoundException;
import com.fetty.studiooffice.repository.FileRepository;
import com.fetty.studiooffice.repository.FileSrcRepository;
import com.fetty.studiooffice.repository.auth.UserRepository;
import com.fetty.studiooffice.security.jwt.JwtUtils;
import com.fetty.studiooffice.security.service.UserDetailsInfo;
import com.fetty.studiooffice.service.FileStorageService;
import com.fetty.studiooffice.util.PhoneUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/profile")
public class ProfileController {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private FileSrcRepository fileSrcRepository;

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private PasswordEncoder encoder;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtUtils jwtUtils;
    @PostMapping("/userCheckPassword")
    @PreAuthorize("hasRole('ROLE_BASIC_USER') || hasRole('ROLE_SOUND_ENGINEER') || hasRole('ROLE_ADMIN')")
    public CheckPasswordResponse userCheckPassword(@RequestBody CheckPasswordRequest request, Principal principal) {
        Optional<User> userOptional = userRepository.findByUsername(principal.getName());
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (user.getId().equals(request.getUserId())) {
                return new CheckPasswordResponse(encoder.matches(request.getPassword(), user.getPassword()));
            }
        }
        return new CheckPasswordResponse(false);
    }

    @PostMapping("/userUpdatePassword")
    @PreAuthorize("hasRole('ROLE_BASIC_USER') || hasRole('ROLE_SOUND_ENGINEER') || hasRole('ROLE_ADMIN')")
    @Transactional
    public ResponseEntity<?> userUpdatePassword(@RequestBody UpdatePasswordRequest request, Principal principal) {
        Optional<User> userOptional = userRepository.findByUsername(principal.getName());
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (user.getId().equals(request.getUserId())) {
                boolean isOldPasswordMatch = encoder.matches(request.getOldPassword(), user.getPassword());
                if (isOldPasswordMatch) {
                    user.setPassword(encoder.encode(request.getNewPassword()));
                    userRepository.save(user);
                    Authentication authentication = authenticationManager.authenticate(
                            new UsernamePasswordAuthenticationToken(user.getUsername(), request.getNewPassword()));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    String jwt = jwtUtils.generateJwtToken(authentication);
                    UserDetailsInfo userDetails = (UserDetailsInfo) authentication.getPrincipal();

                    List<String> roles = userDetails.getAuthorities().stream()
                            .map(GrantedAuthority::getAuthority)
                            .collect(Collectors.toList());

                    return ResponseEntity.ok(new JwtResponse(jwt,
                            userDetails.getId(),
                            userDetails.getFirstName(),
                            userDetails.getLastName(),
                            userDetails.getUsername(),
                            userDetails.getEmail(),
                            userDetails.getPhone(),
                            roles));
                }
            }
            return ResponseEntity.badRequest().body(new MessageResponse("Cannot change other account password"));
        }
        return ResponseEntity.badRequest().body(new MessageResponse("No user found"));
    }

    @PostMapping("/user")
    @PreAuthorize("hasRole('ROLE_BASIC_USER') || hasRole('ROLE_SOUND_ENGINEER') || hasRole('ROLE_ADMIN')")
    public ProfileInfoResponse getProfileInfo(@RequestBody ProfileInfoRequest request, Principal principal) {
        Optional<User> userOptional = userRepository.findByUsername(principal.getName());
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (user.getId().equals(request.getUserId())) {
                ProfileInfoResponse profileInfoResponse = new ProfileInfoResponse();
                profileInfoResponse.setId(user.getId());
                profileInfoResponse.setFirstName(user.getFirstName());
                profileInfoResponse.setLastName(user.getLastName());
                profileInfoResponse.setUsername(user.getUsername());
                profileInfoResponse.setEmail(user.getEmail());
                profileInfoResponse.setPhone(user.getPhone());
                profileInfoResponse.setProfileImg((user.getProfileImg() != null) ? user.getProfileImg().getUrl() : null);
                profileInfoResponse.setBirthDate(user.getBirthDate());
                profileInfoResponse.setCreated(user.getAccountCreated());
                return profileInfoResponse;
            }
        }
        throw new IllegalArgumentException("Cant get other person profile information");
    }

    @PostMapping("/userImg")
    @PreAuthorize("hasRole('ROLE_BASIC_USER') || hasRole('ROLE_SOUND_ENGINEER') || hasRole('ROLE_ADMIN')")
    public UserInfoImgResponse getUserImg(@RequestBody UserInfoImgRequest request, Principal principal) {
        Optional<User> user = userRepository.findById(request.getUserId());
        if (user.isPresent()) {
            File img = user.get().getProfileImg();
            if (img != null) {
                return new UserInfoImgResponse(request.getUserId(), img.getUrl());
            }
        }
        throw new UserImageNotFoundException("No img founded");
    }

    @PostMapping("/updateProfileImg")
    @PreAuthorize("hasRole('ROLE_BASIC_USER') || hasRole('ROLE_SOUND_ENGINEER') || hasRole('ROLE_ADMIN')")
    @Transactional
    public ResponseEntity<?> updateProfileImg(
            @RequestParam("avatarImg") MultipartFile file,
            @RequestParam("id") Long id,
            Principal principal
    ) {
        if (!file.getContentType().equals("image/jpeg")) {
            throw new IllegalArgumentException("Неверный формат.");
        }
        Optional<User> userOptional = userRepository.findByUsername(principal.getName());
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (user.getId().equals(id)) {
                String avatarFileName = fileStorageService.storeFile(file);
                String coverFileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                        .path("api/file/downloadFile/")
                        .path(avatarFileName)
                        .toUriString();
                File file1 = new File();
                file1.setName(user.getUsername() + "avatar");
                file1.setSize(file.getSize());
                file1.setSource(fileSrcRepository.findByCode(EFileSrc.OTHER).get());
                file1.setUrl(coverFileDownloadUri);
                file1.setContentType(file.getContentType());
                user.setProfileImg(fileRepository.save(file1));
                userRepository.save(user);
                return ResponseEntity.ok(new MessageResponse("ok"));
            }
        }
        throw new IllegalArgumentException("Cant change other person profile image");
    }

    @PostMapping("/updateBirthday")
    @PreAuthorize("hasRole('ROLE_BASIC_USER') || hasRole('ROLE_SOUND_ENGINEER') || hasRole('ROLE_ADMIN')")
    @Transactional
    public ResponseEntity<?> updateBirthDate(@RequestBody UpdateBirthdayRequest request, Principal principal) {
        Optional<User> userOptional = userRepository.findByUsername(principal.getName());
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (user.getId().equals(request.getUserId())) {
                user.setBirthDate(request.getDate());
                userRepository.save(user);
                return ResponseEntity.ok(new MessageResponse("ok"));
            }
        }
        throw new IllegalArgumentException("Cant get other person profile information");
    }

    @PostMapping("/updateUserInfo")
    @PreAuthorize("hasRole('ROLE_BASIC_USER') || hasRole('ROLE_SOUND_ENGINEER') || hasRole('ROLE_ADMIN')")
    @Transactional
    public ResponseEntity<?> updateUserInfo(@RequestBody UpdateUserInfoRequest request, Principal principal) {
        Optional<User> userOptional = userRepository.findByUsername(principal.getName());
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (user.getId().equals(request.getUserId())) {
                user.setFirstName(request.getFirstName());
                user.setLastName(request.getLastName());
                String normalizedEmail = request.getEmail().toLowerCase();
                if (!normalizedEmail.equals(user.getEmail())) {
                    if (!userRepository.existsByEmail(normalizedEmail)) {
                        user.setEmail(normalizedEmail);
                    } else {
                        return ResponseEntity
                                .badRequest()
                                .body(new MessageResponse("Ошибка: Email уже используется другим пользователем!"));
                    }
                }
                String normalizedPhone = PhoneUtil.toInternationalPhoneNumberFormatWithPlus(request.getPhone());
                if (!normalizedPhone.equals(user.getPhone())) {
                    if (!userRepository.existsByPhone(normalizedPhone)) {
                        user.setPhone(normalizedPhone);
                    } else {
                        return ResponseEntity
                                .badRequest()
                                .body(new MessageResponse("Ошибка: Этот телефон уже используется другим пользователем!"));
                    }
                }
                userRepository.save(user);
                return ResponseEntity.ok(new MessageResponse("ok"));
            }
        }
        return ResponseEntity
                .badRequest()
                .body(new MessageResponse("Ошибка: Нельзя изменять данные другого человека через профайл!"));
    }
}
