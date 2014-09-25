/* 
 * Copyright (c) CovertJaguar, 2014 http://railcraft.info
 * 
 * This code is the property of CovertJaguar
 * and may only be used with explicit written
 * permission unless otherwise specified on the
 * license page at http://railcraft.info/wiki/info:license.
 */
package mods.railcraft.client.util.effects;

import cpw.mods.fml.client.FMLClientHandler;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import mods.railcraft.client.core.AuraKeyHandler;
import mods.railcraft.client.particles.EntityChimneyFX;
import mods.railcraft.client.particles.EntityChunkLoaderFX;
import mods.railcraft.client.particles.EntitySteamFX;
import mods.railcraft.client.particles.EntityTuningFX;
import mods.railcraft.api.signals.SignalTools;
import mods.railcraft.client.particles.EntityFireSparkFX;
import mods.railcraft.client.particles.EntityHeatTrailFX;
import mods.railcraft.common.items.ItemGoggles;
import mods.railcraft.common.items.ItemGoggles.Aura;
import mods.railcraft.common.util.effects.CommonEffectProxy;
import mods.railcraft.common.util.effects.EffectManager;
import mods.railcraft.common.util.effects.EffectManager.IEffectSource;
import mods.railcraft.common.util.misc.Game;
import mods.railcraft.common.util.misc.MiscTools;
import mods.railcraft.common.util.network.PacketEffect.Effect;

/**
 *
 * @author CovertJaguar <http://www.railcraft.info>
 */
public class ClientEffectProxy extends CommonEffectProxy {

    public static final short TELEPORT_PARTICLES = 64;
    public static final short TRACKING_DISTANCE = 32 * 32;

    public ClientEffectProxy() {
        SignalTools.effectManager = this;
    }

    private void doTeleport(DataInputStream data) throws IOException {

        double startX = data.readDouble();
        double startY = data.readDouble();
        double startZ = data.readDouble();
        double destX = data.readDouble();
        double destY = data.readDouble();
        double destZ = data.readDouble();

//        for(int i = 0; i < TELEPORT_PARTICLES / 4; i++) {
//            float vX = (rand.nextFloat() - 0.5F) * 0.2F;
//            float vY = (rand.nextFloat() - 0.5F) * 0.2F;
//            float vZ = (rand.nextFloat() - 0.5F) * 0.2F;
//            Game.getWorld().spawnParticle("portal", startX, startY, startZ, vX, vY, vZ);
//        }

        for (int i = 0; i < TELEPORT_PARTICLES; i++) {
            double travel = (double) i / ((double) TELEPORT_PARTICLES - 1.0D);
            float vX = (rand.nextFloat() - 0.5F) * 0.2F;
            float vY = (rand.nextFloat() - 0.5F) * 0.2F;
            float vZ = (rand.nextFloat() - 0.5F) * 0.2F;
            double pX = startX + (destX - startX) * travel + (rand.nextDouble() - 0.5D) * 2.0D;
            double pY = startY + (destY - startY) * travel + (rand.nextDouble() - 0.5D) * 2.0D;
            double pZ = startZ + (destZ - startZ) * travel + (rand.nextDouble() - 0.5D) * 2.0D;
            Game.getWorld().spawnParticle("portal", pX, pY, pZ, vX, vY, vZ);
        }
    }

    @Override
    public boolean isAnchorAuraActive() {
        if (ItemGoggles.areEnabled()) {
            ItemStack goggles = ItemGoggles.getGoggles(Minecraft.getMinecraft().thePlayer);
            return ItemGoggles.getCurrentAura(goggles) == Aura.ANCHOR;
        }
        return AuraKeyHandler.isAnchorAuraEnabled();
    }

    @Override
    public boolean isTuningAuraActive() {
        if (ItemGoggles.areEnabled()) {
            ItemStack goggles = ItemGoggles.getGoggles(Minecraft.getMinecraft().thePlayer);
            return ItemGoggles.getCurrentAura(goggles) == Aura.TUNING;
        }
        return AuraKeyHandler.isTuningAuraEnabled();
    }

    @Override
    public boolean isTrackingAuraActive() {
        if (ItemGoggles.areEnabled()) {
            ItemStack goggles = ItemGoggles.getGoggles(Minecraft.getMinecraft().thePlayer);
            return ItemGoggles.getCurrentAura(goggles) == Aura.TRACKING;
        }
        return false;
    }

    @Override
    public void tuningEffect(TileEntity start, TileEntity dest) {
        if (!shouldSpawnParticle(false))
            return;
        if (rand.nextInt(2) == 0) {
            double px = start.xCoord + 0.5 + rand.nextGaussian() * 0.1;
            double py = start.yCoord + 0.5 + rand.nextGaussian() * 0.1;
            double pz = start.zCoord + 0.5 + rand.nextGaussian() * 0.1;
            EntityFX particle = new EntityTuningFX(start.getWorldObj(), px, py, pz, EffectManager.getEffectSource(dest));
            spawnParticle(particle);
        }
    }

    @Override
    public void trailEffect(int startX, int startY, int startZ, TileEntity dest, long colorSeed) {
        if (!shouldSpawnParticle(false))
            return;
        if (Minecraft.getMinecraft().thePlayer.getDistanceSq(startX, startY, startZ) > TRACKING_DISTANCE)
            return;
        if (rand.nextInt(3) == 0) {
            double px = startX + 0.5 + rand.nextGaussian() * 0.1;
            double py = startY + 0.5 + rand.nextGaussian() * 0.1;
            double pz = startZ + 0.5 + rand.nextGaussian() * 0.1;
            EntityFX particle = new EntityHeatTrailFX(dest.getWorldObj(), px, py, pz, colorSeed, EffectManager.getEffectSource(dest));
            spawnParticle(particle);
        }
    }

    @Override
    public void fireSparkEffect(World world, double startX, double startY, double startZ, double endX, double endY, double endZ) {
        if (!shouldSpawnParticle(false))
            return;

        EntityFX particle = new EntityFireSparkFX(world, startX, startY, startZ, endX, endY, endZ);
        spawnParticle(particle);
    }

    private void doFireSpark(DataInputStream data) throws IOException {
        double startX = data.readDouble();
        double startY = data.readDouble();
        double startZ = data.readDouble();
        double destX = data.readDouble();
        double destY = data.readDouble();
        double destZ = data.readDouble();
        fireSparkEffect(Minecraft.getMinecraft().theWorld, startX, startY, startZ, destX, destY, destZ);
    }

    @Override
    public void handleEffectPacket(DataInputStream data) throws IOException {

        byte effectId = data.readByte();
        if (effectId < 0)
            return;

        Effect effect = Effect.values()[effectId];
        switch (effect) {
            case TELEPORT:
                doTeleport(data);
                break;
            case FIRESPARK:
                doFireSpark(data);
                break;
        }
    }

    @Override
    public void chunkLoaderEffect(World world, Object source, Set<ChunkCoordIntPair> chunks) {
        if (!isAnchorAuraActive())
            return;
        IEffectSource es = EffectManager.getEffectSource(source);
        if (FMLClientHandler.instance().getClient().thePlayer.getDistanceSq(es.getX(), es.getY(), es.getZ()) > 25600)
            return;

        for (ChunkCoordIntPair chunk : chunks) {
            int xCorner = chunk.chunkXPos * 16;
            int zCorner = chunk.chunkZPos * 16;
            double yCorner = es.getY() - 8;

//            System.out.println(xCorner + ", " + zCorner);

            if (rand.nextInt(3) == 0) {
                if (!shouldSpawnParticle(false))
                    continue;
                double xParticle = xCorner + rand.nextFloat() * 16;
                double yParticle = yCorner + rand.nextFloat() * 16;
                double zParticle = zCorner + rand.nextFloat() * 16;

                EntityFX particle = new EntityChunkLoaderFX(world, xParticle, yParticle, zParticle, es);
                spawnParticle(particle);
            }
        }
    }

    @Override
    public void steamEffect(World world, Object source, double yOffset) {
        if (!shouldSpawnParticle(true))
            return;
        IEffectSource es = EffectManager.getEffectSource(source);
        double vx = rand.nextGaussian() * 0.1;
        double vy = rand.nextDouble() * 0.01;
        double vz = rand.nextGaussian() * 0.1;
        spawnParticle(new EntitySteamFX(world, es.getX(), es.getY() + yOffset, es.getZ(), vx, vy, vz));
    }

    @Override
    public void steamJetEffect(World world, Object source, double vecX, double vecY, double vecZ) {
        if (!shouldSpawnParticle(true))
            return;
        IEffectSource es = EffectManager.getEffectSource(source);
        double vx = vecX + rand.nextGaussian() * 0.02;
        double vy = vecY + rand.nextGaussian() * 0.02;
        double vz = vecZ + rand.nextGaussian() * 0.02;
        EntitySteamFX fx = new EntitySteamFX(world, es.getX(), es.getY(), es.getZ(), vx, vy, vz, 1.5F);
        fx.gravity = 0;
        spawnParticle(fx);
    }

    @Override
    public void chimneyEffect(World world, double x, double y, double z) {
        if (!shouldSpawnParticle(false))
            return;
        spawnParticle(new EntityChimneyFX(world, x, y, z));
    }

    private boolean shouldSpawnParticle(boolean canDisable) {
        Minecraft mc = FMLClientHandler.instance().getClient();
        int particleSetting = mc.gameSettings.particleSetting;
        if (!canDisable && particleSetting > 1)
            particleSetting = 1;
        if (particleSetting == 1 && MiscTools.RANDOM.nextInt(3) == 0)
            particleSetting = 2;
        if (particleSetting > 1)
            return false;
        return true;
    }

    @Override
    protected void spawnParticle(EntityFX particle) {
        Minecraft mc = FMLClientHandler.instance().getClient();
        mc.effectRenderer.addEffect(particle);
    }

}