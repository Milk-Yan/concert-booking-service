package se325.assignment01.concert.service.mapper;

import se325.assignment01.concert.common.dto.PerformerDTO;
import se325.assignment01.concert.service.domain.Performer;

/**
 * Helper class to convert between domain-model and DTO objects representing
 * Performers and PerformerDTOs. Only converts Performer to PerformerDTO
 * because performers are not created by the client side, and PerformerDTOs
 * should never be converted to a Performer.
 */
public class PerformerMapper {
    static public PerformerDTO toDto(Performer performer) {
        return (new PerformerDTO(performer.getId(),
                performer.getName(),
                performer.getImageName(),
                performer.getGenre(),
                performer.getBlurb()));
    }
}
