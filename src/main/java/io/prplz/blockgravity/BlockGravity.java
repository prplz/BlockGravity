package io.prplz.blockgravity;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.item.EntityItem;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.RenderTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;

@Mod(modid = BlockGravity.MODID, version = BlockGravity.VERSION, useMetadata = true)
public class BlockGravity {

    public static final String MODID = "blockgravity";
    public static final String VERSION = "%%VERSION%%";

    private static final Minecraft mc = Minecraft.getMinecraft();

    private final long[] tickTimes = new long[10];
    private int tick = 0;
    private double tps;
    private String tpsString;

    @EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onEntityJoinWorld(EntityJoinWorldEvent event) {
        if (event.getEntity() instanceof EntityItem) {
            event.getEntity().setDead();
        }
    }

    @SubscribeEvent
    public void onServerTick(ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            long now = System.nanoTime();
            if (tick > tickTimes.length) {
                tps = 1e9 / ((now - tickTimes[tick % tickTimes.length]) / tickTimes.length);
                tpsString = String.format("%.01f ticks/second", Math.min(tps, 20.0));
            }
            tickTimes[tick++ % tickTimes.length] = now;
        }
    }

    @SubscribeEvent
    public void onRenderTick(RenderTickEvent event) {
        if (!mc.gameSettings.showDebugInfo && mc.inGameHasFocus) {
            if (tick >= tickTimes.length) {
                renderTps();
            }
        }
    }

    private void renderTps() {
        mc.fontRenderer.drawString(tpsString, 4, 4, 0xFFFFFFFF, true);
    }
}
