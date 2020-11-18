package se325.assignment01.concert.service.mapper;

import se325.assignment01.concert.common.dto.BookingDTO;
import se325.assignment01.concert.common.dto.SeatDTO;
import se325.assignment01.concert.service.domain.Booking;
import se325.assignment01.concert.service.domain.Seat;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class to convert between domain-model and DTO objects representing
 * Bookings. Only converts Booking to BookingDTO because bookings are not
 * created by the client side, so we should not need to convert a BookingDTO
 * to a Booking.
 */
public class BookingMapper {

    static public BookingDTO toDto(Booking booking) {
        List<SeatDTO> seatDTOs = new ArrayList<>();

        for (Seat seat: booking.getSeats()) {
            seatDTOs.add(SeatMapper.toDto(seat));
        }

        return new BookingDTO(booking.getConcert().getId(), booking.getDate(), seatDTOs);
    }
}
