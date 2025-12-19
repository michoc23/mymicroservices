package com.bustransport.route.mapper;

import com.bustransport.route.dto.response.StopDTO;
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
public class StopMapperImpl implements StopMapper {

    @Override
    public StopDTO toDTO(Stop stop) {
        if ( stop == null ) {
            return null;
        }

        StopDTO.StopDTOBuilder stopDTO = StopDTO.builder();

        stopDTO.address( stop.getAddress() );
        stopDTO.createdAt( stop.getCreatedAt() );
        stopDTO.hasRealTimeInfo( stop.getHasRealTimeInfo() );
        stopDTO.hasShelter( stop.getHasShelter() );
        stopDTO.hasWheelchairAccess( stop.getHasWheelchairAccess() );
        stopDTO.id( stop.getId() );
        stopDTO.isActive( stop.getIsActive() );
        stopDTO.latitude( stop.getLatitude() );
        stopDTO.longitude( stop.getLongitude() );
        stopDTO.name( stop.getName() );
        stopDTO.stopCode( stop.getStopCode() );
        stopDTO.stopType( stop.getStopType() );
        stopDTO.updatedAt( stop.getUpdatedAt() );
        stopDTO.zone( stop.getZone() );

        return stopDTO.build();
    }

    @Override
    public Stop toEntity(StopDTO dto) {
        if ( dto == null ) {
            return null;
        }

        Stop.StopBuilder stop = Stop.builder();

        stop.address( dto.getAddress() );
        stop.hasRealTimeInfo( dto.getHasRealTimeInfo() );
        stop.hasShelter( dto.getHasShelter() );
        stop.hasWheelchairAccess( dto.getHasWheelchairAccess() );
        stop.id( dto.getId() );
        stop.isActive( dto.getIsActive() );
        stop.latitude( dto.getLatitude() );
        stop.longitude( dto.getLongitude() );
        stop.name( dto.getName() );
        stop.stopCode( dto.getStopCode() );
        stop.stopType( dto.getStopType() );
        stop.zone( dto.getZone() );

        return stop.build();
    }

    @Override
    public List<StopDTO> toDTOList(List<Stop> stops) {
        if ( stops == null ) {
            return null;
        }

        List<StopDTO> list = new ArrayList<StopDTO>( stops.size() );
        for ( Stop stop : stops ) {
            list.add( toDTO( stop ) );
        }

        return list;
    }
}
