package dk.aarhustech.edu.rainbow.horario;

import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import retrofit2.Converter;
import retrofit2.Retrofit;

class GsonStringConverterFactory extends Converter.Factory {
    private final Gson gson;

    private GsonStringConverterFactory(Gson gson) {
        this.gson = gson;
    }

    static GsonStringConverterFactory create(Gson gson) {
        return new GsonStringConverterFactory(gson);
    }

    @Override
    public Converter<?, String> stringConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
        TypeAdapter<?> adapter = gson.getAdapter(TypeToken.get(type));
        return new GsonStringConverter<>(adapter);
    }

    private class GsonStringConverter<T> implements Converter<T, String> {
        private final TypeAdapter<T> adapter;

        GsonStringConverter(TypeAdapter<T> adapter) {
            this.adapter = adapter;
        }

        @Override
        public String convert(@NonNull T value) throws IOException {
            return adapter.toJsonTree(value).getAsString(); // TODO: Will only work if Query parameters always are strings...
        }
    }

}