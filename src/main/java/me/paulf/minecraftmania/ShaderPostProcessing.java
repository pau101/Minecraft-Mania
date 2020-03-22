package me.paulf.minecraftmania;

import net.minecraft.client.Minecraft;
import net.minecraft.client.shader.ShaderGroup;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class ShaderPostProcessing {
    private static final ResourceLocation SHADER_LOCATION = new ResourceLocation(MinecraftMania.ID, "shaders/post/jpeg.json");

    public ShaderPostProcessing() {
        LiveEdit.instance().watch(new ResourceLocation(MinecraftMania.ID, "shaders/program/jpeg.fsh"), this::load);
    }

    @SubscribeEvent
    public void onRenderTick(final TickEvent.RenderTickEvent e) {
        if (e.phase == TickEvent.Phase.START && Minecraft.getInstance().player != null) {
            final ShaderGroup activeGroup = Minecraft.getInstance().gameRenderer.getShaderGroup();
            if (activeGroup == null || !SHADER_LOCATION.toString().equals(activeGroup.getShaderGroupName())) {
                this.load();
            }
        }
    }

    private void load() {
        Minecraft.getInstance().gameRenderer.loadShader(SHADER_LOCATION);
    }
}
