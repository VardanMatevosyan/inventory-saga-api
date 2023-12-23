package org.saga.inventory.dto.saga;

import java.time.LocalDateTime;
import java.util.UUID;

public abstract class EventDto {

  final UUID eventId;
  final LocalDateTime dateTime;

  public EventDto() {
    eventId = UUID.randomUUID();
    dateTime = LocalDateTime.now();
  }

}
