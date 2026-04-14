package io.astrodesk.user;

import io.astrodesk.ticket.TicketEntity;
import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "Users")
public class DbUserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    @OneToMany(mappedBy = "author", fetch = FetchType.LAZY)
    private List<TicketEntity> userTickets;

    @OneToMany(mappedBy = "assignedTo", fetch = FetchType.LAZY)
    private List<TicketEntity> assigneeTickets;


    public DbUserEntity() {}

    public DbUserEntity(Long id, String username, String firstName, String lastName, String email, UserRole role) {
        this.userId = id;
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.role = role;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public UserRole getRole() {
        return role;
    }
}
