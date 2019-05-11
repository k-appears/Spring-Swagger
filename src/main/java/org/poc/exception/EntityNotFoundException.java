package org.poc.exception;

public class EntityNotFoundException extends RuntimeException
{
    static final long serialVersionUID = -3387516993334229948L;


    public EntityNotFoundException(String message)
    {
        super(message);
    }

}
