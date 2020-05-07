package fr.andross.banitem.Utils.Item;

import fr.andross.banitem.Utils.BanVersion;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

/**
 * This class offers a way to compare the item meta.
 * All the casts are already checked and valided when caching the datas.
 * @version 2.1
 * @author Andross
 */
@SuppressWarnings("unchecked")
public enum MetaType {
    DISPLAYNAME_EQUALS(o -> (o instanceof String)) {
        @Override
        public boolean matches(@NotNull final ItemStack item, @Nullable final ItemMeta itemMeta, @NotNull final Object o) {
            return itemMeta != null && itemMeta.hasDisplayName() && itemMeta.getDisplayName().equals(o.toString());
        }
    },

    DISPLAYNAME_CONTAINS(o -> (o instanceof String)) {
        @Override
        public boolean matches(@NotNull final ItemStack item, @Nullable final ItemMeta itemMeta, @NotNull final Object o) {
            return itemMeta != null && itemMeta.hasDisplayName() && itemMeta.getDisplayName().contains(o.toString());
        }
    },

    LORE_EQUALS(o -> (o instanceof String) || (o instanceof List) || (o instanceof String[])) {
        @Override
        public boolean matches(@NotNull final ItemStack item, @Nullable final ItemMeta itemMeta, @NotNull final Object o) {
            if (itemMeta == null) return false;
            final List<String> itemLore = itemMeta.hasLore() ? itemMeta.getLore() : null;
            final List<String> lore = (List<String>) o;
            return lore.equals(itemLore);
        }
    },

    LORE_CONTAINS(o -> (o instanceof String) || (o instanceof List) || (o instanceof String[])) {
        @Override
        public boolean matches(@NotNull final ItemStack item, @Nullable final ItemMeta itemMeta, @NotNull final Object o) {
            if (itemMeta == null) return false;
            final List<String> itemLore = itemMeta.hasLore() ? itemMeta.getLore() : null;
            final List<String> lore = (List<String>) o;
            return itemLore != null && lore.stream().anyMatch(lore::contains);
        }
    },

    DURABILITY(o -> (o instanceof Integer)) {
        @Override
        public boolean matches(@NotNull final ItemStack item, @Nullable final ItemMeta itemMeta, @NotNull final Object o) {
            if (BanVersion.v13OrMore && itemMeta == null) return false;
            final int durability = BanVersion.v13OrMore ? ((Damageable)itemMeta).getDamage() : item.getDurability();
            return durability == (int) o;
        }
    },

    ENCHANTMENT_EQUALS(o -> (o instanceof String) || (o instanceof List) || (o instanceof String[])) {
        @Override
        public boolean matches(@NotNull final ItemStack item, @Nullable final ItemMeta itemMeta, @NotNull final Object o) {
            final Map<Enchantment, Integer> itemMap = item.getEnchantments();
            final Map<Enchantment, Integer> map = (Map<Enchantment, Integer>) o;
            return itemMap.equals(map);
        }
    },

    ENCHANTMENT_CONTAINS(o -> (o instanceof String) || (o instanceof List) || (o instanceof String[])) {
        @Override
        public boolean matches(@NotNull final ItemStack item, @Nullable final ItemMeta itemMeta, @NotNull final Object o) {
            if (itemMeta == null) return false;
            final Map<Enchantment, Integer> itemMap = item.getEnchantments();
            final Map<Enchantment, Integer> map = (Map<Enchantment, Integer>) o;

            for (final Map.Entry<Enchantment, Integer> entry : map.entrySet()) {
                if (!itemMap.containsKey(entry.getKey())) return false;
                final int level = itemMap.get(entry.getKey());
                if (level != entry.getValue()) return false;
            }

            return true;
        }
    },

    POTION(o -> (o instanceof String) || (o instanceof List) || (o instanceof String[])) {
        @Override
        public boolean matches(@NotNull final ItemStack item, @Nullable final ItemMeta itemMeta, @NotNull final Object o) {
            final Set<PotionEffectType> types = (Set<PotionEffectType>) o;

            // Checking potion base?
            if (BanVersion.v9OrMore) {
                if (!(itemMeta instanceof PotionMeta)) return false;
                final PotionMeta pm = (PotionMeta) itemMeta;
                final PotionEffectType effectType = pm.getBasePotionData().getType().getEffectType();
                if (types.contains(effectType)) return true;
            } else {
                final Potion p = Potion.fromItemStack(item);
                if (types.contains(p.getType().getEffectType())) return true;
            }

            // Checking custom effects
            if (!(itemMeta instanceof PotionMeta)) return false;
            final PotionMeta pm = (PotionMeta) itemMeta;
            return pm.hasCustomEffects() && pm.getCustomEffects().stream().map(PotionEffect::getType).anyMatch(types::contains);
        }
    };

    private final Predicate<Object> p;

    MetaType(final Predicate<Object> p) {
        this.p = p;
    }

    /**
     * Attempt to validate the object class, to match the meta type
     * @param o the object to validate
     * @throws Exception if the object can not be used for this meta type
     */
    public void validate(@NotNull final Object o) throws Exception {
        if (!p.test(o)) throw new Exception();
    }

    /**
     * Compares the item meta with the object.
     * @param item the item stack (used for durability on mc under 1.13)
     * @param itemMeta the item meta
     * @param o the object to match
     * @return true if the item meta matches, otherwise false
     */
    public abstract boolean matches(@NotNull final ItemStack item, @Nullable final ItemMeta itemMeta, @NotNull final Object o);

}
