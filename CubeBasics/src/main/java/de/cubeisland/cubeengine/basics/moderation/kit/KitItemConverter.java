package de.cubeisland.cubeengine.basics.moderation.kit;

import de.cubeisland.cubeengine.core.util.convert.ConversionException;
import de.cubeisland.cubeengine.core.util.convert.Converter;
import de.cubeisland.cubeengine.core.util.matcher.MaterialMatcher;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.Material;

public class KitItemConverter implements Converter<KitItem>
{
    private static final Pattern pat = Pattern.compile("(?:([0-9]+)\\*)?([a-zA-Z0-9]+)(?::([0-9]+))?(?: ([a-zA-Z0-9]+))?");

    @Override
    public Object toObject(KitItem object) throws ConversionException
    {
        return object.amount + "*" + object.mat.getId() + ":" + object.dura + (object.customName == null ? "" : " " + object.customName);
    }

    @Override
    public KitItem fromObject(Object object) throws ConversionException
    {
        //suported formats: [amount*]id[:data][ customname] 
        String itemString = object.toString();
        if (itemString.matches(pat.pattern()))
        {
            Matcher matcher = pat.matcher(itemString);
            matcher.find();
            String materialString = matcher.group(2);
            String duraString = matcher.group(3);
            String amountString = matcher.group(1);
            String name = matcher.group(4);
            int amount;
            short dura;
            try
            {
                Material mat = MaterialMatcher.get().matchMaterial(materialString);
                if (amountString == null)
                {
                    amount = mat.getMaxStackSize();
                }
                else
                {
                    amount = Integer.parseInt(amountString);
                }
                if (duraString == null)
                {
                    dura = 0;
                }
                else
                {
                    dura = Short.parseShort(duraString);
                }
                return new KitItem(mat, dura, amount, name);
            }
            catch (Exception ex)
            {
                throw new ConversionException("Could not parse kitItem!", ex);
            }
        }
        throw new ConversionException("Could not parse kitItem!");
    }
}