package com.example.h2_update_error.model;

import javax.persistence.*;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

/**
 * @author vssavin
 */
@Entity
@Table(name = "users")
public class User {
    public static final int EXPIRATION_DAYS = 1;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String login;
    private String name;
    private String password;
    private String email;
    private String authority;
    @Column(name = "expiration_date")
    private Date expiration_date;
    @Column(name = "verification_id")
    private String verification_id;

    public User(String login, String name, String password, String email, String authority) {
        this.login = login;
        this.name = name;
        this.password = password;
        this.email = email;
        this.authority = authority;
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, EXPIRATION_DAYS);
        expiration_date = calendar.getTime();
        verification_id = UUID.randomUUID().toString();
    }

    public User() {
    }

    public Long getId() {
        return id;
    }

    public String getLogin() {
        return login;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }

    public String getAuthority() {
        return authority;
    }

    public Date getExpiration_date() {
        return expiration_date;
    }

    public Date getExpirationDate() {
        return expiration_date;
    }

    public String getVerification_id() {
        return verification_id;
    }

    public String getVerificationId() {
        return verification_id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setAuthority(String authority) {
        this.authority = authority;
    }

    public void setExpiration_date(Date expiration_date) {
        this.expiration_date = expiration_date;
    }

    public void setExpirationDate(Date expiration_date) {
        this.expiration_date = expiration_date;
    }

    public void setVerification_id(String verification_id) {
        this.verification_id = verification_id;
    }

    public void setVerificationId(String verification_id) {
        this.verification_id = verification_id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return login.equals(user.login) && email.equals(user.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(login, email);
    }

    @Override
    public String toString() {
        return "User{" +
                "login='" + login + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", authority='" + authority + '\'' +
                ", expiration_date=" + expiration_date +
                ", verification_id='" + verification_id + '\'' +
                '}';
    }
}
