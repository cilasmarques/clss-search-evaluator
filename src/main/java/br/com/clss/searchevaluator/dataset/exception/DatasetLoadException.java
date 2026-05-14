package br.com.clss.searchevaluator.dataset.exception;

public class DatasetLoadException extends RuntimeException {

    public DatasetLoadException(String message) {
        super(message);
    }

    public DatasetLoadException(String message, Throwable cause) {
        super(message, cause);
    }
}
