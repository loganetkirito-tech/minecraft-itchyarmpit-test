package xyz.bonfiremc.windy;
import java.nio.file.Path;
import java.nio.file.Files;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import net.minecraftforge.fml.loading.FMLPaths;

public class WindyConfig {
    public static boolean enabled=true, requireSky=true, scaleWithAltitude=false;
    public static int minimumY=55, maximumY=240, spawnAttempts=7;
    public static double multiplier=1.0, lowAltitudeMultiplier=0.5, highAltitudeMultiplier=2.0;
    public static void load(){
        Path path=FMLPaths.CONFIGDIR.get().resolve("windy-1.18.2.properties");
        Properties p=new Properties();
        try {
            if(Files.exists(path)) try(InputStream in=Files.newInputStream(path)){p.load(in);}
            enabled=Boolean.parseBoolean(p.getProperty("enabled","true"));
            requireSky=Boolean.parseBoolean(p.getProperty("requireSky","true"));
            scaleWithAltitude=Boolean.parseBoolean(p.getProperty("scaleWithAltitude","false"));
            minimumY=Integer.parseInt(p.getProperty("minimumY","55"));
            maximumY=Integer.parseInt(p.getProperty("maximumY","240"));
            spawnAttempts=Integer.parseInt(p.getProperty("spawnAttempts","7"));
            multiplier=Double.parseDouble(p.getProperty("multiplier","1.0"));
            lowAltitudeMultiplier=Double.parseDouble(p.getProperty("lowAltitudeMultiplier","0.5"));
            highAltitudeMultiplier=Double.parseDouble(p.getProperty("highAltitudeMultiplier","2.0"));
            p.setProperty("enabled",String.valueOf(enabled));
            p.setProperty("requireSky",String.valueOf(requireSky));
            p.setProperty("scaleWithAltitude",String.valueOf(scaleWithAltitude));
            p.setProperty("minimumY",String.valueOf(minimumY));
            p.setProperty("maximumY",String.valueOf(maximumY));
            p.setProperty("spawnAttempts",String.valueOf(spawnAttempts));
            p.setProperty("multiplier",String.valueOf(multiplier));
            p.setProperty("lowAltitudeMultiplier",String.valueOf(lowAltitudeMultiplier));
            p.setProperty("highAltitudeMultiplier",String.valueOf(highAltitudeMultiplier));
            Files.createDirectories(path.getParent());
            try(OutputStream out=Files.newOutputStream(path)){p.store(out,"Windy Forge 1.18.2 community port settings");}
        } catch(Exception exception) {System.err.println("[Windy] Could not read settings: "+exception);}
    }
}
