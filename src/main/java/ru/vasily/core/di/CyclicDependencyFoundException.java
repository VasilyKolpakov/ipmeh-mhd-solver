package ru.vasily.core.di;

public class CyclicDependencyFoundException extends RuntimeException
{
    public CyclicDependencyFoundException()
    {
    }

    public CyclicDependencyFoundException(String message)
    {
        super(message);
    }

    public CyclicDependencyFoundException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public CyclicDependencyFoundException(Throwable cause)
    {
        super(cause);
    }

    public CyclicDependencyFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
    {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
