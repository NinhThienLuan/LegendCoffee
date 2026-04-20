package fpt.legendcoffee.common.exception;

public class GHNException extends RuntimeException {
    private final int code;

    public GHNException(String message) {
        super(message);
        this.code = -1;
    }

    public GHNException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
