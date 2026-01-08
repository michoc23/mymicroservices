package com.bustransport.route.mapper;

import com.bustransport.route.dto.response.RouteStopDTO;
import com.bustransport.route.entity.Route;
import com.bustransport.route.entity.RouteStop;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-19T16:51:56+0100",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.44.0.v20251118-1623, environment: Java 21.0.9 (Eclipse Adoptium)"
)
@Component
public class RouteStopMapperImpl implements RouteStopMapper {

    @Autowired
    private StopMapper stopMapper;

    @Override
    public RouteStopDTO toDTO(RouteStop routeStop) {
        if ( routeStop == null ) {
            return null;
        }

        RouteStopDTO.RouteStopDTOBuilder routeStopDTO = RouteStopDTO.builder();

        routeStopDTO.routeId( routeStopRouteId( routeStop ) );
        routeStopDTO.distanceFromStart( routeStop.getDistanceFromStart() );
        routeStopDTO.dwellTime( routeStop.getDwellTime() );
        routeStopDTO.id( routeStop.getId() );
        routeStopDTO.stop( stopMapper.toDTO( routeStop.getStop() ) );
        routeStopDTO.stopSequence( routeStop.getStopSequence() );
        routeStopDTO.timeFromStart( routeStop.getTimeFromStart() );

        return routeStopDTO.build();
    }

    @Override
    public RouteStop toEntity(RouteStopDTO dto) {
        if ( dto == null ) {
            return null;
        }

        RouteStop.RouteStopBuilder routeStop = RouteStop.builder();

        routeStop.distanceFromStart( dto.getDistanceFromStart() );
        routeStop.dwellTime( dto.getDwellTime() );
        routeStop.id( dto.getId() );
        routeStop.stop( stopMapper.toEntity( dto.getStop() ) );
        routeStop.stopSequence( dto.getStopSequence() );
        routeStop.timeFromStart( dto.getTimeFromStart() );

        return routeStop.build();
    }

    @Override
    public List<RouteStopDTO> toDTOList(List<RouteStop> routeStops) {
        if ( routeStops == null ) {
            return null;
        }

        List<RouteStopDTO> list = new ArrayList<RouteStopDTO>( routeStops.size() );
        for ( RouteStop routeStop : routeStops ) {
            list.add( toDTO( routeStop ) );
        }

        return list;
    }

    private Long routeStopRouteId(RouteStop routeStop) {
        if ( routeStop == null ) {
            return null;
        }
        Route route = routeStop.getRoute();
        if ( route == null ) {
            return null;
        }
        Long id = route.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }
}
