package your.mod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

@Mod(modid = "autoplay", version = "1.0")
public class AutoPlay {

    private boolean autoRunning = false; // Auto run switch
    private KeyBinding toggleKey;

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);

        // Register key binding (1.8.9 compatible)
        toggleKey = new KeyBinding("Toggle AutoPlay", Keyboard.KEY_G, "AutoPlay");
        ClientRegistry.registerKeyBinding(toggleKey);
    }
    private void smoothLook(float targetYaw, float targetPitch, float speed,Minecraft mc) {
        float yaw = mc.thePlayer.rotationYaw;
        float pitch = mc.thePlayer.rotationPitch;

        // 差值
        float dYaw = targetYaw - yaw;
        float dPitch = targetPitch - pitch;

        // 限制每 tick 最大转动角度（speed）
        if (Math.abs(dYaw) > speed) dYaw = Math.signum(dYaw) * speed;
        if (Math.abs(dPitch) > speed) dPitch = Math.signum(dPitch) * speed;

        mc.thePlayer.rotationYaw = yaw + dYaw;
        mc.thePlayer.rotationPitch = pitch + dPitch;
    }
    private void lookAt(BlockPos pos, float speed, Minecraft mc) {
        double dx = pos.getX() + 0.5 - mc.thePlayer.posX;
        double dy = pos.getY() + 0.5 - (mc.thePlayer.posY + mc.thePlayer.getEyeHeight());
        double dz = pos.getZ() + 0.5 - mc.thePlayer.posZ;

        double dist = Math.sqrt(dx*dx + dz*dz);
        float targetYaw = (float)(Math.toDegrees(Math.atan2(dz, dx)) - 90);
        float targetPitch = (float)(-Math.toDegrees(Math.atan2(dy, dist)));

        smoothLook(targetYaw, targetPitch, speed,mc);
    }


    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayerSP player = mc.thePlayer;

        if (player == null) return;

        // Toggle auto-running when key pressed
        if (toggleKey.isPressed()) {
            autoRunning = !autoRunning;
            System.out.println("AutoPlay: " + (autoRunning ? "ON" : "OFF"));
        }

        if (!autoRunning) return;

        // === Automatic movement (forward) ===
        BlockPos pos1 = new BlockPos(1,2f,3);
        lookAt(pos1,2,mc);
    }
}
