package com.sb09.sb09moplteam2.content.batch.sport.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SportsEventResponse(
    @JsonProperty("idEvent") String idEvent,
    @JsonProperty("strEvent") String strEvent,
    @JsonProperty("strDescriptionEN") String description,
    @JsonProperty("strThumb") String thumbnail,
    @JsonProperty("dateEvent") String dateEvent,
    @JsonProperty("strLeague") String league
) {}