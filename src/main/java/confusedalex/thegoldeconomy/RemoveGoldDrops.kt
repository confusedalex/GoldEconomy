package confusedalex.thegoldeconomy;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

public class RemoveGoldDrops implements Listener {

  private boolean shouldRemove(Material material) {
    switch (material) {
      case GOLD_BLOCK:
      case GOLD_INGOT:
      case GOLD_NUGGET:
      case GOLDEN_BOOTS:
      case GOLDEN_LEGGINGS:
      case GOLDEN_CHESTPLATE:
      case GOLDEN_HELMET:
      case GOLDEN_AXE:
      case GOLDEN_PICKAXE:
      case GOLDEN_SHOVEL:
      case GOLDEN_HOE:
      case GOLDEN_SWORD:
        return true;
      default:
        return false;
    }
  }

  @EventHandler
  public void entityDeathEvent(EntityDeathEvent e) {
    if (e.getEntityType().equals(EntityType.PLAYER)) return;

    e.getDrops().removeIf(item -> shouldRemove(item.getType()));
  }
}
