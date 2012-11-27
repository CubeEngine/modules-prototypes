package de.cubeisland.cubeengine.core.bukkit;

import net.minecraft.server.NBTTagCompound;
import net.minecraft.server.NBTTagList;
import net.minecraft.server.NBTTagString;
import org.apache.commons.lang.Validate;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class BookItem
{
    private static final String PAGES_FIELD = "pages";
    private static final String AUTHOR_FIELD = "author";
    private static final String TITLE_FIELD = "title";

    private final net.minecraft.server.ItemStack item;
    private final CraftItemStack stack;

    /**
     * Initializes this book with the given ItemStack
     *
     * @param item the ItemStack to use.
     */
    public BookItem(ItemStack item)
    {
        Validate.notNull(item, "The item must not be null!");

        if (item instanceof CraftItemStack)
        {
            this.stack = (CraftItemStack)item;
        }
        else
        {
            this.stack = new CraftItemStack(item);
        }
        this.item = this.stack.getHandle();
    }

    /**
     * Returns all pages of this book
     *
     * @return an array of pages
     */
    public String[] getPages()
    {
        NBTTagCompound tagCompound = this.item.getTag();
        if (tagCompound == null)
        {
            return null;
        }

        NBTTagList pages = tagCompound.getList(PAGES_FIELD);
        String[] pageStrings = new String[pages.size()];

        for (int i = 0; i < pages.size(); ++i)
        {
            pageStrings[i] = pages.get(i).toString();
        }
        return pageStrings;
    }

    /**
     * Returns the author of this book
     *
     * @return the name of the author or null if none was set
     */
    public String getAuthor()
    {
        NBTTagCompound tagCompound = this.item.getTag();
        if (tagCompound == null)
        {
            return null;
        }

        return tagCompound.getString(PAGES_FIELD);
    }

    /**
     * Returns the title of this book
     *
     * @return the title or null if none was set
     */
    public String getTitle()
    {
        NBTTagCompound tagCompound = this.item.getTag();
        if (tagCompound == null)
        {
            return null;
        }

        return tagCompound.getString(TITLE_FIELD);
    }

    /**
     * Replaces all pages with the given ones
     *
     * @param newPages the enw pages
     */
    public void setPages(String[] newPages)
    {
        NBTTagCompound tagCompound = this.item.getTag();
        if (tagCompound != null)
        {
            tagCompound.remove(PAGES_FIELD);
        }
        this.addPages(newPages);
    }

    /**
     * Adds the given array of pages to the book
     *
     * @param newPages the pages
     */
    public void addPages(String[] newPages)
    {
        NBTTagCompound tagCompound = this.item.getTag();
        if (tagCompound == null)
        {
            tagCompound = new NBTTagCompound();
            this.item.setTag(tagCompound);
        }

        NBTTagList pages = tagCompound.getList(PAGES_FIELD);
        if (pages == null)
        {
            pages = new NBTTagList(PAGES_FIELD);
        }

        if (newPages.length == 0 && pages.size() == 0)
        {
            pages.add(new NBTTagString("1", ""));
        }
        else
        {
            for (String page : newPages)
            {
                pages.add(new NBTTagString(String.valueOf(pages.size()), page));
            }
        }

        tagCompound.set(PAGES_FIELD, pages);
    }

    /**
     * Sets the author of this book
     *
     * @param author the author to set
     */
    public void setAuthor(Player author)
    {
        Validate.notNull(author, "The author must not be null!");

        this.setAuthor(author.getName());
    }

    /**
     * Sets the author of this book
     *
     * @param author the name of the author to set
     */
    public void setAuthor(String author)
    {
        Validate.notNull(author, "The author must not be null!");

        NBTTagCompound tagCompound = this.item.getTag();
        if (tagCompound == null)
        {
            tagCompound = new NBTTagCompound();
            this.item.setTag(tagCompound);
        }

        if (author != null && !author.isEmpty())
        {
            tagCompound.setString(AUTHOR_FIELD, author);
        }
    }

    /**
     * Sets the title of this book
     *
     * @param title the title to set
     */
    public void setTitle(String title)
    {
        NBTTagCompound tagCompound = this.item.getTag();

        if (tagCompound == null)
        {
            tagCompound = new NBTTagCompound();
            this.item.setTag(tagCompound);
        }

        if (title != null && !title.isEmpty())
        {
            tagCompound.setString(TITLE_FIELD, title);
        }
    }
    
    /**
     * returns a boolean of a given tag
     * @param tag the tag
     * @return the boolean of the given tag
     */
    public boolean getBooleanTag(String tag)
    {
        NBTTagCompound tagCompound = this.item.getTag();
        if (tagCompound == null)
        {
            return false;
        }
        return tagCompound.getBoolean(tag);
    }
    
    /**
     * returns a string of a given tag
     * @param tag the tag
     * @return the string of the given tag
     */
    public String getStringTag(String tag)
    {
        NBTTagCompound tagCompound = this.item.getTag();
        if (tagCompound == null)
        {
            return null;
        }
        return tagCompound.getString(tag);
    }
    
    
    /**
     * sets a string to a given tag
     * @param tag the tag
     * @param value the string value of the tag
     */
    public void setTag(String tag, String value)
    {
        Validate.notNull(tag, "The tag must not be null");
        Validate.notNull(value, "The value must not be null");
        
        NBTTagCompound tagCompound = this.item.getTag();
        
        if( tagCompound == null )
        {
            tagCompound = new NBTTagCompound();
            this.item.setTag(tagCompound);
        }
        
        if( !tag.isEmpty() && !value.isEmpty() )
        {
            tagCompound.setString(tag, value);
        }
    }
    
    /**
     * sets a boolean value to a given tag
     * @param tag the tag 
     * @param value the boolean value of the tag
     */
    public void setTag(String tag, boolean value)
    {
        Validate.notNull(tag, "The tag must not be null");
        Validate.notNull(value, "The value must not be null");
        
        NBTTagCompound tagCompound = this.item.getTag();
        
        if( tagCompound == null )
        {
            tagCompound = new NBTTagCompound();
            this.item.setTag(tagCompound);
        }
        
        if( !tag.isEmpty() )
        {
            tagCompound.setBoolean(tag, value);
        }
    }

    /**
     * Returns the ItemStack of this Book
     *
     * @return an ItemStack
     */
    public ItemStack getItemStack()
    {
        return this.stack;
    }
}
