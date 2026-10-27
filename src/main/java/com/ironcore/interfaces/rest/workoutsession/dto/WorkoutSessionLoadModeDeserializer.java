package com.ironcore.interfaces.rest.workoutsession.dto;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.ironcore.domain.workoutsession.workoutsessionset.enums.ActualLoadMode;

import java.io.IOException;

public class WorkoutSessionLoadModeDeserializer extends StdDeserializer<ActualLoadMode> {

    public WorkoutSessionLoadModeDeserializer() {
        super(ActualLoadMode.class);
    }

    @Override
    public ActualLoadMode deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        if (parser.currentToken() != JsonToken.VALUE_STRING) {
            throw JsonMappingException.from(parser, "Informe TOTAL ou PER_SIDE.");
        }
        try {
            return ActualLoadMode.valueOf(parser.getText());
        } catch (IllegalArgumentException exception) {
            throw JsonMappingException.from(parser, "Informe TOTAL ou PER_SIDE.");
        }
    }
}
