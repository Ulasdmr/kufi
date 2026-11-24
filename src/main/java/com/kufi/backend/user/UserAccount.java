package com.kufi.backend.user;
import jakarta.persistence.*;
@Entity
@Table(name = "users", uniqueConstraints = @UniqueConstraint(columnNames = "email"))
public class UserAccount {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
    @Column(nullable = false, length = 190) public String email;
    @Column(nullable = false) public String passwordHash;
    @Column(nullable = false, length = 30) public String phone;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) public Role role;
    protected UserAccount() { }
    public Long getId() { return id; }
    public String getEmail() { return email; }
    public UserAccount(String email, String passwordHash, String phone, Role role) {
        this.email = email; this.passwordHash = passwordHash; this.phone = phone; this.role = role;
    }
}
