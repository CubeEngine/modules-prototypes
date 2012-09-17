package de.cubeisland.cubeengine.core.util;

import gnu.trove.map.TCharObjectMap;
import gnu.trove.map.hash.TCharObjectHashMap;
import java.util.regex.Pattern;

/**
 *
 * @author Phillip Schichtel
 */
public enum ChatFormat
{
    BLACK('0'),
    DARK_BLUE('1'),
    DARK_GREEN('2'),
    DARK_AQUA('3'),
    DARK_RED('4'),
    PURPLE('5'),
    GOLD('6'),
    GREY('7'),
    DARK_GREY('8'),
    INDIGO('9'),
    BRIGHT_GREEN('a'),
    AQUA('b'),
    RED('c'),
    PINK('d'),
    YELLOW('e'),
    WHITE('f'),
    
    MAGIC('k'),
    BOLD('l'),
    STRIKE('m'),
    UNDERLINE('n'),
    ITALIC('o'),
    RESET('r');
    
    private static final char BASE_CHAR = '\u00A7';
    private static final TCharObjectMap<ChatFormat> FORMAT_CHARS_MAP;
    private static final String FORMAT_CHARS_STRING = "0123456789AaBbCcDdEeFfKkLlMmNnOoRr";
    private static final Pattern STRIP_FORMATS = Pattern.compile(BASE_CHAR + "[" + FORMAT_CHARS_STRING + "]");
    
    private final char formatChar;
    
    private ChatFormat(char formatChar)
    {
        this.formatChar = formatChar;
    }
    
    public char getChar()
    {
        return this.formatChar;
    }
    
    public static ChatFormat getByChar(char theChar)
    {
        return FORMAT_CHARS_MAP.get(theChar);
    }
    
    public static String stripFormats(String string)
    {
        if (string == null)
        {
            return null;
        }
        return STRIP_FORMATS.matcher(string).replaceAll("");
    }
    
    public static String parseFormats(String string)
    {
        if (string == null)
        {
            return null;
        }
        return parseFormats('&', string);
    }
    
    public static String parseFormats(char baseChar, String string)
    {
        if (string == null)
        {
            return null;
        }
        char[] chars = string.toCharArray();
        for (int i = 0; i < chars.length - 1; i++)
        {
            if ((chars[i] != baseChar) || (FORMAT_CHARS_STRING.indexOf(chars[(i + 1)]) <= -1))
            {
                continue;
            }
            chars[i] = BASE_CHAR;
            i++;
        }

        return new String(chars);
    }
    
    static
    {
        ChatFormat[] values = values();
        FORMAT_CHARS_MAP = new TCharObjectHashMap<ChatFormat>(values.length);
        for (ChatFormat format : values)
        {
            FORMAT_CHARS_MAP.put(format.getChar(), format);
        }
    }
}