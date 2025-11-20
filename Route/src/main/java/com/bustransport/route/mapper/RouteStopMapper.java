package com.bustransport.route.mapper;

import com.bustransport.route.dto.response.RouteStopDTO;
import com.bustransport.route.entity.RouteStop;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", uses = {StopMapper.class})
public interface RouteStopMapper {

    @Mapping(target = "routeId", source = "route.id")
    RouteStopDTO toDTO(RouteStop routeStop);

    @Mapping(target = "route", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    RouteStop toEntity(RouteStopDTO dto);

    List<RouteStopDTO> toDTOList(List<RouteStop> routeStops);
}

