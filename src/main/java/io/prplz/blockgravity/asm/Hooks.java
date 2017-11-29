package io.prplz.blockgravity.asm;

import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.BlockFence;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

@SuppressWarnings("unused")
public class Hooks {

    private static final int BLOCK_FALLING_TICK_RATE = 2;

    public static void onBlockAdded(Block block, World worldIn, BlockPos pos, IBlockState state) {
        block.onBlockAdded(worldIn, pos, state);
        if (!worldIn.isRemote && !BlockFalling.fallInstantly && shouldAffect(block)) {
            worldIn.scheduleUpdate(pos, block, BLOCK_FALLING_TICK_RATE);
        }
    }

    public static void updateTick(Block block, World worldIn, BlockPos pos, IBlockState state, Random rand) {
        block.updateTick(worldIn, pos, state, rand);
        if (!worldIn.isRemote && !BlockFalling.fallInstantly && shouldAffect(block)) {
            checkFallable(block, worldIn, pos);
        }
    }

    public static void neighborChanged(Block block, IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        block.neighborChanged(state, worldIn, pos, blockIn, fromPos);
        if (!worldIn.isRemote && !BlockFalling.fallInstantly && shouldAffect(block)) {
            worldIn.scheduleUpdate(pos, block, BLOCK_FALLING_TICK_RATE);
        }
    }

    public static void entityFallingBlockOnUpdate(EntityFallingBlock entity) {
        if (!entity.world.isRemote && !entity.isDead) {
            Block block = entity.world.getBlockState(new BlockPos(entity.posX, entity.posY - 1, entity.posZ)).getBlock();
            if (block instanceof BlockFence || block instanceof BlockFenceGate) {
                entity.setDead();
            }
        }
    }

    public static List<AxisAlignedBB> getCollisionBoxes(World world, Entity entityIn, AxisAlignedBB aabb) {
        List<AxisAlignedBB> list = Lists.newArrayList();
        world.getCollisionBoxes_(entityIn, aabb, false, list);
        return list;
    }

    private static boolean shouldAffect(Block block) {
        return block != Blocks.BEDROCK && !(block instanceof BlockFalling) && !(block instanceof BlockLiquid);
    }

    private static void checkFallable(Block block, World worldIn, BlockPos pos) {
        if ((worldIn.isAirBlock(pos.down()) || canFallThrough(worldIn.getBlockState(pos.down()))) && pos.getY() >= 0) {
            if (worldIn.isAreaLoaded(pos.add(-32, -32, -32), pos.add(32, 32, 32))) {
                IBlockState state = worldIn.getBlockState(pos);
                EntityFallingBlock entity = new EntityFallingBlock(worldIn, (double) pos.getX() + 0.5D, (double) pos.getY(), (double) pos.getZ() + 0.5D, state);
                if (block.hasTileEntity(state)) {
                    TileEntity tileEntity = worldIn.getTileEntity(pos);
                    if (tileEntity != null) {
                        entity.tileEntityData = tileEntity.serializeNBT();
                        worldIn.removeTileEntity(pos);
                    }
                }
                worldIn.spawnEntity(entity);
            }
        }
    }

    private static boolean canFallThrough(IBlockState state) {
        Block block = state.getBlock();
        Material material = state.getMaterial();
        return block == Blocks.FIRE || material == Material.AIR || material == Material.WATER || material == Material.LAVA;
    }
}
