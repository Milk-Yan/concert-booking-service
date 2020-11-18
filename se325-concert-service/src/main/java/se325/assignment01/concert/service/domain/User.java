package se325.assignment01.concert.service.domain;

import javax.persistence.*;
import java.util.Objects;
import java.util.Set;

/**
 * Class to represent a User. A User consists of a
 * unique ID (primary key), a username, and a password.
 */
@Entity
@Table(name = "USERS")
public class User {

    @Id
    @GeneratedValue
    @Column(name = "ID", nullable = false)
    private Long id;

    @Column(name = "USERNAME", unique=true, nullable = false)
    private String username;

    @Column(name = "PASSWORD", nullable = false)
    private String password;

    @Version
    @Column(name = "VERSION")
    private long version;

    // bookings should be refreshed as the user is refreshed to
    // get the most recent state from the database.
    @OneToMany(mappedBy = "user", cascade = CascadeType.REFRESH)
    private Set<Booking> bookings;

    public User(){}

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public long getId() { return id; }

    public Set<Booking> getBookings() { return bookings; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;

        return id.equals(user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
