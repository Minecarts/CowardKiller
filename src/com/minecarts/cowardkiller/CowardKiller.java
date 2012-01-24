package com.minecarts.cowardkiller;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.text.MessageFormat;

import org.bukkit.configuration.file.FileConfiguration;

import org.bukkit.event.Event.Type;
import org.bukkit.event.Event.Priority;

import org.bukkit.event.entity.EntityListener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerQuitEvent;


import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.Command;

import java.util.WeakHashMap;
import java.util.Date;

import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.Projectile;


public class CowardKiller extends org.bukkit.plugin.java.JavaPlugin {
    private static final Logger logger = Logger.getLogger("com.minecarts.cowardkiller"); 
    
    protected FileConfiguration config;
    protected boolean debug;
    protected int combatWindow;
    
    protected WeakHashMap<Player, DatedEvent> lastDamageEvents = new WeakHashMap<Player, DatedEvent>();
    
    
    public void onEnable() {
        reloadConfig();
        
        // reload config command
        getCommand("cowardkiller").setExecutor(new CommandExecutor() {
            public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
                if(!sender.isOp()) return true; // "hide" command output for non-ops
                
                if(args[0].equalsIgnoreCase("reload")) {
                    CowardKiller.this.reloadConfig();
                    log("Config reloaded by {0}", sender.getName());
                    sender.sendMessage("CowardKiller config reloaded.");
                    return true;
                }
                
                return false;
            }
        });
        
        
        // record last PVP damage event
        getServer().getPluginManager().registerEvent(Type.ENTITY_DAMAGE, new EntityListener() {
            @Override
            public void onEntityDamage(EntityDamageEvent event) {
                if(event.isCancelled()) return;
                
                if(event instanceof EntityDamageByEntityEvent) {
                    Player defender = getParentPlayer(event.getEntity());
                    Player attacker = getParentPlayer(((EntityDamageByEntityEvent) event).getDamager());
                    
                    if(defender != null && attacker != null && !attacker.equals(defender)) {
                        debug("{0} attacked {1}, storing current date for each", attacker.getName(), defender.getName());
                        
                        DatedEvent now = new DatedEvent(event);
                        lastDamageEvents.put(attacker, now);
                        lastDamageEvents.put(defender, now);
                    }
                }
            }
        }, Priority.Monitor, this);
        
        // penalize combat logging
        getServer().getPluginManager().registerEvent(Type.PLAYER_QUIT, new PlayerListener() {
            @Override
            public void onPlayerQuit(PlayerQuitEvent event) {
                Player player = event.getPlayer();
                DatedEvent lastDamage = lastDamageEvents.get(player);
                
                if(lastDamage != null) {
                    float secondsSinceLastDamage = lastDamage.elapsed() / 1000;
                    
                    if(combatWindow > secondsSinceLastDamage) {
                        int damage = (int) Math.round(player.getMaxHealth() * (1 - (secondsSinceLastDamage / combatWindow)));
                        
                        log("Penalizing {0} with {1} damage for logging out while in PVP combat", player.getName(), damage);
                        
                        if(lastDamage.getEvent() instanceof EntityDamageByEntityEvent) {
                            EntityDamageByEntityEvent damageEvent = (EntityDamageByEntityEvent) lastDamage.getEvent();
                            log("Last EntityDamageByEntityEvent: {0} inflicted {1} damage on {2}", damageEvent.getDamager(), damageEvent.getDamage(), damageEvent.getEntity());
                        }
                        
                        PlayerCombatPenaltyEvent penaltyEvent = new PlayerCombatPenaltyEvent(player, damage, event);
                        getServer().getPluginManager().callEvent(penaltyEvent);
                        
                        if(!penaltyEvent.isCancelled()) {
                            player.damage(penaltyEvent.getDamage());
                        }
                    }
                }
            }
        }, Priority.Monitor, this);
        
        
        log("Version {0} enabled.", getDescription().getVersion());
    }
    
    public void onDisable() {
    }
    
    @Override
    public void reloadConfig() {
        super.reloadConfig();
        
        config = getConfig();
        
        debug = config.getBoolean("debug");
        combatWindow = config.getInt("combatWindow");
    }
    
    public void log(String message) {
        log(Level.INFO, message);
    }
    public void log(Level level, String message) {
        logger.log(level, MessageFormat.format("{0}> {1}", getDescription().getName(), message));
    }
    public void log(String message, Object... args) {
        log(MessageFormat.format(message, args));
    }
    public void log(Level level, String message, Object... args) {
        log(level, MessageFormat.format(message, args));
    }
    
    public void debug(String message) {
        if(debug) log(message);
    }
    public void debug(String message, Object... args) {
        if(debug) log(message, args);
    }
    
    
    public Player getParentPlayer(Object entity) {
        if(entity instanceof Player) {
            return (Player) entity;
        }
        
        if(entity instanceof Tameable) {
            return getParentPlayer(((Tameable) entity).getOwner());
        }
        
        if(entity instanceof Projectile) {
            return getParentPlayer(((Projectile) entity).getShooter());
        }
        
        return null;
    }
    
    
}