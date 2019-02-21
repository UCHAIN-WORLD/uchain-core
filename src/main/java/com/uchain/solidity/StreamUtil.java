
package com.uchain.solidity;

import java.util.Optional;
import java.util.stream.Stream;

public class StreamUtil {

    public static <T> Stream<T> streamOf(T value) {
        return Optional.ofNullable(value)
                .map(Stream::of)
                .orElseGet(Stream::empty);
    }
}
