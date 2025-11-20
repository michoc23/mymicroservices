package com.bustransport.route.mapper;

import com.bustransport.route.dto.response.DepartureDTO;
import com.bustransport.route.entity.Departure;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DepartureMapper {

    @Mapping(target = "scheduleId", source = "schedule.id")
    @Mapping(target = "stopId", source = "stop.id")
    @Mapping(target = "stopName", source = "stop.name")
    @Mapping(target = "routeNumber", source = "schedule.route.routeNumber")
    @Mapping(target = "routeName", source = "schedule.route.name")
    @Mapping(target = "actualDepartureTime", expression = "java(departure.getActualDepartureTime())")
    DepartureDTO toDTO(Departure departure);

    @Mapping(target = "schedule", ignore = true)
    @Mapping(target = "stop", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Departure toEntity(DepartureDTO dto);

    List<DepartureDTO> toDTOList(List<Departure> departures);
}

