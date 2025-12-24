package com.tricolori.backend.core.domain.models;

import com.tricolori.backend.shared.enums.AccountStatus;
import com.tricolori.backend.shared.enums.PersonRole;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDateTime;

@Entity(name = "Person")
@Table(name = "persons")
@Inheritance(strategy = InheritanceType.JOINED)
@Getter @Setter @NoArgsConstructor
public class Person {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private PersonRole role;

    @Column(
            name = "first_name",
            nullable = false
    )
    private String firstName;

    @Column(
            name = "last_name",
            nullable = false
    )
    private String lastName;

    @Column(
            name = "phone_num",
            nullable = false
    )
    private String phoneNum;

    @Column(
            unique = true,
            nullable = false
    )
    @Email
    private String email;

    @Column(nullable = false)
    @Length(min = 8)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_status")
    private AccountStatus accountStatus;

    @Column(
            name = "created_at",
            updatable = false,
            nullable = false
    )
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "pfp_url")
    private String pfpUrl;

    @Embedded
    private Block block;

}