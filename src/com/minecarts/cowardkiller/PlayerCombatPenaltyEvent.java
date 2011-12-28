package com.minecarts.cowardkiller;

import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.entity.Player;

public class PlayerCombatPenaltyEvent extends EntityDamageEvent {
    private final PlayerQuitEvent quit;

    public PlayerCombatPenaltyEvent(Player player, int damage, PlayerQuitEvent quit) {
        super(player, DamageCause.CUSTOM, damage);
        this.quit = quit;
    }

    public PlayerQuitEvent getQuitEvent() {
        return this.quit;
    }
}
