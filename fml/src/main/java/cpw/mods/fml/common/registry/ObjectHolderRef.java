package cpw.mods.fml.common.registry;

import java.lang.reflect.Field;
import org.apache.logging.log4j.Level;
import com.google.common.base.Throwables;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.relauncher.FinalFieldHelper;
import cpw.mods.fml.common.registry.GameRegistry.ObjectHolder;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;


/**
 * Internal class used in tracking {@link ObjectHolder} references
 *
 * @author cpw
 *
 */
class ObjectHolderRef {
    private Field field;
    private String injectedObject;
    private boolean isBlock;
    private boolean isItem;


    ObjectHolderRef(Field field, String injectedObject, boolean extractFromExistingValues)
    {
        this.field = field;
        this.isBlock = Block.class.isAssignableFrom(field.getType());
        this.isItem = Item.class.isAssignableFrom(field.getType());
        if (extractFromExistingValues)
        {
            try
            {
                Object existing = field.get(null);
                // nothing is ever allowed to replace AIR
                if (existing == null || existing == GameData.getBlockRegistry().getDefaultValue())
                {
                    this.injectedObject = null;
                    this.field = null;
                    this.isBlock = false;
                    this.isItem = false;
                    return;
                }
                else
                {
                    this.injectedObject = isBlock ? GameData.getBlockRegistry().getNameForObject(existing) :
                        isItem ? GameData.getItemRegistry().getNameForObject(existing) : null;
                }
            } catch (Exception e)
            {
                throw Throwables.propagate(e);
            }
        }
        else
        {
            this.injectedObject = injectedObject;
        }

        if (this.injectedObject == null || !isValid())
        {
            throw new IllegalStateException(String.format("The ObjectHolder annotation cannot apply to a field that is not an Item or Block (found : %s at %s.%s)", field.getType().getName(), field.getClass().getName(), field.getName()));
        }
    }

    public boolean isValid()
    {
        return isBlock || isItem;
    }
    public void apply()
    {
        Object thing;
        if (isBlock)
        {
            thing = GameData.getBlockRegistry().getObject(injectedObject);
            if (thing == Blocks.air)
            {
                thing = null;
            }
        }
        else if (isItem)
        {
            thing = GameData.getItemRegistry().getObject(injectedObject);
        }
        else
        {
            thing = null;
        }

        if (thing == null)
        {
            FMLLog.getLogger().log(Level.DEBUG, "Unable to lookup {} for {}. This means the object wasn't registered. It's likely just mod options.", injectedObject, field);
            return;
        }
        try
        {
            FinalFieldHelper.setField(field, null, thing);
        }
        catch (Exception e)
        {
            FMLLog.log(Level.WARN, e, "Unable to set %s with value %s (%s)", this.field, thing, this.injectedObject);
        }
    }
}
