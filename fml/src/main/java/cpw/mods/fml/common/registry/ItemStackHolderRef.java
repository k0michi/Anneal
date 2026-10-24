package cpw.mods.fml.common.registry;

import java.lang.reflect.Field;

import net.minecraft.item.ItemStack;

import org.apache.logging.log4j.Level;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.relauncher.FinalFieldHelper;
import cpw.mods.fml.common.registry.GameRegistry.ItemStackHolder;


/**
 * Internal class used in tracking {@link ItemStackHolder} references
 *
 * @author cpw
 *
 */
class ItemStackHolderRef {
    private Field field;
    private String itemName;
    private int meta;
    private String serializednbt;


    ItemStackHolderRef(Field field, String itemName, int meta, String serializednbt)
    {
        this.field = field;
        this.itemName = itemName;
        this.meta = meta;
        this.serializednbt = serializednbt;
    }

    public void apply()
    {
        ItemStack is;
        try
        {
            is = GameRegistry.makeItemStack(itemName, meta, 1, serializednbt);
        } catch (RuntimeException e)
        {
            FMLLog.getLogger().log(Level.ERROR, "Caught exception processing itemstack {},{},{} in annotation at {}.{}", itemName, meta, serializednbt,field.getClass().getName(),field.getName());
            throw e;
        }
        try
        {
            FinalFieldHelper.setField(field, null, is);
        }
        catch (Exception e)
        {
            FMLLog.getLogger().log(Level.WARN, "Unable to set {} with value {},{},{}", this.field, this.itemName, this.meta, this.serializednbt);
        }
    }
}
