package com.bustransport.route.mapper;

import com.bustransport.route.dto.response.RouteDTO;
import com.bustransport.route.entity.Route;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", uses = {RouteStopMapper.class})
public interface RouteMapper {

    @Mapping(target = "stops", source = "routeStops")
    RouteDTO toDTO(Route route);

    @Mapping(target = "routeStops", ignore = true)
    @Mapping(target = "schedules", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Route toEntity(RouteDTO dto);

    List<RouteDTO> toDTOList(List<Route> routes);
}

