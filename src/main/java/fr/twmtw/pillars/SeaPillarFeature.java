package fr.twmtw.pillars;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/** Independent HTTYD-inspired pillar feature, not a decompilation of BWG code. */
public class SeaPillarFeature extends Feature<NoneFeatureConfiguration> {
    public SeaPillarFeature(Codec<NoneFeatureConfiguration> codec) { super(codec); }

    private static double clamp(double x, double min, double max) { return Math.max(min, Math.min(max, x)); }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel world = context.level();
        Random random = context.random();
        BlockPos origin = context.origin();
        int cx = origin.getX(), cz = origin.getZ();
        int floorY = world.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, cx, cz) - 1;
        int sea = world.getSeaLevel();
        if (floorY <= world.getMinBuildHeight() + 4 || floorY >= sea - 3) return false;
        if (world.getBlockState(new BlockPos(cx, floorY + 1, cz)).getFluidState().isEmpty()) return false;

        int topY = Math.min(world.getMaxBuildHeight() - 24, sea + 25 + random.nextInt(61));
        int height = topY - floorY;
        int radius = 11 + random.nextInt(10); // 11-20; much wider than earlier build
        double phase = random.nextDouble() * Math.PI * 2;
        double twist = 2 + random.nextDouble() * 3;
        double waist = 0.07 + random.nextDouble() * 0.12;
        int reach = radius + 9;
        int written = 0;
        BlockPos.MutableBlockPos probe = new BlockPos.MutableBlockPos();

        // Vertical cliffs, no conical taper: radius changes slowly with altitude.
        // Small angular harmonics and offset drift make the cylinder asymmetric.
        for (int y = floorY; y <= topY; y++) {
            double t = (y - floorY) / (double) height;
            double centerX = Math.sin(phase + t * 3.5) * twist * t;
            double centerZ = Math.cos(phase * 1.3 + t * 3.0) * twist * t;
            double mid = Math.sin(Math.PI * t);
            double baseRadius = radius * (1 - waist * mid + 0.06 * Math.sin(t * 8 + phase));
            // Broad shelf near the summit, with nearly level top.
            if (t > 0.89) baseRadius *= 1.02;
            for (int dx = -reach; dx <= reach; dx++) {
                for (int dz = -reach; dz <= reach; dz++) {
                    double x = dx - centerX, z = dz - centerZ;
                    double angle = Math.atan2(z, x);
                    double modulation = 1 + 0.09 * Math.sin(angle * 5 + phase + t * 5)
                                          + 0.05 * Math.cos(angle * 9 - phase * 0.5 + t * 7);
                    double d = x * x + z * z;
                    double r = baseRadius * modulation;
                    if (d > r * r) continue;
                    probe.set(cx + dx, y, cz + dz);
                    BlockState old = world.getBlockState(probe);
                    // Do not excavate existing terrain, but replace water and plants.
                    if (!old.isAir() && old.getFluidState().isEmpty() && !old.getMaterial().isReplaceable()) continue;
                    boolean edge = d > (r - 1.9) * (r - 1.9);
                    BlockState stone = Blocks.STONE.defaultBlockState();
                    if (edge && random.nextInt(11) == 0) stone = Blocks.MOSSY_COBBLESTONE.defaultBlockState();
                    if (edge && random.nextInt(24) == 0) stone = Blocks.MOSS_BLOCK.defaultBlockState();
                    world.setBlock(probe, stone, 2);
                    written++;
                }
            }
        }
        if (written == 0) return false;

        // Match the summit drift; only plant where this feature generated solid stone.
        double summitX = Math.sin(phase + 3.5) * twist;
        double summitZ = Math.cos(phase * 1.3 + 3.0) * twist;
        int sx = cx + (int)Math.round(summitX), sz = cz + (int)Math.round(summitZ);
        int innerRadius = Math.max(5, radius - 4);
        for (int dx = -innerRadius; dx <= innerRadius; dx++) {
            for (int dz = -innerRadius; dz <= innerRadius; dz++) {
                double d = dx * dx + dz * dz;
                if (d > innerRadius * innerRadius) continue;
                probe.set(sx + dx, topY, sz + dz);
                BlockState b = world.getBlockState(probe);
                if (!b.is(Blocks.STONE) && !b.is(Blocks.MOSSY_COBBLESTONE) && !b.is(Blocks.MOSS_BLOCK)) continue;
                if (random.nextFloat() < 0.76f) world.setBlock(probe, random.nextFloat() < 0.25f ? Blocks.MOSS_BLOCK.defaultBlockState() : Blocks.GRASS_BLOCK.defaultBlockState(), 2);
            }
        }

        // Tree generation is coupled to THIS pillar, never the water surface.
        // A small set of tall birches, deliberately not a continuous forest canopy.
        int trees = 1 + random.nextInt(4);
        for (int i = 0; i < trees; i++) {
            int dx = random.nextInt(Math.max(1, innerRadius * 2 - 6)) - (innerRadius - 3);
            int dz = random.nextInt(Math.max(1, innerRadius * 2 - 6)) - (innerRadius - 3);
            if (dx * dx + dz * dz > (innerRadius - 3) * (innerRadius - 3)) continue;
            int x = sx + dx, z = sz + dz;
            probe.set(x, topY, z);
            BlockState ground = world.getBlockState(probe);
            if (!ground.is(Blocks.GRASS_BLOCK) && !ground.is(Blocks.MOSS_BLOCK)) continue;
            int treeHeight = 12 + random.nextInt(12);
            if (topY + treeHeight + 3 >= world.getMaxBuildHeight()) continue;
            boolean clear = true;
            for (int y = topY + 1; y <= topY + treeHeight + 3; y++) {
                probe.set(x, y, z);
                if (!world.getBlockState(probe).isAir() && world.getBlockState(probe).getFluidState().isEmpty()) { clear = false; break; }
            }
            if (!clear) continue;
            for (int y = topY + 1; y <= topY + treeHeight; y++) {
                probe.set(x, y, z);
                world.setBlock(probe, Blocks.BIRCH_LOG.defaultBlockState(), 2);
            }
            int crownY = topY + treeHeight - 1;
            for (int ly = -3; ly <= 2; ly++) {
                int leafR = (ly == -3 || ly == 2) ? 2 : 3;
                for (int lx = -leafR; lx <= leafR; lx++) {
                    for (int lz = -leafR; lz <= leafR; lz++) {
                        if (lx * lx + lz * lz > leafR * leafR + random.nextInt(3)) continue;
                        probe.set(x + lx, crownY + ly, z + lz);
                        BlockState old = world.getBlockState(probe);
                        if (old.isAir() || !old.getFluidState().isEmpty()) world.setBlock(probe, Blocks.OAK_LEAVES.defaultBlockState(), 2);
                    }
                }
            }
        }
        return true;
    }
}
