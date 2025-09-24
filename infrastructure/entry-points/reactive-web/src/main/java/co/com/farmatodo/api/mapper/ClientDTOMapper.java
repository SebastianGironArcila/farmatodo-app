package co.com.farmatodo.api.mapper;

import co.com.farmatodo.api.dto.client.ClientDTO;
import co.com.farmatodo.api.dto.client.CreateClientDTO;
import co.com.farmatodo.model.client.Client;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ClientDTOMapper {
    Client toModel(CreateClientDTO dto);

    ClientDTO toResponse(Client client);
}
