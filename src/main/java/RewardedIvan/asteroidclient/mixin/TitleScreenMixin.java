/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client/).
 * Copyright (c) 2022 Meteor Development.
 */

package RewardedIvan.asteroidclient.mixin;

import RewardedIvan.asteroidclient.utils.Utils;
import RewardedIvan.asteroidclient.utils.misc.Version;
import RewardedIvan.asteroidclient.utils.player.TitleScreenCredits;
import com.google.gson.JsonParser;
import RewardedIvan.asteroidclient.AsteroidClient;
import RewardedIvan.asteroidclient.systems.config.Config;
import RewardedIvan.asteroidclient.utils.network.Http;
import RewardedIvan.asteroidclient.utils.network.MeteorExecutor;
import RewardedIvan.asteroidclient.utils.render.prompts.OkPrompt;
import RewardedIvan.asteroidclient.utils.render.prompts.YesNoPrompt;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TitleScreen.class)
public class TitleScreenMixin extends Screen {
    public TitleScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/TitleScreen;drawStringWithShadow(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/font/TextRenderer;Ljava/lang/String;III)V", ordinal = 0))
    private void onRenderIdkDude(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo info) {
        if (Utils.firstTimeTitleScreen) {
            Utils.firstTimeTitleScreen = false;
            AsteroidClient.LOG.info("Checking latest version of Meteor Client");

            MeteorExecutor.execute(() -> {
                String res = Http.get("https://meteorclient.com/api/stats").sendString();
                if (res == null) return;

                Version latestVer = new Version(JsonParser.parseString(res).getAsJsonObject().get("version").getAsString());

                if (latestVer.isHigherThan(AsteroidClient.VERSION)) {
                    YesNoPrompt.create()
                        .title("New Update")
                        .message("A new version of Meteor has been released.")
                        .message("Your version: %s", AsteroidClient.VERSION)
                        .message("Latest version: %s", latestVer)
                        .message("Do you want to update?")
                        .onYes(() -> Util.getOperatingSystem().open("https://meteorclient.com/"))
                        .onNo(() -> OkPrompt.create()
                            .title("Are you sure?")
                            .message("Using old versions of Meteor is not recommended")
                            .message("and could report in issues.")
                            .id("new-update-no")
                            .onOk(this::close)
                            .show())
                        .id("new-update")
                        .show();
                }
            });
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void onRender(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo info) {
        if (Config.get().titleScreenCredits.get()) TitleScreenCredits.render(matrices);
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void onMouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> info) {
        if (Config.get().titleScreenCredits.get() && button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            if (TitleScreenCredits.onClicked(mouseX, mouseY)) info.setReturnValue(true);
        }
    }
}
