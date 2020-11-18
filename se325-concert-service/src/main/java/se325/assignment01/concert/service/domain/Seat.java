package se325.assignment01.concert.service.domain;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Class to represent a Seat that can be booked. A Seat consists
 * of a unique ID (primary key), a label, the price, and the date
 * that it can be booked on.
 */
@Entity
@Table(name = "SEAT")
public class Seat {

	@Id
	@GeneratedValue
	@Column(name = "ID", nullable = false)
	private Long id;

	@Column(name = "LABEL", nullable = false)
	private String label;

	@Column(name = "PRICE", nullable = false)
	private BigDecimal price;

	@Column(name = "DATE", nullable = false)
	private LocalDateTime date;

	@Column(name = "IS_BOOKED", nullable = false)
	private boolean isBooked;

	@Version
	@Column(name = "VERSION", nullable = false)
	private long version = 1;

	public Seat(){}

	public Seat(String label, BigDecimal price) {
		this.label = label;
		this.price = price;
	}

	public Seat(String seatLabel, boolean b, LocalDateTime date, BigDecimal price) {
		this.label = seatLabel;
		this.isBooked = b;
		this.date = date;
		this.price = price;
	}

	public String getLabel() {
		return label;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public boolean isBooked() { return isBooked; }

	public void setBooked(boolean isBooked) { this.isBooked = isBooked; }

	public LocalDateTime getDate() { return date;}

	public long getId() { return id; }

}
