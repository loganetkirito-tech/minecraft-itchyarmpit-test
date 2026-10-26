package xyz.bonfiremc.windy;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.eventbus.api.IEventBus;
import java.util.Random;

@Mod("windy")
public class WindyMod {
    private static final DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, "windy");
    public static final RegistryObject<SimpleParticleType> WIND = PARTICLES.register("wind", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> STRONG = PARTICLES.register("strong_wind", () -> new SimpleParticleType(false));

    public WindyMod() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        PARTICLES.register(bus);
        WindyConfig.load();
    }

    @Mod.EventBusSubscriber(modid="windy", value=Dist.CLIENT, bus=Mod.EventBusSubscriber.Bus.MOD)
    public static class ParticleRegistration {
        @SubscribeEvent
        public static void registerFactories(ParticleFactoryRegisterEvent event) {
            Minecraft.getInstance().particleEngine.register(WIND.get(), sprites -> new WindFactory(sprites, false));
            Minecraft.getInstance().particleEngine.register(STRONG.get(), sprites -> new WindFactory(sprites, true));
        }
    }

    @Mod.EventBusSubscriber(modid="windy", value=Dist.CLIENT, bus=Mod.EventBusSubscriber.Bus.FORGE)
    public static class ClientTick {
        @SubscribeEvent
        public static void tick(TickEvent.ClientTickEvent event) {
            if(event.phase != TickEvent.Phase.END || !WindyConfig.enabled) return;
            Minecraft mc=Minecraft.getInstance();
            ClientLevel level=mc.level;
            if(level==null || mc.player==null || mc.isPaused()) return;
            Random rand=level.random;
            int attempts=WindyConfig.spawnAttempts;
            for(int i=0; i<attempts; i++) {
                int x=Mth.floor(mc.player.getX())+rand.nextInt(49)-24;
                int z=Mth.floor(mc.player.getZ())+rand.nextInt(49)-24;
                int y=Mth.floor(mc.player.getY())+rand.nextInt(25)-12;
                if(y<WindyConfig.minimumY) continue;
                BlockPos pos=new BlockPos(x,y,z);
                if(WindyConfig.requireSky && !level.canSeeSky(pos)) continue;
                if(!level.getBlockState(pos).isAir()) continue;
                if(!level.getFluidState(pos).isEmpty()) continue;
                double mult=WindyConfig.multiplier;
                if(WindyConfig.scaleWithAltitude) {
                    double t=Mth.clamp((y-WindyConfig.minimumY)/(double)Math.max(1,WindyConfig.maximumY-WindyConfig.minimumY),0,1);
                    mult *= WindyConfig.lowAltitudeMultiplier+(WindyConfig.highAltitudeMultiplier-WindyConfig.lowAltitudeMultiplier)*t;
                }
                Biome biome=level.getBiome(pos).value();
                if(biome.getPrecipitation()==Biome.Precipitation.NONE) mult*=0.75;
                if(level.isRainingAt(pos)) mult*=2.0;
                if(level.isThundering()) mult*=1.55;
                if(rand.nextDouble() >= Math.min(1.0,0.065 * mult)) continue;
                boolean strong = level.isThundering() || (level.isRainingAt(pos) && rand.nextInt(3)==0);
                double vx=0.045+rand.nextDouble()*0.075;
                double vy=(rand.nextDouble()-0.5)*0.013;
                double vz=(rand.nextDouble()-0.5)*0.05;
                level.addParticle(strong ? STRONG.get() : WIND.get(), x+rand.nextDouble(), y+rand.nextDouble(),z+rand.nextDouble(),vx,vy,vz);
            }
        }
    }

    private static final class WindFactory implements ParticleProvider<SimpleParticleType> {
        final SpriteSet sprites;
        final boolean strong;
        WindFactory(SpriteSet sprites, boolean strong) {this.sprites=sprites;this.strong=strong;}
        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x,double y,double z,double xd,double yd,double zd) {
            return new WindParticle(level,x,y,z,xd,yd,zd,sprites,strong);
        }
    }

    private static final class WindParticle extends TextureSheetParticle {
        final SpriteSet sprites;
        WindParticle(ClientLevel level,double x,double y,double z,double xd,double yd,double zd,SpriteSet sprites,boolean strong){
            super(level,x,y,z,xd,yd,zd);
            this.sprites=sprites;
            this.hasPhysics=false;
            this.gravity=0;
            this.friction=1.0f;
            this.xd=xd;this.yd=yd;this.zd=zd;
            this.lifetime=strong? 19+random.nextInt(15) : 24+random.nextInt(22);
            this.quadSize=strong?0.65f:0.43f;
            this.alpha=0.80f;
            this.setSpriteFromAge(sprites);
        }
        @Override public void tick(){
            super.tick();
            this.setSpriteFromAge(sprites);
            if(age>lifetime-7) this.alpha = Math.max(0f, (lifetime-age)/7f)*0.80f;
        }
        @Override public ParticleRenderType getRenderType(){ return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT; }
    }
}
