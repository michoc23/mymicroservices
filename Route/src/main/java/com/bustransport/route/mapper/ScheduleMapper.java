package com.bustransport.route.mapper;

import com.bustransport.route.dto.response.ScheduleDTO;
import com.bustransport.route.entity.Schedule;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ScheduleMapper {

    @Mapping(target = "routeId", source = "route.id")
    @Mapping(target = "routeName", source = "route.name")
    ScheduleDTO toDTO(Schedule schedule);

    @Mapping(target = "route", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Schedule toEntity(ScheduleDTO dto);

    List<ScheduleDTO> toDTOList(List<Schedule> schedules);
}

