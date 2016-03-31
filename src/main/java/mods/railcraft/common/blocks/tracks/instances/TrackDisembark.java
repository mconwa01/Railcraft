/*******************************************************************************
 * Copyright (c) CovertJaguar, 2011-2016
 * http://railcraft.info
 *
 * This code is the property of CovertJaguar
 * and may only be used with explicit written
 * permission unless otherwise specified on the
 * license page at http://railcraft.info/wiki/info:license.
 ******************************************************************************/
package mods.railcraft.common.blocks.tracks.instances;

import mods.railcraft.api.core.items.IToolCrowbar;
import mods.railcraft.api.tracks.ITrackPowered;
import mods.railcraft.common.blocks.tracks.EnumTrack;
import mods.railcraft.common.blocks.tracks.TrackTools;
import mods.railcraft.common.carts.CartUtils;

import net.minecraft.block.BlockRailBase;
import net.minecraft.block.BlockRailBase.EnumRailDirection;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class TrackDisembark extends TrackBaseRailcraft implements ITrackPowered {

    private static final int TIME_TILL_NEXT_MOUNT = 40;
    private boolean powered = false;
    private boolean mirrored = false;

    @Override
    public EnumTrack getTrackType() {
        return EnumTrack.DISEMBARK;
    }

    @Override
    public IIcon getIcon() {
        if (mirrored) {
            if (isPowered())
                return getIcon(2);
            return getIcon(3);
        }
        if (isPowered())
            return getIcon(0);
        return getIcon(1);
    }

    @Override
    public void onMinecartPass(EntityMinecart cart) {
        if (powered && cart.canBeRidden() && cart.riddenByEntity != null) {
            double x = getPos().getX();
            double z = getPos().getZ();
            double offset = 1.5;
            IBlockState state = getWorld().getBlockState(getPos());
            EnumRailDirection dir = TrackTools.getTrackDirectionRaw(state);
            if (dir == EnumRailDirection.NORTH_SOUTH)
                if (mirrored)
                    x += offset;
                else
                    x -= offset;
            else if (mirrored)
                z += offset;
            else
                z -= offset;
            CartUtils.dismount(cart, x + 0.5, getPos().getY() + 1, z + 0.5);
            cart.getEntityData().setInteger("MountPrevention", TIME_TILL_NEXT_MOUNT);
        }
    }

    @Override
    public boolean blockActivated(EntityPlayer player) {
        ItemStack current = player.getCurrentEquippedItem();
        if (current != null && current.getItem() instanceof IToolCrowbar) {
            IToolCrowbar crowbar = (IToolCrowbar) current.getItem();
            if (crowbar.canWhack(player, current, getPos())) {
                mirror();
                crowbar.onWhack(player, current, getPos());
                sendUpdateToClient();
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isPowered() {
        return powered;
    }

    @Override
    public void setPowered(boolean powered) {
        this.powered = powered;
    }

    public boolean isMirrored() {
        return powered;
    }

    public void mirror() {
        mirrored = !mirrored;
    }

    @Override
    public void writeToNBT(NBTTagCompound nbttagcompound) {
        super.writeToNBT(nbttagcompound);
        nbttagcompound.setBoolean("powered", powered);
        nbttagcompound.setBoolean("mirrored", mirrored);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbttagcompound) {
        super.readFromNBT(nbttagcompound);
        powered = nbttagcompound.getBoolean("powered");
        mirrored = nbttagcompound.getBoolean("mirrored");
    }

    @Override
    public void writePacketData(DataOutputStream data) throws IOException {
        super.writePacketData(data);

        data.writeBoolean(powered);
        data.writeBoolean(mirrored);
    }

    @Override
    public void readPacketData(DataInputStream data) throws IOException {
        super.readPacketData(data);
        powered = data.readBoolean();
        mirrored = data.readBoolean();
        markBlockNeedsUpdate();
    }

}