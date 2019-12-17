package com.parcom.exceptions.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
public class ExceptionResource {



    private String url;
    private String method;
    private String message;
    private String exceptionClass;
    private String description;

    public String toJson() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return   mapper.writeValueAsString(this);
    }

  }