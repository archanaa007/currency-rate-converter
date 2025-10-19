package pl.cleankod.util;

import java.util.function.Function;

public abstract class Result<T, E> {
    private Result() {}

    public abstract boolean isSuccess();
    public abstract boolean isFailure();
    public abstract T getOrNull();
    public abstract E getErrorOrNull();
    public abstract <U> Result<U, E> map(Function<? super T, ? extends U> mapper);
    public abstract <F> Result<T, F> mapError(Function<? super E, ? extends F> mapper);

    public static <T, E> Result<T, E> success(T value) {
        return new Success<>(value);
    }
    public static <T, E> Result<T, E> failure(E error) {
        return new Failure<>(error);
    }

    public static final class Success<T, E> extends Result<T, E> {
        private final T value;
        public Success(T value) { this.value = value; }
        public boolean isSuccess() { return true; }
        public boolean isFailure() { return false; }
        public T getOrNull() { return value; }
        public E getErrorOrNull() { return null; }
        public <U> Result<U, E> map(Function<? super T, ? extends U> mapper) {
            return success(mapper.apply(value));
        }
        public <F> Result<T, F> mapError(Function<? super E, ? extends F> mapper) {
            return success(value);
        }
    }

    public static final class Failure<T, E> extends Result<T, E> {
        private final E error;
        public Failure(E error) { this.error = error; }
        public boolean isSuccess() { return false; }
        public boolean isFailure() { return true; }
        public T getOrNull() { return null; }
        public E getErrorOrNull() { return error; }
        public <U> Result<U, E> map(Function<? super T, ? extends U> mapper) {
            return failure(error);
        }
        public <F> Result<T, F> mapError(Function<? super E, ? extends F> mapper) {
            return failure(mapper.apply(error));
        }
    }
}

