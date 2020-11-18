package se325.assignment01.concert.service.domain;

import java.time.LocalDateTime;
import java.util.*;

import javax.persistence.*;

/**
 * Class to represent a Concert. A Concert consists of a unique ID,
 * a title, an image name, a blurb, and the dates that the concert
 * is on.
 */
@Entity
@Table(name = "CONCERTS")
public class Concert {

    @Id
    @GeneratedValue
    @Column(name = "ID", nullable = false)
    private Long id;

    @Column(name = "TITLE", nullable = false)
    private String title;

    @Column(name = "IMAGE_NAME")
    private String imageName;

    @Column(name = "BLURB", columnDefinition = "VARCHAR(2048)")
    private String blurb;

    @ElementCollection
    @CollectionTable(
            name = "CONCERT_DATES",
            joinColumns = @JoinColumn(name = "CONCERT_ID")
    )
    @Column(name = "DATE")
    private Set<LocalDateTime> dates;

    @ManyToMany
    @JoinTable(
            name = "CONCERT_PERFORMER",
            joinColumns = @JoinColumn(name = "CONCERT_ID"),
            inverseJoinColumns = @JoinColumn(name = "PERFORMER_ID")
    )
    private Set<Performer> performers;

    public Concert() {}

    public Concert(long id, String title, String imageName, String blurb) {
        this.id = id;
        this.title = title;
        this.imageName = imageName;
        this.blurb = blurb;
        this.dates = new HashSet<>();
        this.performers = new HashSet<>();
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getImageName() {
        return imageName;
    }

    public String getBlurb() {
        return blurb;
    }

    public Set<LocalDateTime> getDates() {
        return dates;
    }

    public Set<Performer> getPerformers() {
        return performers;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Concert concert = (Concert) o;
        return id.equals(concert.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
