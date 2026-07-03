package com.redfox.ret;

import eu.pb4.trinkets.api.SlotType;
import eu.pb4.trinkets.api.TrinketAttachment;
import eu.pb4.trinkets.api.TrinketSlotAccess;
import eu.pb4.trinkets.api.TrinketsApi;
import net.fabricmc.fabric.api.entity.event.v1.EntityElytraEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

import java.util.ArrayList;
import java.util.List;

/** Server- and client-side methods for Elytra Trinket. */
public final class ServerTools {
    /**
     * Determine whether or not the given item stack contains a usable Elytra.
     *
     * @param stack The item stack.
     * @return Whether or not the given item stack contains a usable Elytra.
     */
    private static boolean isUsableElytra(ItemStack stack) {
        return stack != null
                && !stack.isEmpty()
                && stack.is(Items.ELYTRA)
                && (!stack.isDamageableItem()
                    || stack.getDamageValue() < stack.getMaxDamage() - 1);
    }

    /**
     * Make the given entity fly if the given Elytra is usable.
     *
     * @param entity The entity.
     * @param slot  The Elytra slot access.
     * @param doTick Whether or not the Elytra should be checked on this tick.
     * @returns Whether or not the entity was made to fly.
     */
    private static boolean useElytraTrinket(LivingEntity entity, TrinketSlotAccess slot, boolean doTick) {
        ItemStack stack = slot.get();

        if (!ServerTools.isUsableElytra(stack) || !(entity instanceof Player playerEntity)) {
            return false;
        }

        if (!doTick) {
            return true;
        }

        int nextRoll = entity.getFallFlyingTicks() + 1;
        Level world = entity.level();
        if (!world.isClientSide() && nextRoll % 10 == 0) {
            if ((nextRoll / 10) % 2 == 0) {
                TrinketsApi.hurtAndBreakItemStack(
                        stack,
                        1,
                        playerEntity,
                        slot
                );
            }

            entity.gameEvent(GameEvent.ELYTRA_GLIDE);
        }

        return true;
    }

    /** Enable flight when wearing an Elytra in a cape trinket slot. */
    protected static void registerFlight() {
        EntityElytraEvents.CUSTOM.register((entity, tickElytra) -> {
            // If an equipped Elytra is usable, fly.
            for (TrinketSlotAccess slot : ServerTools.getEquippedElytraTrinkets(entity)) {
                if (ServerTools.useElytraTrinket(entity, slot, tickElytra)) {
                    return true;
                }
            }

            // No usable Elytra is in a cape trinket slot.
            return false;
        });
    }

    /**
     * Get a list of equipped Elytra trinkets.
     *
     * @param entity The entity that has the Elytra equipped.
     * @returns A list of equipped Elytra trinkets.
     */
    public static List<TrinketSlotAccess> getEquippedElytraTrinkets(LivingEntity entity) {
        List<TrinketSlotAccess> out = new ArrayList<>();

        TrinketAttachment attachment =
                TrinketsApi.getAttachment(entity);

        for (TrinketSlotAccess slot :
                attachment.equipped(Items.ELYTRA, false)) {

            SlotType type = slot.slotType();

            // Only allow chest/cape
            if (!type.group().equals("chest")
                    || !type.getId().equals("cape")) {
                continue;
            }

            if (!slot.get().isEmpty()) {
                out.add(slot);
            }
        }

        return out;
    }

    /**
     * Determine whether or not the given entity is wearing an Elytra in a trinket
     * slot.
     *
     * @param entity The entity to check.
     * @returns Whether or not the entity is wearing an Elytra in a trinket slot.
     */
    public static boolean isElytraTrinketEquipped(LivingEntity entity) {
        return !getEquippedElytraTrinkets(entity).isEmpty();
    }
}
