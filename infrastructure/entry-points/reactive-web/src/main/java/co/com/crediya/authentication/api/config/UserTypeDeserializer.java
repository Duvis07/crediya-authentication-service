package co.com.crediya.authentication.api.config;

import co.com.crediya.authentication.model.user.UserType;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class UserTypeDeserializer extends JsonDeserializer<UserType> {

    @Override
    public UserType deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String value = p.getValueAsString();
        return UserType.fromCode(value);
    }
}
