/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client/).
 * Copyright (c) 2022 Meteor Development.
 */

package RewardedIvan.asteroidclient.gui.screens;

import RewardedIvan.asteroidclient.gui.widgets.pressable.WButton;
import RewardedIvan.asteroidclient.gui.GuiTheme;
import RewardedIvan.asteroidclient.gui.WindowScreen;

public abstract class AddAccountScreen extends WindowScreen {
    public final AccountsScreen parent;
    public WButton add;
    private int timer;

    protected AddAccountScreen(GuiTheme theme, String title, AccountsScreen parent) {
        super(theme, title);
        this.parent = parent;
    }

    @Override
    public void tick() {
        if (locked) {
            if (timer > 2) {
                add.set(getNext(add));
                timer = 0;
            }
            else {
                timer++;
            }
        }

        else if (!add.getText().equals("Add")) {
            add.set("Add");
        }
    }

    private String getNext(WButton add) {
        return switch (add.getText()) {
            case "Add", "oo0" -> "ooo";
            case "ooo" -> "0oo";
            case "0oo" -> "o0o";
            case "o0o" -> "oo0";
            default -> "Add";
        };
    }
}
