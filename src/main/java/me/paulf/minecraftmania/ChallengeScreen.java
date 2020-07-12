package me.paulf.minecraftmania;

import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nullable;

public abstract class ChallengeScreen extends Screen {
    @Nullable
    protected final Screen parent;

    protected ChallengeScreen(@Nullable final Screen parent, final ITextComponent title) {
        super(title);
        this.parent = parent;
    }

    @Override
    public final boolean isPauseScreen() {
        return false;
    }

    @Override
    public final boolean shouldCloseOnEsc() {
        return true;
    }

    @Override
    public void tick() {
        if (this.parent != null && !(this.parent instanceof ChatScreen)) {
            this.parent.tick();
        }
        super.tick();
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) this.minecraft.displayGuiScreen(this.parent);
    }

    protected final void renderParent(final int mouseX, final int mouseY, final float delta) {
        if (this.parent != null && !(this.parent instanceof ChatScreen)) {
            this.parent.render(mouseX, mouseY, delta);
        }
    }

    protected final void play(final ISound sound) {
        if (this.minecraft != null) this.minecraft.getSoundHandler().play(sound);
    }

    protected void complete() {
        this.play(SimpleSound.master(SoundEvents.ENTITY_PLAYER_LEVELUP, 1.0F));
        this.onClose();
    }
}
