package util;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.Duration;

public class DurationAdapter extends TypeAdapter<Duration> {

    @Override
    public void write(JsonWriter out, Duration value) throws IOException {
        if (value == null) {
            out.nullValue();
        } else {
            out.value(value.toString()); // например: PT30M
        }
    }

    @Override
    public Duration read(JsonReader in) throws IOException {
        String durationString = in.nextString();
        return Duration.parse(durationString);
    }
}