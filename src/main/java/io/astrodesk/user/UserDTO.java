package io.astrodesk.user;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDTO {
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private UserRole role;
}
