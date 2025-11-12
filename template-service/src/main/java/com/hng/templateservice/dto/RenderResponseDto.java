package com.hng.templateservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RenderResponseDto {

    private String renderedSubject;
    private String renderedBody;

}
