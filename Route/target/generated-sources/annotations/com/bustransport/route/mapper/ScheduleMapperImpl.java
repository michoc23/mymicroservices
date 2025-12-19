package com.bustransport.route.mapper;

import com.bustransport.route.dto.response.ScheduleDTO;
import com.bustransport.route.entity.Route;
import com.bustransport.route.entity.Schedule;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-19T16:51:57+0100",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.44.0.v20251118-1623, environment: Java 21.0.9 (Eclipse Adoptium)"
)
@Component
public class ScheduleMapperImpl implements ScheduleMapper {

    @Override
    public ScheduleDTO toDTO(Schedule schedule) {
        if ( schedule == null ) {
            return null;
        }

        ScheduleDTO.ScheduleDTOBuilder scheduleDTO = ScheduleDTO.builder();

        scheduleDTO.routeId( scheduleRouteId( schedule ) );
        scheduleDTO.routeName( scheduleRouteName( schedule ) );
        scheduleDTO.endTime( schedule.getEndTime() );
        scheduleDTO.firstDeparture( schedule.getFirstDeparture() );
        scheduleDTO.frequency( schedule.getFrequency() );
        scheduleDTO.id( schedule.getId() );
        scheduleDTO.isActive( schedule.getIsActive() );
        scheduleDTO.lastDeparture( schedule.getLastDeparture() );
        scheduleDTO.notes( schedule.getNotes() );
        scheduleDTO.serviceType( schedule.getServiceType() );
        scheduleDTO.startTime( schedule.getStartTime() );
        scheduleDTO.validFrom( schedule.getValidFrom() );
        scheduleDTO.validUntil( schedule.getValidUntil() );

        return scheduleDTO.build();
    }

    @Override
    public Schedule toEntity(ScheduleDTO dto) {
        if ( dto == null ) {
            return null;
        }

        Schedule.ScheduleBuilder schedule = Schedule.builder();

        schedule.endTime( dto.getEndTime() );
        schedule.firstDeparture( dto.getFirstDeparture() );
        schedule.frequency( dto.getFrequency() );
        schedule.id( dto.getId() );
        schedule.isActive( dto.getIsActive() );
        schedule.lastDeparture( dto.getLastDeparture() );
        schedule.notes( dto.getNotes() );
        schedule.serviceType( dto.getServiceType() );
        schedule.startTime( dto.getStartTime() );
        schedule.validFrom( dto.getValidFrom() );
        schedule.validUntil( dto.getValidUntil() );

        return schedule.build();
    }

    @Override
    public List<ScheduleDTO> toDTOList(List<Schedule> schedules) {
        if ( schedules == null ) {
            return null;
        }

        List<ScheduleDTO> list = new ArrayList<ScheduleDTO>( schedules.size() );
        for ( Schedule schedule : schedules ) {
            list.add( toDTO( schedule ) );
        }

        return list;
    }

    private Long scheduleRouteId(Schedule schedule) {
        if ( schedule == null ) {
            return null;
        }
        Route route = schedule.getRoute();
        if ( route == null ) {
            return null;
        }
        Long id = route.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String scheduleRouteName(Schedule schedule) {
        if ( schedule == null ) {
            return null;
        }
        Route route = schedule.getRoute();
        if ( route == null ) {
            return null;
        }
        String name = route.getName();
        if ( name == null ) {
            return null;
        }
        return name;
    }
}
