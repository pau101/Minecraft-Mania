package me.paulf.minecraftmania.function;

import me.paulf.minecraftmania.MinecraftMania;
import me.paulf.minecraftmania.PostProcessingEffect;
import me.paulf.minecraftmania.RunningFunction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.time.Duration;

public class JpegFunction extends DurationFunction {
    public JpegFunction(final Duration duration) {
        super(duration);
    }

    @Override
    protected RunningFunction createFunction() {
        return new RunningFunction() {
            final PostProcessingEffect effect = new PostProcessingEffect(new ResourceLocation(MinecraftMania.ID, "shaders/post/jpeg.json"));

            @SubscribeEvent
            public void render(final TickEvent.RenderTickEvent e) {
                if (e.phase == TickEvent.Phase.END) {
                    this.effect.render(e.renderTickTime);
                }
            }

            @Override
            public void stop() {
                this.effect.close();
            }
        };
    }
}
