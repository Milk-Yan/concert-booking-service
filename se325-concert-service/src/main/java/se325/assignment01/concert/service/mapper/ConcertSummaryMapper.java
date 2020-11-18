package se325.assignment01.concert.service.mapper;

import se325.assignment01.concert.common.dto.ConcertSummaryDTO;
import se325.assignment01.concert.service.domain.Concert;

/**
 * Helper class to convert between domain-model and DTO objects representing
 * Concerts and ConcertSummaries(DTO). Only converts Concert to ConcertSummaryDTO
 * because concerts are not created by the client side, and ConcertSummaryDTOs
 * should never be converted to a Concert.
 */
public class ConcertSummaryMapper {
    static public ConcertSummaryDTO toDto(Concert concert) {
        return (new ConcertSummaryDTO(concert.getId(),
                concert.getTitle(),
                concert.getImageName()));
    }
}
