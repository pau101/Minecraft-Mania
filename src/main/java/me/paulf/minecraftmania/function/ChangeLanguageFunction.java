package me.paulf.minecraftmania.function;

import me.paulf.minecraftmania.MinecraftMania;
import me.paulf.minecraftmania.RunningFunction;
import me.paulf.minecraftmania.ViewerCommand;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.Language;
import net.minecraft.client.resources.LanguageManager;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import java.time.Duration;

public class ChangeLanguageFunction extends DurationFunction {
    private final String language;

    public ChangeLanguageFunction(final String language, final Duration duration) {
        super(duration);
        this.language = language;
    }

    @Override
    public ITextComponent getMessage(final MinecraftMania.Context context) {
        final LanguageManager manager = Minecraft.getInstance().getLanguageManager();
        final Language language = manager.getLanguage(this.language);
        return new TranslationTextComponent(
            "mania.lang",
            context.getViewerName(),
            new StringTextComponent(language == null ? "missingno" : language.getName()).applyTextStyle(TextFormatting.LIGHT_PURPLE)
        );
    }

    @Override
    protected RunningFunction createFunction() {
        return new RunningFunction() {
            String originalLangCode;

            @Override
            public ITextComponent getMessage(final ViewerCommand command, final int seconds) {
                final LanguageManager manager = Minecraft.getInstance().getLanguageManager();
                final Language language = manager.getLanguage(ChangeLanguageFunction.this.language);
                return new TranslationTextComponent("mania.lang.running", new StringTextComponent(language == null ? "missingno" : language.getName()).applyTextStyle(TextFormatting.LIGHT_PURPLE), seconds).applyTextStyle(TextFormatting.ITALIC);
            }

            @Override
            public void start() {
                this.originalLangCode = this.setLanguage(ChangeLanguageFunction.this.language);
            }

            @Override
            public void stop() {
                if (this.originalLangCode == null) {
                    return;
                }
                final Minecraft mc = Minecraft.getInstance();
                final LanguageManager manager = mc.getLanguageManager();
                final Language language = manager.getLanguage(ChangeLanguageFunction.this.language);
                if (manager.getCurrentLanguage().equals(language)) {
                    this.setLanguage(this.originalLangCode);
                }
            }

            private String setLanguage(final String langCode) {
                final Minecraft mc = Minecraft.getInstance();
                final LanguageManager manager = mc.getLanguageManager();
                final Language currentLanguage = manager.getCurrentLanguage();
                final Language language = manager.getLanguage(langCode);
                //noinspection ConstantConditions
                if (language != null && !currentLanguage.equals(language)) {
                    manager.setCurrentLanguage(language);
                    mc.gameSettings.language = language.getCode();

                    //ForgeHooksClient.refreshResources(mc, net.minecraftforge.resource.VanillaResourceType.LANGUAGES);
                    manager.onResourceManagerReload(mc.getResourceManager());

                    mc.fontRenderer.setBidiFlag(manager.isCurrentLanguageBidirectional());
                    mc.gameSettings.saveOptions();

                    if (mc.currentScreen != null) {
                        mc.currentScreen.init(mc, mc.getMainWindow().getScaledWidth(), mc.getMainWindow().getScaledHeight());
                    }

                    return currentLanguage.getCode();
                }
                return null;
            }
        };
    }
}
