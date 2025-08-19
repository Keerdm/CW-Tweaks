package com.keerdm.cw_tweaks.mixins;

import com.keerdm.cw_tweaks.handler.ChestLockHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorldGenRegion.class)
public class WorldGenRegionMixin {
    
    @Shadow @Final private ServerLevel level;
    
    @Inject(method = "setBlock", at = @At(value = "RETURN", ordinal = 1))
    public void onBlockPlaced(BlockPos blockPos, BlockState blockState, int flags, int recursionLeft, CallbackInfoReturnable<Boolean> cir) {
        try {
            ChunkAccess chunkAccess = ((WorldGenRegion)(Object)this).getChunk(blockPos);

            RandomSource randomSource = RandomSource.create();

            ChestLockHandler.onBlockPlaced((WorldGenRegion)(Object)this, level, blockPos, blockState, chunkAccess, randomSource);
            
        } catch (Exception e) {
            System.err.println("[CW Tweaks] Error in WorldGenRegionMixin: " + e.getMessage());
        }
    }
}