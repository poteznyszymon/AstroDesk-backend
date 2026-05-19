package io.astrodesk.network.mapper;

import io.astrodesk.network.dto.NetworkDtos.NetworkHistoryResponse;
import io.astrodesk.network.dto.NetworkDtos.NetworkItemResponse;
import io.astrodesk.network.entity.NetworkDevice;
import io.astrodesk.network.entity.NetworkHistory;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface NetworkMapper {

    NetworkItemResponse toItemResponse(NetworkDevice device);

    NetworkHistoryResponse toHistoryResponse(NetworkHistory history);
}
