/* 
 * Copyright (c) CovertJaguar, 2014 http://railcraft.info
 * 
 * This code is the property of CovertJaguar
 * and may only be used with explicit written
 * permission unless otherwise specified on the
 * license page at http://railcraft.info/wiki/info:license.
 */
package mods.railcraft.common.blocks.detector;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;
import mods.railcraft.api.carts.CartTools;
import mods.railcraft.common.blocks.RailcraftTileEntity;
import mods.railcraft.common.plugins.forge.LocalizationPlugin;
import mods.railcraft.common.plugins.forge.PowerPlugin;
import mods.railcraft.common.plugins.forge.WorldPlugin;
import mods.railcraft.common.util.misc.Game;
import mods.railcraft.common.util.misc.SafeNBTWrapper;
import mods.railcraft.common.util.network.IGuiReturnHandler;
import net.minecraft.block.Block;

public class TileDetector extends RailcraftTileEntity implements IGuiReturnHandler {

    public static final float SENSITIVITY = 0.2f;
    public ForgeDirection direction = ForgeDirection.UP;
    public int powerState = 0;
    public Detector detector = Detector.DUMMY;
    private boolean tested;

    public void setDetector(EnumDetector type) {
        this.detector = type.buildHandler();
        detector.setTile(this);
        if (worldObj != null) {
            markBlockForUpdate();
            notifyBlocksOfNeighborChange();
        }
    }

    public Detector getDetector() {
        return detector;
    }

    @Override
    public String getName() {
        return LocalizationPlugin.translate(getDetector().getType().getTag() + ".name");
    }

    public List<EntityMinecart> getCarts() {
        return CartTools.getMinecartsOnAllSides(worldObj, xCoord, yCoord, zCoord, SENSITIVITY);
    }

    public boolean blockActivated(EntityPlayer player) {
        return detector.blockActivated(player);
    }

    public void onNeighborBlockChange(Block block) {
        detector.onNeighborBlockChange(block);
    }

    @Override
    public void writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);

        data.setByte("type", (byte) detector.getType().ordinal());
        detector.writeToNBT(data);
        data.setByte("direction", (byte) direction.ordinal());
        data.setByte("powerState", (byte) powerState);
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);

        SafeNBTWrapper safe = new SafeNBTWrapper(data);

        direction = ForgeDirection.getOrientation(safe.getByte("direction"));
        powerState = data.getByte("powerState");

        if (data.hasKey("type"))
            setDetector(EnumDetector.fromOrdinal(data.getByte("type")));
        detector.readFromNBT(data);
    }

    @Override
    public void writePacketData(DataOutputStream data) throws IOException {
        super.writePacketData(data);
        data.writeByte(detector.getType().ordinal());
        data.writeByte(powerState);
        data.writeByte(direction.ordinal());
        detector.writePacketData(data);
    }

    @Override
    public void readPacketData(DataInputStream data) throws IOException {
        super.readPacketData(data);
        int type = data.readByte();
        if (detector == Detector.DUMMY || detector.getType().ordinal() != type)
            setDetector(EnumDetector.fromOrdinal(type));
        powerState = data.readByte();
        direction = ForgeDirection.getOrientation(data.readByte());
        detector.readPacketData(data);
        markBlockForUpdate();
    }

    @Override
    public boolean canUpdate() {
        return true;
    }

    @Override
    public void updateEntity() {
        super.updateEntity();
        if (Game.isNotHost(getWorld()))
            return;
        if (!tested) {
            tested = true;
            int meta = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
            if (meta != 0) {
                worldObj.removeTileEntity(xCoord, yCoord, yCoord);
                Block block = BlockDetector.getBlock();
                if (block != null)
                    worldObj.setBlock(xCoord, yCoord, yCoord, block, 0, 3);
            }
        }
        if ((detector.updateInterval() == 0 || clock % detector.updateInterval() == 0)) {
            int newPowerState = detector.shouldTest() ? detector.testCarts(getCarts()) : PowerPlugin.NO_POWER;
            if (newPowerState != powerState) {
                powerState = newPowerState;
                sendUpdateToClient();
                worldObj.notifyBlocksOfNeighborChange(xCoord, yCoord, zCoord, BlockDetector.getBlock());
                WorldPlugin.notifyBlocksOfNeighborChangeOnSide(worldObj, xCoord, yCoord, zCoord, BlockDetector.getBlock(), direction);
            }
        }

    }

    @Override
    public short getId() {
        return 76;
    }

    @Override
    public void writeGuiData(DataOutputStream data) throws IOException {
        detector.writeGuiData(data);
    }

    @Override
    public void readGuiData(DataInputStream data, EntityPlayer sender) throws IOException {
        detector.readGuiData(data, sender);
    }

}