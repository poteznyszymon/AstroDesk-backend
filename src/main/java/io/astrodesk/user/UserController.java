package io.astrodesk.user;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<UserDTO> login(@RequestBody UserLoginRequest userLoginRequest,
                                         HttpServletResponse response, HttpServletRequest request) {
        UserDTO user = userService.authorizeLogin(userLoginRequest, response, request);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request, HttpServletResponse response) {
        userService.authorizeLogout(response, request);
        return ResponseEntity.ok("Logged out successfully");
    }
}
