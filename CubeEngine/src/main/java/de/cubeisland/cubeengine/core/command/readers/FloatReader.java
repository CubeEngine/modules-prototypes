package de.cubeisland.cubeengine.core.command.readers;

import de.cubeisland.cubeengine.core.command.ArgumentReader;
import de.cubeisland.cubeengine.core.command.InvalidArgumentException;
import de.cubeisland.cubeengine.core.util.Pair;

public class FloatReader extends ArgumentReader<Float>
{
    public FloatReader()
    {
        super(Float.class);
    }

    @Override
    public Pair<Integer, Float> read(String... args) throws InvalidArgumentException
    {
        String num = args[0].replace(',', '.');
        int lastDot = num.lastIndexOf('.');
        num = num.substring(0, lastDot).replace(".", "") + num.substring(lastDot);
        try
        {
            Float value = Float.parseFloat(num);
            return new Pair<Integer, Float>(0, value);
        }
        catch (NumberFormatException e)
        {
            return new Pair<Integer, Float>(0, null);
        }
    }
}