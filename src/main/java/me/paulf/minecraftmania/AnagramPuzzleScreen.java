package me.paulf.minecraftmania;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.UnmodifiableListIterator;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.util.Random;

public class AnagramPuzzleScreen extends ChallengeScreen {
    private final AnagramChallenge challenge;

    private TextFieldWidget answer;

    private Anagram<?> anagram = AnagramChallenge.FIRCATMEN;

    private ImmutableList<String> hints = ImmutableList.of();

    private int shownHints = 0;

    public AnagramPuzzleScreen(@Nullable final Screen parent, final AnagramChallenge challenge) {
        super(parent, new TranslationTextComponent("mania.anagram.title"));
        this.challenge = challenge;
    }

    @Override
    public void init(final Minecraft minecraft, final int width, final int height) {
        super.init(minecraft, width, height);
        this.minecraft.keyboardListener.enableRepeatEvents(true);
        this.answer = new TextFieldWidget(this.font, this.width / 2 - 180 / 2, this.height / 2 + 20, 180, 20, "");
        this.answer.setFocused2(true);
        this.answer.setResponder(this::onAnswerChange);
        this.children.add(this.answer);
        this.setFocusedDefault(this.answer);
        this.anagram = this.challenge.generate(new Random());
        if (this.minecraft.world != null) {
            this.hints = this.anagram.getHints(this.minecraft.world).stream().map(ITextComponent::getFormattedText).collect(ImmutableList.toImmutableList());
        } else {
            this.hints = ImmutableList.of();
        }
        this.shownHints = Integer.MAX_VALUE;
        /*this.answer.setMessage(this.anagram.value.replaceAll("(?<=.)", ".") + " Answer");*/
    }

    @Override
    public void tick() {
        super.tick();
        this.answer.tick();
    }

    @Override
    public void removed() {
        super.removed();
        this.minecraft.keyboardListener.enableRepeatEvents(false);
    }

    @SuppressWarnings("IntegerDivisionInFloatingPointContext")
    @Override
    public void render(final int mouseX, final int mouseY, final float delta) {
        this.renderParent(mouseX, mouseY, delta);
        RenderSystem.clear(GL11.GL_DEPTH_BUFFER_BIT, Minecraft.IS_RUNNING_ON_MAC);
        this.renderBackground();
        this.drawCenteredString(this.font, this.title.getFormattedText(), this.width / 2, this.height / 2 - 70, 0xFFFFFF);
        RenderSystem.pushMatrix();
        RenderSystem.translatef(this.width / 2, this.height / 2 - 40, 0.0F);
        RenderSystem.scalef(2.0F, 2.0F, 1.0F);
        this.font.drawString(this.anagram.value, -this.font.getStringWidth(this.anagram.value) / 2, -this.font.FONT_HEIGHT / 2, 0xFFFFFF);
        RenderSystem.popMatrix();
        final UnmodifiableListIterator<String> it = this.hints.listIterator();
        for (int index; it.hasNext() && (index = it.nextIndex()) < this.shownHints; ) {
            this.font.drawString(it.next(), this.answer.x + 4, this.height / 2 - 24 + index * (1 + this.font.FONT_HEIGHT), 0xC0C0C0);
        }
        final String word = this.anagram.getWord();
        final int dist = MathHelper.clamp(StringUtils.getLevenshteinDistance(word, this.answer.getText()), 0, word.length());
        final int size = this.answer.getWidth();
        fill(this.answer.x, this.answer.y + this.answer.getHeight() + 4, this.answer.x + (size - dist * size / word.length()), this.answer.y + this.answer.getHeight() + 4 + 2, 0xFFFFFFFF);
        this.answer.render(mouseX, mouseX, delta);
        super.render(mouseX, mouseY, delta);
    }

    private void onAnswerChange(final String s) {
        if (this.anagram.test(s)) {
            this.complete();
        }
    }
}
