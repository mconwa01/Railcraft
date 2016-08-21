/*------------------------------------------------------------------------------
 Copyright (c) CovertJaguar, 2011-2016
 http://railcraft.info

 This code is the property of CovertJaguar
 and may only be used with explicit written
 permission unless otherwise specified on the
 license page at http://railcraft.info/wiki/info:license.
 -----------------------------------------------------------------------------*/

package mods.railcraft.common.blocks.tracks.behaivor;

import mods.railcraft.common.blocks.tracks.TrackTools;
import mods.railcraft.common.plugins.forge.WorldPlugin;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by CovertJaguar on 8/7/2016 for Railcraft.
 *
 * @author CovertJaguar <http://www.railcraft.info>
 */
public class AbandonedTrackTools {

    public static boolean isSupportedDirectly(IBlockAccess world, BlockPos pos) {
        return world.isSideSolid(pos.down(), EnumFacing.UP, false);
    }

    public static boolean isSupported(World world, BlockPos pos) {
        return isSupported(world, pos, false, 0, new HashSet<>());
    }

    private static boolean isSupported(World world, BlockPos pos, boolean checkSelf, int distance, Set<BlockPos> checked) {
        if (checked.contains(pos))
            return false;
        checked.add(pos);
        if (!WorldPlugin.isBlockLoaded(world, pos))
            return true;
        if (checkSelf && !TrackTools.isRailBlockAt(world, pos))
            return false;
        if (world.isSideSolid(pos.down(), EnumFacing.UP, false))
            return true;
        if (distance >= 2)
            return false;
        distance++;
        for (BlockPos connectedTrack : TrackTools.getConnectedTracks(world, pos)) {
            if (isSupported(world, connectedTrack, true, distance, checked))
                return true;
        }
        return false;
    }
}
