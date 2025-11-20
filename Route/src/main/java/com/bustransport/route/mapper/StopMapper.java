package com.bustransport.route.mapper;

import com.bustransport.route.dto.response.StopDTO;
import com.bustransport.route.entity.Stop;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface StopMapper {

    StopDTO toDTO(Stop stop);

    @Mapping(target = "routeStops", ignore = true)
    @Mapping(target = "departures", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Stop toEntity(StopDTO dto);

    List<StopDTO> toDTOList(List<Stop> stops);
}

