package com.tricolori.backend.mapper;

import com.tricolori.backend.dto.notifications.NotificationDto;
import com.tricolori.backend.entity.Notification;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    NotificationDto toDto(Notification notification);

    List<NotificationDto> toDtoList(List<Notification> notifications);
}