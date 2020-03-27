package me.paulf.minecraftmania.function;

import me.paulf.minecraftmania.PostProcessingEffect;
import me.paulf.minecraftmania.RunningFunction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.time.Duration;

public class PostProcessingFunction extends DurationFunction {
    private final ResourceLocation shader;

    public PostProcessingFunction(final ResourceLocation shader, final Duration duration) {
        super(duration);
        this.shader = shader;
    }

    @Override
    protected RunningFunction createFunction() {
        final ResourceLocation shader = this.shader;
        return new RunningFunction() {
            final PostProcessingEffect effect = new PostProcessingEffect(shader);

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
