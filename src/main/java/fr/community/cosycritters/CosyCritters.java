package fr.community.cosycritters;

import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Experimental Forge 1.18.2 reimplementation of a subset of PigCart's Cosy Critters.
 * No original class files or 1.20 Mixins are reused. Textures come from the MIT-licensed mod.
 */
@Mod("cosycritters")
public class CosyCritters {
    private static final DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, "cosycritters");
    public static final RegistryObject<SimpleParticleType> BIRD = PARTICLES.register("bird", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> MOTH = PARTICLES.register("moth", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> SPIDER = PARTICLES.register("spider", () -> new SimpleParticleType(false));

    public CosyCritters() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        PARTICLES.register(modBus);
    }

    @Mod.EventBusSubscriber(modid="cosycritters", value=Dist.CLIENT, bus=Mod.EventBusSubscriber.Bus.MOD)
    public static class Factories {
        @SubscribeEvent
        public static void registerFactories(ParticleFactoryRegisterEvent event) {
            Minecraft.getInstance().particleEngine.register(BIRD.get(), sprites -> new CritterFactory(sprites, 0));
            Minecraft.getInstance().particleEngine.register(MOTH.get(), sprites -> new CritterFactory(sprites, 1));
            Minecraft.getInstance().particleEngine.register(SPIDER.get(), sprites -> new CritterFactory(sprites, 2));
        }
    }

    @Mod.EventBusSubscriber(modid="cosycritters", value=Dist.CLIENT, bus=Mod.EventBusSubscriber.Bus.FORGE)
    public static class Spawns {
        private static int ticks = 0;
        @SubscribeEvent
        public static void tick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) return;
            Minecraft mc = Minecraft.getInstance();
            ClientLevel level = mc.level;
            if (level == null || mc.player == null || mc.isPaused()) return;
            Random random = level.random;
            ticks++;
            if (ticks % 55 == 0 && random.nextFloat() < 0.70f) {
                int x=Mth.floor(mc.player.getX())+random.nextInt(37)-18;
                int z=Mth.floor(mc.player.getZ())+random.nextInt(37)-18;
                int top=level.getHeight(Heightmap.Types.MOTION_BLOCKING, x,z);
                int y=top+3+random.nextInt(9);
                BlockPos p=new BlockPos(x,y,z);
                if (Math.abs(y-mc.player.getY())<30 && level.canSeeSky(p) && level.getBlockState(p).isAir()) {
                    // Spawn 1-3 birds with slight offsets; only a rudimentary flock in this alpha.
                    int count=1+random.nextInt(3);
                    for (int i=0;i<count;i++) level.addParticle(BIRD.get(), x+i*1.4, y+random.nextFloat()*2,z+i*0.6,
                           (random.nextBoolean()?1:-1)*(0.035+random.nextDouble()*0.045),0,random.nextGaussian()*0.012);
                }
            }
            if (ticks % 28 == 0) {
                int x=Mth.floor(mc.player.getX())+random.nextInt(27)-13;
                int z=Mth.floor(mc.player.getZ())+random.nextInt(27)-13;
                int y=Mth.floor(mc.player.getY())+random.nextInt(15)-7;
                BlockPos p=new BlockPos(x,y,z);
                if (level.getBlockState(p).isAir() && level.getMaxLocalRawBrightness(p)<10 && level.getBlockState(p.below()).isAir()) {
                    // Prefer dark spots next to light sources.
                    for (BlockPos neighbour : BlockPos.betweenClosed(p.offset(-3,-2,-3),p.offset(3,2,3))) {
                        if (level.getBlockState(neighbour).getLightEmission()>8) {
                            level.addParticle(MOTH.get(), x+0.5,y+0.5,z+0.5,0,0,0);
                            break;
                        }
                    }
                }
            }
            if (ticks % 70 == 0) {
                int x=Mth.floor(mc.player.getX())+random.nextInt(25)-12;
                int z=Mth.floor(mc.player.getZ())+random.nextInt(25)-12;
                int y=Mth.floor(mc.player.getY())+random.nextInt(17)-8;
                BlockPos p=new BlockPos(x,y,z);
                if (level.getBlockState(p).is(Blocks.COBWEB)) {
                    level.addParticle(SPIDER.get(), x+0.5,y+0.5,z+0.5,0,0,0);
                }
            }
        }
    }

    private static class CritterFactory implements ParticleProvider<SimpleParticleType> {
        final SpriteSet sprites;
        final int kind;
        CritterFactory(SpriteSet sprites,int kind){this.sprites=sprites;this.kind=kind;}
        @Override public Particle createParticle(SimpleParticleType type, ClientLevel level,
                       double x,double y,double z,double xd,double yd,double zd) {
            return new Critter(level,x,y,z,xd,yd,zd,sprites,kind);
        }
    }

    private static class Critter extends TextureSheetParticle {
        final SpriteSet sprites;
        final int kind;
        private final double startX,startY,startZ;
        Critter(ClientLevel level,double x,double y,double z,double xd,double yd,double zd,SpriteSet sprites,int kind){
            super(level,x,y,z,xd,yd,zd);
            this.sprites=sprites;this.kind=kind;
            this.startX=x;this.startY=y;this.startZ=z;
            this.hasPhysics=false;this.gravity=0;this.friction=1.0f;
            this.xd=xd;this.yd=yd;this.zd=zd;
            this.lifetime=kind==0 ? 180+random.nextInt(100) : kind==1 ? 120+random.nextInt(100) : 65+random.nextInt(60);
            this.quadSize=kind==0 ? 0.75f : kind==1 ? 0.23f : 0.16f;
            this.alpha=1.0f;
            this.setSpriteFromAge(sprites);
        }
        @Override public void tick(){
            super.tick();
            if(kind==0){
                // Glide with small wingbeat-driven vertical movement.
                this.yd = Math.sin(age*0.17)*0.012;
                this.zd += (random.nextDouble()-0.5)*0.0016;
            } else if (kind==1){
                this.xd=0.020*Math.sin(age*0.19+startX);
                this.yd=0.017*Math.cos(age*0.12+startY);
                this.zd=0.018*Math.cos(age*0.16+startZ);
            } else {
                this.xd=0.009*Math.cos(age*0.08+startX);
                this.yd=-0.002;
                this.zd=0.009*Math.sin(age*0.08+startZ);
            }
            this.setSpriteFromAge(sprites);
            if (age > lifetime-16) this.alpha=Math.max(0.0f,(lifetime-age)/16f);
        }
        @Override public ParticleRenderType getRenderType(){return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;}
    }
}
