package eu.pb4.polymer.impl.client.compat;

import eu.pb4.polymer.api.client.PolymerClientUtils;
import eu.pb4.polymer.api.item.PolymerItemUtils;
import eu.pb4.polymer.impl.PolymerImpl;
import eu.pb4.polymer.impl.client.InternalClientItemGroup;
import eu.pb4.polymer.impl.client.interfaces.ClientItemGroupExtension;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.entry.EntryRegistry;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.comparison.EntryComparator;
import me.shedaniel.rei.api.common.entry.comparison.ItemComparatorRegistry;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtElement;

import java.util.Collection;
import java.util.function.Predicate;

public class ReiCompatibility implements REIClientPlugin {
    private static final Predicate<? extends EntryStack<?>> SHOULD_REMOVE = (x) -> x.getValue() instanceof ItemStack stack && (PolymerItemUtils.isPolymerServerItem(stack) || PolymerItemUtils.getPolymerIdentifier(stack) != null);

    private static final EntryComparator<ItemStack> ITEM_STACK_ENTRY_COMPARATOR = (c, i) -> {
        var nbt = i.getNbt();

        if (nbt != null && nbt.contains(PolymerItemUtils.POLYMER_ITEM_ID, NbtElement.STRING_TYPE)) {
            return nbt.getString(PolymerItemUtils.POLYMER_ITEM_ID).hashCode();
        }

        return 0;
    };

    @Override
    public void registerItemComparators(ItemComparatorRegistry registry) {
        if (PolymerImpl.ENABLE_REI && PolymerImpl.IS_CLIENT) {
            try {
                registry.registerGlobal(ITEM_STACK_ENTRY_COMPARATOR);
            } catch (Throwable e) {
                // no one cares
            }
        }
    }

    public static void registerEvents() {
        if (PolymerImpl.ENABLE_REI && PolymerImpl.IS_CLIENT) {
            PolymerClientUtils.ON_CLEAR.register(() -> update(EntryRegistry.getInstance()));
            PolymerClientUtils.ON_SEARCH_REBUILD.register(() -> update(EntryRegistry.getInstance()));
        }
    }

    private static void update(EntryRegistry registry) {
        try {
            registry.removeEntryIf(SHOULD_REMOVE);
            for (var group : ItemGroup.GROUPS) {
                if (group == ItemGroup.SEARCH) {
                    continue;
                }

                Collection<ItemStack> stacks;

                if (group instanceof InternalClientItemGroup clientItemGroup) {
                    stacks = clientItemGroup.getStacks();
                } else {
                    stacks = ((ClientItemGroupExtension) group).polymer_getStacks();
                }

                if (stacks != null) {
                    for (var stack : stacks) {
                        registry.addEntry(EntryStack.of(VanillaEntryTypes.ITEM, stack));
                    }
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
