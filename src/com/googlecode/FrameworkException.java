package com.googlecode;

public class FrameworkException extends RuntimeException {

    private static final long serialVersionUID = -4505526491557362977L;

    public FrameworkException() {
        super();
    }
    
    public FrameworkException(String msg) {
        super(msg);
    }
    
    public FrameworkException(Throwable cause) {
        super(cause);
    }
    
    public FrameworkException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
