package com.keerdm.cw_tweaks.handler;

import com.keerdm.cw_tweaks.data.LootTier;
import com.keerdm.cw_tweaks.setup.CwTweaksSetup;
import com.keerdm.cw_tweaks.util.ChestLockUtil;
import mcjty.lostcities.api.ILostCities;
import mcjty.lostcities.api.ILostCityInformation;
import mcjty.lostcities.api.ILostChunkInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ChestLockHandler {

    private static final Queue<PendingChest> PENDING_CHESTS = new ConcurrentLinkedQueue<>();
    private static boolean processingScheduled = false;

    private static class PendingChest {
        final ServerLevel level;
        final BlockPos pos;
        final long timestamp;
        final RandomSource randomSource;

        PendingChest(ServerLevel level, BlockPos pos, RandomSource randomSource) {
            this.level = level;
            this.pos = pos;
            this.timestamp = System.currentTimeMillis();
            this.randomSource = randomSource;
        }
    }

    public static void onBlockPlaced(LevelAccessor levelAccessor, ServerLevel level, BlockPos blockPos, BlockState state, ChunkAccess chunkAccess, RandomSource randomSource) {
        if (!(state.getBlock() instanceof ChestBlock)) return;

        try {
            BlockEntity blockEntity = levelAccessor.getBlockEntity(blockPos);
            if (!(blockEntity instanceof ChestBlockEntity) && !blockEntity.getClass().getPackageName().contains("lootr:")) {
                return;
            }

            if (!isInCityBuilding(level, blockPos)) {
                return;
            }

            var nbt = blockEntity.saveWithoutMetadata();
            if (!nbt.contains("LootTable")) {
                PENDING_CHESTS.add(new PendingChest(level, blockPos.immutable(), RandomSource.create(randomSource.nextLong())));
                scheduleDelayedProcessing(level);
                return;
            }

            if (blockEntity instanceof ChestBlockEntity chest) {
                processChestNow(levelAccessor, level, blockPos, chunkAccess, randomSource, chest);
            }

        } catch (Exception e) {
            System.err.println("[CW Tweaks] Error processing chest at " + blockPos + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void scheduleDelayedProcessing(ServerLevel level) {
        if (processingScheduled) return;

        processingScheduled = true;
        level.getServer().execute(() -> {
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    level.getServer().execute(() -> {
                        processPendingChests();
                        processingScheduled = false;
                    });
                }
            }, 100);
        });
    }

    private static void processPendingChests() {
        int processed = 0;
        int matched = 0;

        while (!PENDING_CHESTS.isEmpty()) {
            PendingChest pending = PENDING_CHESTS.poll();
            processed++;

            try {
                BlockEntity blockEntity = pending.level.getBlockEntity(pending.pos);
                if (!(blockEntity instanceof ChestBlockEntity chest)) {
                    continue;
                }

                if (!isInCityBuilding(pending.level, pending.pos)) {
                    continue;
                }

                var nbt = chest.saveWithoutMetadata();
                if (!nbt.contains("LootTable")) {
                    continue;
                }

                boolean wasMatched = processChestNow(pending.level, pending.level, pending.pos, null, pending.randomSource, chest);
                if (wasMatched) matched++;

            } catch (Exception e) {
                System.err.println("[CW Tweaks] Error processing pending chest at " + pending.pos + ": " + e.getMessage());
            }
        }
    }

    private static boolean processChestNow(LevelAccessor levelAccessor, ServerLevel level, BlockPos blockPos, ChunkAccess chunkAccess, RandomSource randomSource, ChestBlockEntity chest) {
        var nbt = chest.saveWithoutMetadata();

        int weight = randomSource.nextInt(101); // 0-100
        LootTier tier = LootTier.fromWeight(weight);

        String newLootTable = tier.getLootTableId();
        nbt.putString("LootTable", newLootTable);
        nbt.putLong("LootTableSeed", randomSource.nextLong());
        chest.load(nbt);

        if (tier.shouldHaveLock(randomSource)) {
            ChestLockUtil.addLockToChest(levelAccessor, level, blockPos, tier.getLockType(), chunkAccess, randomSource);
        }

        return true;
    }

    public static boolean isInCityBuilding(Level world, BlockPos pos) {
        try {
            ILostCities lostCities = CwTweaksSetup.getLostCitiesAPI();
            if (lostCities == null) {
                return false;
            }

            ILostCityInformation cityInfo = lostCities.getLostInfo(world);
            if (cityInfo == null) {
                return false;
            }

            int chunkX = pos.getX() >> 4;
            int chunkZ = pos.getZ() >> 4;
            ILostChunkInfo chunkInfo = cityInfo.getChunkInfo(chunkX, chunkZ);

            if (!chunkInfo.isCity()) {
                return false;
            }

            int cityLevel = chunkInfo.getCityLevel();
            int cityGroundHeight = cityInfo.getRealHeight(cityLevel);

            String buildingType = chunkInfo.getBuildingType();

            boolean inBounds;
            if (buildingType != null) {
                int numCellars = chunkInfo.getNumCellars();
                int numFloors = chunkInfo.getNumFloors();

                int cityBottom = cityGroundHeight - (numCellars * 6);
                int cityTop = cityGroundHeight + (numFloors * 6);

                inBounds = pos.getY() >= cityBottom && pos.getY() <= cityTop;
            } else {
                inBounds = pos.getY() >= cityGroundHeight;
            }

            return inBounds;

        } catch (Exception e) {
            System.err.println("[CW Tweaks] Error checking if position is in city building: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}