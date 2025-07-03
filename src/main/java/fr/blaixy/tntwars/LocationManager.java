package fr.blaixy.tntwars;

import fr.blaixy.tntwars.TNTWarsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class LocationManager {

    private TNTWarsPlugin plugin;

    public LocationManager() {
        this.plugin = TNTWarsPlugin.getInstance();
    }

    public Location getLobby() {
        String locationString = plugin.getConfig().getString("locations.lobby");
        return stringToLocation(locationString);
    }

    public Location getRedSpawn() {
        String locationString = plugin.getConfig().getString("locations.red-spawn");
        return stringToLocation(locationString);
    }

    public Location getBlueSpawn() {
        String locationString = plugin.getConfig().getString("locations.blue-spawn");
        return stringToLocation(locationString);
    }

    public void setLobby(Location location) {
        plugin.getConfig().set("locations.lobby", locationToString(location));
        plugin.saveConfig();
    }

    public void setRedSpawn(Location location) {
        plugin.getConfig().set("locations.red-spawn", locationToString(location));
        plugin.saveConfig();
    }

    public void setBlueSpawn(Location location) {
        plugin.getConfig().set("locations.blue-spawn", locationToString(location));
        plugin.saveConfig();
    }

    private String locationToString(Location location) {
        if (location == null) return null;
        return location.getWorld().getName() + "," +
                location.getX() + "," +
                location.getY() + "," +
                location.getZ() + "," +
                location.getYaw() + "," +
                location.getPitch();
    }

    private Location stringToLocation(String locationString) {
        if (locationString == null) return null;

        try {
            String[] parts = locationString.split(",");
            if (parts.length != 6) return null;

            World world = Bukkit.getWorld(parts[0]);
            if (world == null) return null;

            double x = Double.parseDouble(parts[1]);
            double y = Double.parseDouble(parts[2]);
            double z = Double.parseDouble(parts[3]);
            float yaw = Float.parseFloat(parts[4]);
            float pitch = Float.parseFloat(parts[5]);

            return new Location(world, x, y, z, yaw, pitch);
        } catch (Exception e) {
            return null;
        }
    }
}