package com.example.fullmod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.BlockPos;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.input.Keyboard;

@Mod(modid = "Fishingmod_v1", name = "Move Mod", version = "1.0")
public class FullTestMod {

    private final Minecraft mc = Minecraft.getMinecraft();

    private boolean running = false;
    private int tickCounter = 0;
    private boolean lastRKeyState = false;
    private FishingState state = FishingState.IDLE;
    private int stateTick = 0;
    final private AutoFishingController autoFishingController = new AutoFishingController();
    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
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

}
