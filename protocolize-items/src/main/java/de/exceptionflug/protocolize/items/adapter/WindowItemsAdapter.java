package de.exceptionflug.protocolize.items.adapter;

import de.exceptionflug.protocolize.api.event.PacketReceiveEvent;
import de.exceptionflug.protocolize.api.handler.PacketAdapter;
import de.exceptionflug.protocolize.api.protocol.Stream;
import de.exceptionflug.protocolize.items.InventoryManager;
import de.exceptionflug.protocolize.items.ItemStack;
import de.exceptionflug.protocolize.items.ItemsModule;
import de.exceptionflug.protocolize.items.PlayerInventory;
import de.exceptionflug.protocolize.items.packet.WindowItems;

public class WindowItemsAdapter extends PacketAdapter<WindowItems> {

  public WindowItemsAdapter() {
    super(Stream.DOWNSTREAM, WindowItems.class);
  }

  @Override
  public void receive(final PacketReceiveEvent<WindowItems> event) {
    final WindowItems packet = event.getPacket();
    if (packet.getWindowId() != 0)
      return;
    final PlayerInventory playerInventory = InventoryManager.getCombinedSendInventory(event.getPlayer().getUniqueId(), event.getServerInfo().getName());
    for (int i = 0; i < packet.getItems().size(); i++) {
      final ItemStack stack = packet.getItemStackAtSlot(i);
      if (stack == null || stack.getType() == ItemType.NO_DATA) {
        if (playerInventory.getItem(i) != null && !playerInventory.getItem(i).isHomebrew() && ItemsModule.isSpigotInventoryTracking()) {
          playerInventory.removeItem(i);
        }
      } else if (ItemsModule.isSpigotInventoryTracking()) {
        playerInventory.setItem(i, stack);
      }
      if (ItemsModule.isSpigotInventoryTracking()) {
        if (packet.setItemStackAtSlot(i, playerInventory.getItem(i))) {
          event.markForRewrite();
        }
      } else {
        final ItemStack toWrite = playerInventory.getItem(i);
        if (toWrite != null && toWrite.isHomebrew()) {
          if (packet.setItemStackAtSlot(i, toWrite)) {
            event.markForRewrite();
          }
        }
      }
    }
  }
}
