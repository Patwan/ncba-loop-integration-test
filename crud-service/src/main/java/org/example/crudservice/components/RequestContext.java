package org.example.crudservice.components;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import java.time.Instant;

@Component
@RequestScope
@Getter
@Setter
public class RequestContext {
    private Instant requestTime;
}