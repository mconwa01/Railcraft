/*******************************************************************************
 * Copyright (c) CovertJaguar, 2011-2016
 * http://railcraft.info
 *
 * This code is the property of CovertJaguar
 * and may only be used with explicit written
 * permission unless otherwise specified on the
 * license page at http://railcraft.info/wiki/info:license.
 ******************************************************************************/
package mods.railcraft.common.modules;

import mods.railcraft.api.core.IRailcraftModule;
import mods.railcraft.api.core.RailcraftModule;
import mods.railcraft.common.blocks.BlockFactory;
import mods.railcraft.common.core.IRailcraftObjectContainer;

import javax.annotation.Nonnull;
import java.util.*;

public abstract class RailcraftModulePayload implements IRailcraftModule {

    private static final ModuleEventHandler BLANK_EVENT_HANDLER = new ModuleEventHandler();
    private final Set<IRailcraftObjectContainer> objectContainers = new HashSet<IRailcraftObjectContainer>();
    private final List<BlockFactory> blockFactories = new ArrayList<BlockFactory>();
    private final ModuleEventHandler baseEventHandler = new BaseModuleEventHandler();
    private ModuleEventHandler enabledEventHandler = BLANK_EVENT_HANDLER;
    private ModuleEventHandler disabledEventHandler = BLANK_EVENT_HANDLER;

    public final void setEnabledEventHandler(@Nonnull ModuleEventHandler enabledEventHandler) {
        this.enabledEventHandler = enabledEventHandler;
    }

    public final void setDisabledEventHandler(@Nonnull ModuleEventHandler disabledEventHandler) {
        this.disabledEventHandler = disabledEventHandler;
    }

    @Deprecated
    public final void addBlockFactory(@Nonnull BlockFactory factory) {
        if (RailcraftModuleManager.getStage() != RailcraftModuleManager.Stage.CONSTRUCTION)
            throw new RuntimeException("You can only define Block Factories in Construction!");
        blockFactories.add(factory);
    }

    public final void add(IRailcraftObjectContainer... objects) {
        if (RailcraftModuleManager.getStage() != RailcraftModuleManager.Stage.CONSTRUCTION)
            throw new RuntimeException("You can only associate Railcraft Objects with a Module during the Construction phase!");
        objectContainers.addAll(Arrays.asList(objects));
    }

    @Nonnull
    @Override
    public final ModuleEventHandler getModuleEventHandler(boolean enabled) {
        if (enabled)
            return baseEventHandler;
        return disabledEventHandler;
    }

    @Override
    public void checkPrerequisites() throws MissingPrerequisiteException {
    }

    @Override
    public String toString() {
        return "Railcraft Module: " + getClass().getAnnotation(RailcraftModule.class).value();
    }

    private final class BaseModuleEventHandler extends ModuleEventHandler {
        @Override
        public void construction() {
            enabledEventHandler.construction();
        }

        @Override
        public void preInit() {
            for (BlockFactory factory : blockFactories) {
                factory.initBlock();
            }
            for (IRailcraftObjectContainer obj : objectContainers) {
                obj.register();
            }
            enabledEventHandler.preInit();
        }

        @Override
        public void init() {
            for (BlockFactory factory : blockFactories) {
                factory.initRecipes();
            }
            enabledEventHandler.init();
        }

        @Override
        public void postInit() {
            for (BlockFactory factory : blockFactories) {
                factory.finalizeBlocks();
            }
            enabledEventHandler.postInit();
        }
    }

}