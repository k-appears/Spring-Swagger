package org.poc.exception;

public class AssignedDriverNotOnlineException extends RuntimeException
{

    private static final long serialVersionUID = -8593369814850883033L;


    public AssignedDriverNotOnlineException(String message)
    {
        super(message);
    }
}
