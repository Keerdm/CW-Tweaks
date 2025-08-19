package com.keerdm.cw_tweaks.util;

import com.keerdm.cw_tweaks.data.LockType;
import melonslise.locks.common.capability.ILockableHandler;
import melonslise.locks.common.init.LocksCapabilities;
import melonslise.locks.common.init.LocksItems;
import melonslise.locks.common.item.LockingItem;
import melonslise.locks.common.util.Cuboid6i;
import melonslise.locks.common.util.Lock;
import melonslise.locks.common.util.Lockable;
import melonslise.locks.common.util.Transform;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

public class ChestLockUtil {

    public static boolean addLockToChest(LevelAccessor levelAccessor, ServerLevel level, BlockPos chestPos, LockType lockType, ChunkAccess chunkAccess, RandomSource randomSource) {
        if (lockType == LockType.NONE) return false;

        try {
            ILockableHandler handler = level.getCapability(LocksCapabilities.LOCKABLE_HANDLER).orElse(null);
            if (handler == null) {
                return false;
            }

            ItemStack lockStack = createLockStack(lockType);
            if (lockStack.isEmpty()) {
                return false;
            }

            BlockState chestState = levelAccessor.getBlockState(chestPos);
            Direction chestFacing = chestState.getValue(ChestBlock.FACING);

            Cuboid6i boundingBox = new Cuboid6i(chestPos, chestPos);

            Lock lock = Lock.from(lockStack);

            Transform transform = getChestLockTransform(chestFacing);

            Lockable lockable = new Lockable(boundingBox, lock, transform, lockStack, level);
            boolean added = handler.add(lockable);

            return added;

        } catch (Exception e) {
            System.err.println("Failed to add lock to chest at " + chestPos + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private static Transform getChestLockTransform(Direction chestFacing) {
        return switch (chestFacing) {
            case SOUTH -> Transform.SOUTH_MID;
            case WEST -> Transform.WEST_MID;
            case EAST -> Transform.EAST_MID;
            default -> Transform.NORTH_MID;
        };
    }

    private static ItemStack createLockStack(LockType lockType) {
        try {
            Item lockItem = switch (lockType) {
                case WOOD -> LocksItems.WOOD_LOCK.get();
                case IRON -> LocksItems.IRON_LOCK.get();
                case STEEL -> LocksItems.STEEL_LOCK.get();
                case GOLD -> LocksItems.GOLD_LOCK.get();
                case DIAMOND -> LocksItems.DIAMOND_LOCK.get();
                default -> null;
            };

            if (lockItem == null) return ItemStack.EMPTY;

            ItemStack stack = new ItemStack(lockItem);
            LockingItem.getOrSetId(stack);
            return stack;

        } catch (Exception e) {
            System.err.println("[CW Tweaks] Failed to create lock stack for " + lockType + ": " + e.getMessage());
            return ItemStack.EMPTY;
        }
    }
}