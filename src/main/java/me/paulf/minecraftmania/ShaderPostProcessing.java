package me.paulf.minecraftmania;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class ShaderPostProcessing {
    private static final Logger LOGGER = LogManager.getLogger();

    private static final ResourceLocation SHADER_LOCATION = new ResourceLocation(MinecraftMania.ID, "shaders/post/jpeg.json");

    private final PostProcessingEffect effect;

    public ShaderPostProcessing() {
        LiveEdit.instance().watch(new ResourceLocation(MinecraftMania.ID, "shaders/program/jpeg.fsh"), this::load);
        this.effect = new PostProcessingEffect(SHADER_LOCATION);
    }

    @SubscribeEvent
    public void onRenderTick(final TickEvent.RenderTickEvent e) {
        if (e.phase == TickEvent.Phase.END) {
            this.render(e.renderTickTime);
        }
    }

    private void load() {
        this.effect.reload();
    }

    private void render(final float delta) {
        this.effect.render(delta);
    }
}
