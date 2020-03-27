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

    private PostProcessingEffect effect;

    @Override
    protected RunningFunction createFunction() {
        if (this.effect == null) {
            this.effect = new PostProcessingEffect(this.shader);
            //LiveEdit.instance().watch(new ResourceLocation(MinecraftMania.ID, "shaders/program"), this.effect::reload);
        }
        return new RunningFunction() {
            @SubscribeEvent
            public void render(final TickEvent.RenderTickEvent e) {
                if (e.phase == TickEvent.Phase.END) {
                    PostProcessingFunction.this.effect.render(e.renderTickTime);
                }
            }
        };
    }
}
