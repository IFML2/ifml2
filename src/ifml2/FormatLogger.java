package ifml2;

import org.apache.log4j.Logger;

import java.text.MessageFormat;

public class FormatLogger //fixme remove
{
    private Logger logger;

    public FormatLogger(Class aClass)
    {
        logger = Logger.getLogger(aClass);
    }

    public static FormatLogger getLogger(Class aClass)
    {
        return new FormatLogger(aClass);
    }

    public void debug(String message)
    {
        logger.debug(message);
    }

    public void debug(String message, Object ... args)
    {
        debug(MessageFormat.format(message, args));
    }

    public void warn(String message)
    {
        logger.warn(message);
    }

    public void warn(String message, Object ... args)
    {
        warn(MessageFormat.format(message, args));
    }

    public void error(String message)
    {
        logger.error(message);
    }

    public void error(String message, Object... args)
    {
        error(MessageFormat.format(message, args));
    }

    public void info(String message)
    {
        logger.info(message);
    }

    public void info(String message, Object... args)
    {
        info(MessageFormat.format(message, args));
    }

    public void trace(String message, Object... args)
    {
        trace(MessageFormat.format(message, args));
    }

    public void trace(String message)
    {
        logger.trace(message);
    }
}
