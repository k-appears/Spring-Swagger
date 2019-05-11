package org.poc.exception;

public class CarAlreadyInUseException extends RuntimeException
{

    private static final long serialVersionUID = -8593369814850883033L;


    public CarAlreadyInUseException(final String message)
    {
        super(message);
    }

}
