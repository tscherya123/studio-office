package com.fetty.studiooffice.rest.auth;

import com.fetty.studiooffice.entity.auth.ERole;
import com.fetty.studiooffice.entity.auth.Role;
import com.fetty.studiooffice.entity.auth.User;
import com.fetty.studiooffice.entity.data.*;
import com.fetty.studiooffice.repository.auth.RoleRepository;
import com.fetty.studiooffice.repository.auth.UserRepository;
import com.fetty.studiooffice.security.jwt.JwtUtils;
import com.fetty.studiooffice.security.service.UserDetailsInfo;
import com.fetty.studiooffice.util.PhoneUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private JwtUtils jwtUtils;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = null;
        try {
            authentication = tryLoginDifferentWays(loginRequest);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Неверные данные для входа"));
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsInfo userDetails = (UserDetailsInfo) authentication.getPrincipal();
        if (userDetails.isBanned()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Аккаунт не подтвержден администратором или заблокирован"));
        }
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

    private Authentication tryLoginDifferentWays(LoginRequest loginRequest) {
        String userName = loginRequest.getUsername();
        if (userRepository.existsByEmail(loginRequest.getUsername())) {
            Optional<User> user = userRepository.findByEmail(loginRequest.getUsername());
            if (user.isPresent()) {
                userName = user.get().getUsername();
            }
        }
        if (userRepository.existsByPhone(loginRequest.getUsername())) {
            Optional<User> user = userRepository.findByPhone(loginRequest.getUsername());
            if (user.isPresent()) {
                userName = user.get().getUsername();
            }
        }
        return authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(userName, loginRequest.getPassword()));
    }

    @PostMapping("/signup/checkCredential")
    public ResponseEntity<?> checkCredentialValid(@Valid @RequestBody CheckCredentialValidRequest request) {
        String type = request.getType().toLowerCase();
        if (type.equals("email") || type.equals("phone") || type.equals("username")) {
            boolean exist = false;
            switch (type) {
                case "email":
                    exist = userRepository.existsByEmail(request.getCredential().toLowerCase());
                    break;
                case "phone":
                    exist = userRepository.existsByPhone(PhoneUtil.toInternationalPhoneNumberFormatWithPlus(request.getCredential()));
                    break;
                case "username":
                    exist = userRepository.existsByUsername(request.getCredential().toLowerCase());
                    break;
            }
            if (exist) {
                return ResponseEntity
                        .badRequest()
                        .body(new MessageResponse("Exist"));
            } else {
                return ResponseEntity
                        .ok(new MessageResponse("Not exist"));
            }
        }

        return ResponseEntity
                .badRequest().body(new MessageResponse("Unknown type"));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignUpRequest signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.getUsername().toLowerCase())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Username is already taken!"));
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail().toLowerCase())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Email is already in use!"));
        }

        if (userRepository.existsByPhone(PhoneUtil.toInternationalPhoneNumberFormatWithPlus(signUpRequest.getPhone()))) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Phone number is already in use!"));
        }

        // Create new user's account
        User user = new User(
                signUpRequest.getFirstName(),
                signUpRequest.getLastName(),
                signUpRequest.getUsername().toLowerCase(),
                signUpRequest.getEmail().toLowerCase(),
                PhoneUtil.toInternationalPhoneNumberFormatWithPlus(signUpRequest.getPhone()),
                encoder.encode(signUpRequest.getPassword()),
                new Date());
        user.setBanned(true);
        Set<String> strRoles = signUpRequest.getRole();
        Set<Role> roles = new HashSet<>();

        if (strRoles == null) {
            Role userRole = roleRepository.findByCode(ERole.ROLE_BASIC_USER)
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            roles.add(userRole);
        } else {
            strRoles.forEach(role -> {
                Role findRole = roleRepository.findByCode(ERole.valueOf(role)).get();
                roles.add(findRole);
            });
        }

        user.setRoles(roles);
        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }
}
