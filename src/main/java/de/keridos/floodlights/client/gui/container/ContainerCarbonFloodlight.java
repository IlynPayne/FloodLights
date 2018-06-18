package de.keridos.floodlights.client.gui.container;

import de.keridos.floodlights.tileentity.TileEntityCarbonFloodlight;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;

import static de.keridos.floodlights.util.GeneralUtil.getBurnTime;

/**
 * Created by Keridos on 09/10/2014.
 * This Class describes the Container for the Carbon Floodlight.
 */
public class ContainerCarbonFloodlight extends Container {

    public ContainerCarbonFloodlight(InventoryPlayer invPlayer, TileEntityCarbonFloodlight entity) {
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 9; x++) {
                this.addSlotToContainer(new Slot(invPlayer, 9 + x + y * 9, 8 + x * 18, 58 + y * 18));
            }
        }
        for (int x = 0; x < 9; x++) {
            this.addSlotToContainer(new Slot(invPlayer, x, 8 + x * 18, 116));
        }

        IItemHandler inventory = entity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        addSlotToContainer(new SlotItemHandler(inventory, 0, 26, 22) {
            @Override
            public boolean isItemValid(@Nonnull ItemStack stack) {
                return getBurnTime(stack) > 0;
            }

            @Override
            public void onSlotChanged() {
                super.onSlotChanged();
                entity.markDirty();
            }
        });
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int i) {
        Slot slot = getSlot(i);
        if (slot.getHasStack()) {
            ItemStack itemstack = slot.getStack();
            ItemStack result = itemstack.copy();

            if (i >= 36) {
                if (!mergeItemStack(itemstack, 0, 36, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!mergeItemStack(itemstack, 36, 36 + 1, false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack.getCount() == 0) {
                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.onSlotChanged();
            }
            slot.onTake(player, itemstack);
            return result;
        }

        return ItemStack.EMPTY;
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return true;
    }
}
