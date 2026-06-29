package com.sb09.sb09moplteam2.content.batch.Sports.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SportsPageResponse(
    @JsonProperty("events") List<SportsResponse> events
) {}