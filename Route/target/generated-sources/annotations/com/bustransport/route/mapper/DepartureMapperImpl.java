package com.bustransport.route.mapper;

import com.bustransport.route.dto.response.DepartureDTO;
import com.bustransport.route.entity.Departure;
import com.bustransport.route.entity.Route;
import com.bustransport.route.entity.Schedule;
import com.bustransport.route.entity.Stop;
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
public class DepartureMapperImpl implements DepartureMapper {

    @Override
    public DepartureDTO toDTO(Departure departure) {
        if ( departure == null ) {
            return null;
        }

        DepartureDTO.DepartureDTOBuilder departureDTO = DepartureDTO.builder();

        departureDTO.scheduleId( departureScheduleId( departure ) );
        departureDTO.stopId( departureStopId( departure ) );
        departureDTO.stopName( departureStopName( departure ) );
        departureDTO.routeNumber( departureScheduleRouteRouteNumber( departure ) );
        departureDTO.routeName( departureScheduleRouteName( departure ) );
        departureDTO.arrivalTime( departure.getArrivalTime() );
        departureDTO.delayMinutes( departure.getDelayMinutes() );
        departureDTO.departureTime( departure.getDepartureTime() );
        departureDTO.id( departure.getId() );
        departureDTO.platform( departure.getPlatform() );
        departureDTO.status( departure.getStatus() );
        departureDTO.statusMessage( departure.getStatusMessage() );
        departureDTO.tripId( departure.getTripId() );

        departureDTO.actualDepartureTime( departure.getActualDepartureTime() );

        return departureDTO.build();
    }

    @Override
    public Departure toEntity(DepartureDTO dto) {
        if ( dto == null ) {
            return null;
        }

        Departure.DepartureBuilder departure = Departure.builder();

        departure.arrivalTime( dto.getArrivalTime() );
        departure.delayMinutes( dto.getDelayMinutes() );
        departure.departureTime( dto.getDepartureTime() );
        departure.id( dto.getId() );
        departure.platform( dto.getPlatform() );
        departure.status( dto.getStatus() );
        departure.statusMessage( dto.getStatusMessage() );
        departure.tripId( dto.getTripId() );

        return departure.build();
    }

    @Override
    public List<DepartureDTO> toDTOList(List<Departure> departures) {
        if ( departures == null ) {
            return null;
        }

        List<DepartureDTO> list = new ArrayList<DepartureDTO>( departures.size() );
        for ( Departure departure : departures ) {
            list.add( toDTO( departure ) );
        }

        return list;
    }

    private Long departureScheduleId(Departure departure) {
        if ( departure == null ) {
            return null;
        }
        Schedule schedule = departure.getSchedule();
        if ( schedule == null ) {
            return null;
        }
        Long id = schedule.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private Long departureStopId(Departure departure) {
        if ( departure == null ) {
            return null;
        }
        Stop stop = departure.getStop();
        if ( stop == null ) {
            return null;
        }
        Long id = stop.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String departureStopName(Departure departure) {
        if ( departure == null ) {
            return null;
        }
        Stop stop = departure.getStop();
        if ( stop == null ) {
            return null;
        }
        String name = stop.getName();
        if ( name == null ) {
            return null;
        }
        return name;
    }

    private String departureScheduleRouteRouteNumber(Departure departure) {
        if ( departure == null ) {
            return null;
        }
        Schedule schedule = departure.getSchedule();
        if ( schedule == null ) {
            return null;
        }
        Route route = schedule.getRoute();
        if ( route == null ) {
            return null;
        }
        String routeNumber = route.getRouteNumber();
        if ( routeNumber == null ) {
            return null;
        }
        return routeNumber;
    }

    private String departureScheduleRouteName(Departure departure) {
        if ( departure == null ) {
            return null;
        }
        Schedule schedule = departure.getSchedule();
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
