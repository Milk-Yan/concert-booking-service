package se325.assignment01.concert.service.mapper;

import se325.assignment01.concert.common.dto.SeatDTO;
import se325.assignment01.concert.service.domain.Seat;

/**
 * Helper class to convert between domain-model and DTO objects representing
 * Seats and SeatDTOs. Only converts Seat to SeatDTO because seats are not
 * created by the client side, and a SeatDTO should never be converted to a
 * Seat.
 */
public class SeatMapper {
    static public SeatDTO toDto(Seat seat) {
        return (new SeatDTO(seat.getLabel(),
                seat.getPrice()));
    }
}
