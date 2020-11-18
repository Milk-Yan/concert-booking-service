package se325.assignment01.concert.service.mapper;

import se325.assignment01.concert.common.dto.ConcertDTO;
import se325.assignment01.concert.common.dto.PerformerDTO;
import se325.assignment01.concert.service.domain.Concert;
import se325.assignment01.concert.service.domain.Performer;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Helper class to convert between domain-model and DTO objects representing
 * Concerts. Only converts Concert to ConcertDTO because concerts are not
 * created by the client side, so we should not need to convert a ConcertDTO
 * to a Concert.
 */
public class ConcertMapper {

    static public ConcertDTO toDto(Concert concert) {
        ConcertDTO concertDTO = new ConcertDTO(concert.getId(),
                concert.getTitle(),
                concert.getImageName(),
                concert.getBlurb());
        concertDTO.setDates(new ArrayList<>(concert.getDates()));
        concertDTO.setPerformers(performerListToDto(concert.getPerformers()));

        return concertDTO;
    }

    /**
     * Helper method to convert all the performers in the list to their
     * DTO form.
     * @param performerList domain-model performer list.
     * @return DTO object performer list.
     */
    static private List<PerformerDTO> performerListToDto(Set<Performer> performerList) {
        List<PerformerDTO> performerDTOList = new ArrayList<>();

        for (Performer performer: performerList) {
            performerDTOList.add(PerformerMapper.toDto(performer));
        }

        return performerDTOList;
    }
}
