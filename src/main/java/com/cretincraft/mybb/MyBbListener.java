package com.cretincraft.mybb;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

import java.sql.SQLException;

public class MyBbListener implements Listener {
    private final MyBb mybb;

    public MyBbListener(MyBb mybb) {
        this.mybb = mybb;
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        try {
            if (this.mybb.hasAccount(event.getPlayer())) {
                event.getPlayer().addAttachment(this.mybb, "mybb.account", true);
            }
        } catch (SQLException exception) {
            this.mybb.getLogger().severe("SQLException while checking for account!");

            exception.printStackTrace();
        }
    }
}
