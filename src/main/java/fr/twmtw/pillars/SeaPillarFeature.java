package fr.twmtw.pillars;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import java.util.Random;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/** Independent approximation of TWMTW pillar shapes, NOT BWG decompiled code. */
public class SeaPillarFeature extends Feature<NoneFeatureConfiguration> {
    public SeaPillarFeature(Codec<NoneFeatureConfiguration> codec) { super(codec); }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel world = context.level();
        Random rand = context.random();
        BlockPos pos = context.origin();
        int centerX = pos.getX(), centerZ = pos.getZ();
        int seabed = world.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, centerX, centerZ) - 1;
        int waterline = world.getSeaLevel();
        // Only underwater columns, prevents pillars filling dry land and cliff tops.
        if (seabed >= waterline - 2 || seabed < world.getMinBuildHeight() + 5) return false;
        BlockPos floor = new BlockPos(centerX, seabed, centerZ);
        if (!world.getBlockState(floor).getFluidState().isEmpty() || world.getBlockState(floor).isAir()) return false;
        int height = 15 + rand.nextInt(61);
        int radius = 7 + rand.nextInt(14);
        double topScale = 0.1 + rand.nextDouble() * 0.4;
        double phase = rand.nextDouble() * Math.PI * 2.0;
        double freq = 0.01 + rand.nextDouble() * 0.09;
        int maxY = Math.min(world.getMaxBuildHeight() - 2, seabed + height);
        boolean placed = false;
        BlockPos.MutableBlockPos target = new BlockPos.MutableBlockPos();
        for (int y = seabed; y <= maxY; y++) {
            double t = (y - seabed) / (double) Math.max(1, height);
            double taper = 1.0 - (1.0 - topScale) * t;
            // Nonlinear contour: slight waist, naturally uneven sides and tilted center.
            double r = radius * taper * (1.0 + 0.10 * Math.sin(t * 7.0 + phase));
            double dx0 = Math.sin(t * 3.5 + phase) * 1.7 * t;
            double dz0 = Math.cos(t * 3.0 + phase) * 1.7 * t;
            int reach = (int) Math.ceil(radius * 1.2 + 3);
            for (int dx = -reach; dx <= reach; dx++) for (int dz = -reach; dz <= reach; dz++) {
                double x = dx - dx0, z = dz - dz0;
                double theta = Math.atan2(z,x);
                double irregular = 1 + 0.10 * Math.sin(theta * 5 + phase + y * freq * 5)
                                   + 0.07 * Math.cos(theta * 9 - y * freq * 8);
                if (x*x + z*z > r*r*irregular*irregular) continue;
                target.set(centerX + dx, y, centerZ + dz);
                BlockState old = world.getBlockState(target);
                if (!old.isAir() && old.getFluidState().isEmpty() && old.getBlock() != Blocks.GRASS_BLOCK && old.getBlock() != Blocks.DIRT) continue;
                boolean shell = x*x + z*z > (r - 1.4)*(r - 1.4)*irregular*irregular;
                BlockState state = Blocks.STONE.defaultBlockState();
                if (shell && rand.nextFloat() < 0.11f) state = Blocks.MOSSY_COBBLESTONE.defaultBlockState();
                else if (shell && rand.nextFloat() < 0.035f) state = Blocks.MOSS_BLOCK.defaultBlockState();
                world.setBlock(target, state, 2);
                placed = true;
            }
        }
        return placed;
    }
}
