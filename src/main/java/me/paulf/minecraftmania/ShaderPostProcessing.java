package me.paulf.minecraftmania;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class ShaderPostProcessing {
    private static final ResourceLocation SHADER_LOCATION = new ResourceLocation(MinecraftMania.ID, "shaders/post/jpeg.json");

    private final PostProcessingEffect effect;

    public ShaderPostProcessing() {
        this.effect = new PostProcessingEffect(SHADER_LOCATION);
        //LiveEdit.instance().watch(new ResourceLocation(MinecraftMania.ID, "shaders/program/jpeg.fsh"), this.effect::reload);
    }

    @SubscribeEvent
    public void onRenderTick(final TickEvent.RenderTickEvent e) {
        if (e.phase == TickEvent.Phase.END) {
            this.effect.render(e.renderTickTime);
        }
    }
}
