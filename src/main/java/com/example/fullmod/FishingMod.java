package com.example.fullmod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.BlockPos;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.input.Keyboard;

@Mod(modid = "Fishingmod_v1", name = "Move Mod", version = "1.0")
public class FishingMod {

    private final Minecraft mc = Minecraft.getMinecraft();
    private float startYaw, startPitch;
    private float targetYaw, targetPitch;
    private int smoothTicks = 0;
    private int maxSmoothTicks = 0;
    private boolean isSmoothLooking = false;
    public static FishingMod instance;
    private boolean running = false;
    private int tickCounter = 0;
    private boolean lastRKeyState = false;
    private FishingState state = FishingState.IDLE;
    private int stateTick = 0;
    final private AutoFishingController autoFishingController = new AutoFishingController();
    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
        ClientCommandHandler.instance.registerCommand(new CommandSmoothLook());
    }
    public FishingMod(){instance = this;}
    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        handleSmoothLook2();
        if (mc.thePlayer == null || mc.theWorld == null) return;
        if (event.phase == TickEvent.Phase.END) {
            autoFishingController.onTick();
        }
        // 检测 O 键是否从未按下 -> 按下一瞬间
        boolean currentRKey = Keyboard.isKeyDown(Keyboard.KEY_SEMICOLON);
        if (currentRKey && !lastRKeyState) {
            running = !running;
            resetKeys();
            tickCounter = 0;
            if(running) {
                autoFishingController.start();
            }
            else autoFishingController.stop();
            mc.thePlayer.addChatMessage(new ChatComponentText(
                    "§e[FullMod] 自动动作已 " + (running ? "§a开启" : "§c关闭")
            ));
        }
        lastRKeyState = currentRKey;

        if (!running) return;

        tickCounter++;

        // 防止空指针
//        if (mc.objectMouseOver == null || mc.objectMouseOver.getBlockPos() == null) return;
//        BlockPos pos = mc.objectMouseOver.getBlockPos();
    }
    private void clickBlock(BlockPos pos) {
        if (mc.objectMouseOver == null) return;
        mc.playerController.onPlayerDamageBlock(pos, mc.objectMouseOver.sideHit);
        mc.thePlayer.swingItem(); // 1.8.9 使用 swingItem()
    }

    private void press(KeyBinding key) {
        KeyBinding.setKeyBindState(key.getKeyCode(), true);
    }

    private void release(KeyBinding key) {
        KeyBinding.setKeyBindState(key.getKeyCode(), false);
    }

    private void resetKeys() {
        release(mc.gameSettings.keyBindLeft);
        release(mc.gameSettings.keyBindRight);
        release(mc.gameSettings.keyBindForward);
        release(mc.gameSettings.keyBindSneak);
        release(mc.gameSettings.keyBindAttack);
    }
    public float wrapAngleTo180_float(float angle) {
        angle %= 360.0F;
        if (angle >= 180.0F) angle -= 360.0F;
        if (angle < -180.0F) angle += 360.0F;
        return angle;
    }
    public void smoothLook(float yaw, float pitch, float durationSeconds) {
        this.startYaw = mc.thePlayer.rotationYaw;
        this.startPitch = mc.thePlayer.rotationPitch;

        this.targetYaw = yaw;
        this.targetPitch = pitch;

        this.maxSmoothTicks = (int)(durationSeconds * 20); // 秒 → tick
        this.smoothTicks = 0;

        this.isSmoothLooking = true;
    }
    private void handleSmoothLook2(){
        if (this.isSmoothLooking) {
            if (smoothTicks >= maxSmoothTicks) {
                mc.thePlayer.rotationYaw = targetYaw;
                mc.thePlayer.rotationPitch = targetPitch;
                this.isSmoothLooking = false;
            } else {
                float t = (float)smoothTicks / maxSmoothTicks; // 0~1
                float k = easeInOut(t); // 使用缓动

                float diffYaw = wrapAngleTo180_float(targetYaw - startYaw);
                float newYaw = startYaw + diffYaw * k;
                float newPitch = startPitch + (targetPitch - startPitch) * k;

                mc.thePlayer.rotationYaw = newYaw;
                mc.thePlayer.prevRotationYaw = newYaw;

                mc.thePlayer.rotationPitch = newPitch;
                mc.thePlayer.prevRotationPitch = newPitch;

                smoothTicks++;
            }
        }
    }
    private float easeInOut(float t) {
        return (float)(t * t * (3 - 2 * t));
    }
}
