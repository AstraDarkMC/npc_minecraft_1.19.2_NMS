package net.matcraft.thecraftingdead.npc.commands;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.datafixers.util.Pair;
import net.matcraft.thecraftingdead.TheCraftingDead;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.EquipmentSlot;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_19_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.UUID;

public class NPCCommand implements CommandExecutor {

    TheCraftingDead main = TheCraftingDead.getInstance();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if(sender instanceof Player){
            Player player = (Player) sender;
            CraftPlayer craftPlayer = (CraftPlayer) player;
            ServerPlayer serverPlayer = craftPlayer.getHandle();

            //create a new game profile with a random uuid and a name NOTE: (https://mineskin.org/) for skin
            GameProfile profile = new GameProfile(UUID.randomUUID(), "SurvivalNPC");
            profile.getProperties().put("textures",new Property("textures",
                    "ewogICJ0aW1lc3RhbXAiIDogMTY3MDk2MjU3ODk4OSwKICAicHJvZmlsZUlkIiA6ICJmMTA0NzMxZjljYTU0NmI0OTkzNjM4NTlkZWY5N2NjNiIsCiAgInByb2ZpbGVOYW1lIiA6ICJ6aWFkODciLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTUyZWU2MTI5OTExNmFjMzdiNjM2N2Q3MmNiN2VkY2NmNDBiNDBlNzA3ZjRlZTU4YTA4MDk4MWI5YzIwN2E0NCIKICAgIH0KICB9Cn0=",
                    "QpPE1uNlJW022O9zR75kgkLOp1cHvzcokzmuSp+eBI2NZ6pyN5pCgyhyXy495kgOEasAxHhC96oAmS0/+xdJEgR04WA7InwHLz0THxCf77qqIkoF29XU/gHmFJZWCqYoS72TjyMeSbngND5L24ilP61o/sda1em6H+zr8Mq/4LjaLNmbCKki6BWY2VUq8DgWNagyZYzEW/jHg2VsNL0axAski9PmzJoHqg7qqlmB54cyUpkVC6+F+zM9CXuPteYfE3Sic8Hl1eFUfnEeNTPIWPYMEbp+XLGG5b4N3hyW8h1PDZ50+69tlXhNaCvRwmA90J8VQ6e+HWFDRxhTCeTskAD46bmD7OBxLDmn6AtBgDOVKVRCyXXxsHxyF5d+SGA+Wkdm5EUOpdL72YBJkavCIA5o1Vw0wuySVY5ewT7CCrqjepCQJFXDNvMKkx/6lnwyuCY41Dz+IB+6B6iw/djFzYl3E+m5bY0wUZ0+qo7fQtKimdVDRPbS+p24f+on0+UyCFtTfw21454+nemPLp3+dYK6L++p4Qm3NwNPvDF6M5biRGQPTuAC3nOLQPkYm7v8wZuBPxOuC5ZSUAtiqplGy3lVznfEX884Bb87CZ5QJxQisMhCoca/1hICDkb5uypVeYiFMlbuKpgPx9TgujsRDXYKYmazXCoT80x5XrnEY3s="
            ));


            ServerPlayer npc = new ServerPlayer(serverPlayer.getServer(), serverPlayer.getLevel(), profile, null);
            npc.setPos(490,150,-281);


            ServerGamePacketListenerImpl connection = serverPlayer.connection;
            connection.send(new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.ADD_PLAYER, npc));
            connection.send(new ClientboundAddPlayerPacket(npc));

            SynchedEntityData data = npc.getEntityData();
            byte bitmask = (byte) (0x01 | 0x04 | 0x08 | 0x10 | 0x20 | 0x40);
            data.set(new EntityDataAccessor<>(17, EntityDataSerializers.BYTE), bitmask);
            connection.send(new ClientboundSetEntityDataPacket(npc.getId(), data, true));

            float yaw = 1f;
            float pitch = 1f;
            connection.send(new ClientboundRotateHeadPacket(npc, (byte) ((yaw % 360) * 256 / 360)));
            connection.send(new ClientboundMoveEntityPacket.Rot(
                    npc.getBukkitEntity().getEntityId(),
                    (byte) ((yaw % 360) * 256 / 360),
                    (byte) ((pitch % 360) * 256 / 360),
                    true));

            //Add items in hand
            //connection.send(new ClientboundSetEquipmentPacket(npc.getBukkitEntity().getEntityId(), Arrays.asList(new Pair<>(EquipmentSlot.MAINHAND, CraftItemStack.asNMSCopy(new ItemStack(Material.DIAMOND_SWORD))))));

            Bukkit.getScheduler().runTaskLater(main, () -> {
                connection.send(new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.REMOVE_PLAYER, npc));
            }, 20);



        }

        return false;
    }
}
