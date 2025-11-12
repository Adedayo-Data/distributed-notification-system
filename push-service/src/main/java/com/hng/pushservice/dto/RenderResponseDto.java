package com.hng.pushservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RenderResponseDto {

    @JsonProperty("rendered_subject")
    private String renderedSubject;

    @JsonProperty("rendered_body")
    private String renderedBody;

    @JsonProperty("rendered_image_url")
    private String renderedImageUrl;

    @JsonProperty("rendered_action_link")
    private String renderedActionLink;

}
