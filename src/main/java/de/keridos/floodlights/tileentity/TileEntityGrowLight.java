package de.keridos.floodlights.tileentity;

import de.keridos.floodlights.block.BlockFLColorableMachine;
import de.keridos.floodlights.handler.ConfigHandler;
import de.keridos.floodlights.init.ModBlocks;
import de.keridos.floodlights.reference.Names;
import de.keridos.floodlights.util.GeneralUtil;
import de.keridos.floodlights.util.MathUtil;
import de.keridos.floodlights.util.RandomUtil;
import net.minecraft.block.Block;
import net.minecraft.block.IGrowable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

import static de.keridos.floodlights.block.BlockPhantomLight.UPDATE;
import static de.keridos.floodlights.util.GeneralUtil.safeLocalize;

/**
 * Created by Keridos on 04.05.2015.
 * This Class is the tile entity for the small floodlight.
 */

public class TileEntityGrowLight extends TileEntityFLElectric {
    private long nextGrowTick = 0;

    /*@Override
    public boolean canConnectEnergy(EnumFacing from) {
        return (from.getOpposite().ordinal() == orientation.ordinal());
    }*/


    @SuppressWarnings("ConstantConditions")
    public void growSource(boolean remove) {
        int[] rotatedCoords = MathUtil.rotate(1, 0, 0, this.orientation);


        int x = this.pos.getX() + rotatedCoords[0];
        int y = this.pos.getY() + rotatedCoords[1];
        int z = this.pos.getZ() + rotatedCoords[2];
        BlockPos blockPos = new BlockPos(x, y, z);
        if (remove) {
            if (world.getBlockState(blockPos).getBlock() == ModBlocks.blockPhantomLight) {
                TileEntityPhantomLight light = (TileEntityPhantomLight) world.getTileEntity(blockPos);
                light.removeSource(this.pos);
            }
        } else if (world.getBlockState(blockPos).getBlock().isAir(world.getBlockState(blockPos), world, blockPos)) {
            setLight(blockPos);
            world.setBlockState(blockPos, world.getBlockState(blockPos).withProperty(UPDATE, false));
        } else if (world.getBlockState(blockPos).getBlock() == ModBlocks.blockPhantomLight) {
            TileEntityPhantomLight light = (TileEntityPhantomLight) world.getTileEntity(blockPos);
            light.addSource(this.pos);
        }

    }

    public void update() {
        super.update();
        World world = this.getWorld();
        if (!world.isRemote) {
            int realEnergyUsage = ConfigHandler.energyUsageGrowLight;
            tryDischargeItem(inventory.getStackInSlot(0));
            if (timeout > 0) {
                timeout--;
                return;
            }
            if (active && energy.getEnergyStored() >= realEnergyUsage) {
                if (world.getWorldTime() > nextGrowTick) {
                    BlockPos blockPosTarget = new BlockPos(this.pos.getX() + this.orientation.getFrontOffsetX() * 2, this.pos.getY() + this.orientation.getFrontOffsetY() * 2, this.pos.getZ() + this.orientation.getFrontOffsetZ() * 2);
                    BlockPos blockPosFront = new BlockPos(this.pos.getX() + this.orientation.getFrontOffsetX(), this.pos.getY() + this.orientation.getFrontOffsetY(), this.pos.getZ() + this.orientation.getFrontOffsetZ());
                    Block block = world.getBlockState(blockPosTarget).getBlock();
                    Block blockFront = world.getBlockState(blockPosFront).getBlock();
                    if (GeneralUtil.isBlockValidGrowable(block, world, blockPosTarget) && blockFront.isAir(world.getBlockState(blockPosFront), world, blockPosFront)) {
                        ((IGrowable) block).grow(world, RandomUtil.random, blockPosTarget, world.getBlockState(blockPosTarget));
                    }
                    nextGrowTick = world.getWorldTime() + RandomUtil.getRandomTickTimeoutFromFloatChance(ConfigHandler.chanceGrowLight);
                }
                if (update) {
                    if (_mode == LIGHT_MODE_STRAIGHT) {
                        growSource(true);
                        growSource(false);
                    }
                    world.setBlockState(this.pos, world.getBlockState(this.pos).withProperty(BlockFLColorableMachine.ACTIVE, true), 2);
                    world.markBlocksDirtyVertical(this.pos.getX(), this.pos.getZ(), this.pos.getX(), this.pos.getZ());
                    update = false;
                } else if (!wasActive) {
                    if (_mode == LIGHT_MODE_STRAIGHT) {
                        growSource(false);
                    }
                    world.setBlockState(this.pos, world.getBlockState(this.pos).withProperty(BlockFLColorableMachine.ACTIVE, true), 2);
                    world.markBlocksDirtyVertical(this.pos.getX(), this.pos.getZ(), this.pos.getX(), this.pos.getZ());
                }

                energy.extractEnergy(realEnergyUsage, false);
                wasActive = true;
            } else if ((!active || energy.getEnergyStored() < realEnergyUsage) && wasActive) {
                if (_mode == LIGHT_MODE_STRAIGHT) {
                    growSource(true);
                }
                world.setBlockState(this.pos, world.getBlockState(this.pos).withProperty(BlockFLColorableMachine.ACTIVE, false), 2);
                world.markBlocksDirtyVertical(this.pos.getX(), this.pos.getZ(), this.pos.getX(), this.pos.getZ());
                wasActive = false;
                timeout = ConfigHandler.timeoutFloodlights;
                update = false;
            }
        }
    }

    public void changeMode(EntityPlayer player) {
        World world = this.getWorld();
        if (!world.isRemote) {
            if (_mode == LIGHT_MODE_STRAIGHT) {
                growSource(true);
            }
            _mode = (_mode == LIGHT_MODE_NARROW_CONE ? LIGHT_MODE_STRAIGHT : _mode + 1);
            if (active && energy.getEnergyStored() >= ConfigHandler.energyUsage && _mode == LIGHT_MODE_STRAIGHT) {
                growSource(false);
            }
            String modeString = (_mode == LIGHT_MODE_STRAIGHT ? Names.Localizations.LIGHTING : Names.Localizations.DARK_LIGHT);
            player.sendMessage(new TextComponentString(safeLocalize(Names.Localizations.MODE) + ": " + safeLocalize(modeString)));
        }
    }
}
