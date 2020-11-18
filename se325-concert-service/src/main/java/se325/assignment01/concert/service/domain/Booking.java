package se325.assignment01.concert.service.domain;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;

/**
 * Class to represent a Booking. A Booking consists of a
 * unique ID (primary key), the concert, the date, and seats
 * that the booking books, and the user that makes the booking.
 */
@Entity
@Table(name = "BOOKINGS")
public class Booking {

    @Id
    @GeneratedValue
    @Column(name = "ID", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CONCERT_ID", nullable = false)
    private Concert concert;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", nullable = false)
    private User user;

    @Column(name = "DATE", nullable = false)
    private LocalDateTime date;

    // we want the seats to be re-read from the database if our booking refreshes,
    // as we may have a different state for our seats.
    @OneToMany(cascade = CascadeType.REFRESH)
    private Set<Seat> seats;

    public Booking(){}

    public Booking(Concert concert, User user, LocalDateTime date, Set<Seat> seats) {
        this.concert = concert;
        this.user = user;
        this.date = date;
        this.seats = seats;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Booking booking = (Booking) o;
        return id.equals(booking.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public Concert getConcert() {
        return concert;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public Set<Seat> getSeats() {
        return seats;
    }

    public long getId() { return id; }

    public User getUser() { return user; }

}
