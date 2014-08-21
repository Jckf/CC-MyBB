package com.cretincraft.mybb;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.*;

public class MyBb extends JavaPlugin {
    private Connection db;

    private PreparedStatement testSelect;
    private PreparedStatement selectFid;

    public void onEnable() {
        this.getLogger().info("Loading configuration...");

        this.saveDefaultConfig();

        this.getLogger().info("Testing database connection...");

        if (this.getDb() == null) {
            this.getLogger().warning("Database connection failed! Disabling plugin...");
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }

        this.getLogger().info("Registering listeners...");

        this.getServer().getPluginManager().registerEvents(new MyBbListener(this), this);
    }

    public void onDisable() {
        this.getLogger().info("Unregistering listeners...");

        HandlerList.unregisterAll(this);

        this.getLogger().info("Cleaning up database connection...");

        try {
            if (this.testSelect != null)
                this.testSelect.close();
        } catch (SQLException ignored) { }

        try {
            if (this.db != null)
                this.db.close();
        } catch (SQLException ignored) { }
    }

    private Connection getDb() {
        if (this.db != null) {
            try {
                if (this.db.isClosed())
                    throw new SQLException("An existing connection was closed.");

                ResultSet result = this.testSelect.executeQuery();
                if (result.next())
                    return this.db;
            } catch (SQLException ignored) { }

            // It wasn't null, but we couldn't use it. Be gone, evildoer!
            this.db = null;
        }

        try {
            Class.forName("com.mysql.jdbc.Driver");
            this.db = DriverManager.getConnection(
                "jdbc:mysql://" + this.getConfig().getString("mysql.hostname", "localhost") + ":" + this.getConfig().getInt("mysql.port", 3306) + "/" + this.getConfig().getString("mysql.schema", "mybb"),
                this.getConfig().getString("mysql.username", "root"),
                this.getConfig().getString("mysql.password", "")
            );

            // New connection means new statements.
            this.testSelect = this.db.prepareStatement("SELECT 1");
            this.selectFid = this.db.prepareStatement("SELECT 1 FROM " + this.getConfig().getString("mysql.prefix", "mybb_") + "userfields WHERE fid" + this.getConfig().getString("account.field") + "=?");
        } catch (ClassNotFoundException exception) {
            this.db = null;
            this.getLogger().warning("ClassNotFoundException while connecting to database!");
        } catch (SQLException exception) {
            this.db = null;
            this.getLogger().warning("SQLException while connecting to database!");
            exception.printStackTrace();
        }

        return this.db;
    }

    public boolean hasAccount(Player player) throws SQLException {
        this.getDb();

        this.selectFid.setString(1, player.getName());

        return this.selectFid.executeQuery().isBeforeFirst();
    }
}
