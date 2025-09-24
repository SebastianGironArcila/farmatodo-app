package co.com.farmatodo.api.mapper;

import co.com.farmatodo.api.dto.card.CardDTO;
import co.com.farmatodo.model.card.Card;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CardDTOMapper {
    Card toModel(CardDTO dto);}
