package se325.assignment01.concert.service.domain;

import se325.assignment01.concert.common.types.Genre;

import javax.persistence.*;
import java.util.Objects;

/**
 * Class to represent a Performer. A Performer consists of a
 * unique ID (primary key), a name, an image, a genre, and a blurb.
 */
@Entity
@Table(name = "PERFORMERS")
public class Performer {

    @Id
    @GeneratedValue
    @Column(name = "ID", nullable = false)
    private Long id;

    @Column(name = "NAME", nullable = false)
    private String name;

    @Column(name = "IMAGE_NAME")
    private String imageName;

    @Enumerated(EnumType.STRING)
    @Column(name = "GENRE")
    private Genre genre;

    @Column(name = "BLURB", columnDefinition = "VARCHAR(2048)")
    private String blurb;

    public Performer(){}

    public Performer(long id, String name, String imageName, Genre genre, String blurb) {
        this.id = id;
        this.name = name;
        this.imageName = imageName;
        this.genre = genre;
        this.blurb = blurb;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getImageName() {
        return imageName;
    }

    public Genre getGenre() {
        return genre;
    }

    public String getBlurb() {
        return blurb;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Performer performer = (Performer) o;
        return id.equals(performer.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
