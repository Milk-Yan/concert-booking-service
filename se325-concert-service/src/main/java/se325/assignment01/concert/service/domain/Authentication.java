package se325.assignment01.concert.service.domain;

import javax.persistence.*;

/**
 * Class to represent an Authentication. An Authentication is characterised
 * by a unique authentication token (primary key), and the User it is for.
 */
@Entity
@Table(name = "AUTHENTICATIONS")
public class Authentication {

    @Id
    @Column(name = "AUTHENTICATION_TOKEN", nullable = false)
    private String authToken;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", nullable = false)
    private User user;

    public Authentication(){};

    public Authentication(String authToken, User user) {
        this.authToken = authToken;
        this.user = user;
    }

    public User getUser() {
        return user;
    }
}
