package com.bustransport.route.mapper;

import com.bustransport.route.dto.response.RouteDTO;
import com.bustransport.route.entity.Route;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-19T16:51:57+0100",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.44.0.v20251118-1623, environment: Java 21.0.9 (Eclipse Adoptium)"
)
@Component
public class RouteMapperImpl implements RouteMapper {

    @Autowired
    private RouteStopMapper routeStopMapper;

    @Override
    public RouteDTO toDTO(Route route) {
        if ( route == null ) {
            return null;
        }

        RouteDTO.RouteDTOBuilder routeDTO = RouteDTO.builder();

        routeDTO.stops( routeStopMapper.toDTOList( route.getRouteStops() ) );
        routeDTO.color( route.getColor() );
        routeDTO.createdAt( route.getCreatedAt() );
        routeDTO.description( route.getDescription() );
        routeDTO.endStopId( route.getEndStopId() );
        routeDTO.estimatedDuration( route.getEstimatedDuration() );
        routeDTO.id( route.getId() );
        routeDTO.isActive( route.getIsActive() );
        routeDTO.name( route.getName() );
        routeDTO.operatorId( route.getOperatorId() );
        routeDTO.polyline( route.getPolyline() );
        routeDTO.routeNumber( route.getRouteNumber() );
        routeDTO.routeType( route.getRouteType() );
        routeDTO.startStopId( route.getStartStopId() );
        routeDTO.totalDistance( route.getTotalDistance() );
        routeDTO.updatedAt( route.getUpdatedAt() );

        return routeDTO.build();
    }

    @Override
    public Route toEntity(RouteDTO dto) {
        if ( dto == null ) {
            return null;
        }

        Route.RouteBuilder route = Route.builder();

        route.color( dto.getColor() );
        route.description( dto.getDescription() );
        route.endStopId( dto.getEndStopId() );
        route.estimatedDuration( dto.getEstimatedDuration() );
        route.id( dto.getId() );
        route.isActive( dto.getIsActive() );
        route.name( dto.getName() );
        route.operatorId( dto.getOperatorId() );
        route.polyline( dto.getPolyline() );
        route.routeNumber( dto.getRouteNumber() );
        route.routeType( dto.getRouteType() );
        route.startStopId( dto.getStartStopId() );
        route.totalDistance( dto.getTotalDistance() );

        return route.build();
    }

    @Override
    public List<RouteDTO> toDTOList(List<Route> routes) {
        if ( routes == null ) {
            return null;
        }

        List<RouteDTO> list = new ArrayList<RouteDTO>( routes.size() );
        for ( Route route : routes ) {
            list.add( toDTO( route ) );
        }

        return list;
    }
}
