package com.example.fullmod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.BlockPos;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.input.Keyboard;

@Mod(modid = "fullmod", name = "Move Mod", version = "1.0")
public class FullTestMod {

    private final Minecraft mc = Minecraft.getMinecraft();

    private boolean running = false;
    private int tickCounter = 0;
    private int phase = 0;
    private boolean lastRKeyState = false;
    private int counter = 0;

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;

        // 检测 O 键是否从未按下 -> 按下一瞬间
        boolean currentRKey = Keyboard.isKeyDown(Keyboard.KEY_O);
        if (currentRKey && !lastRKeyState) {
            running = !running;
            resetKeys();
            tickCounter = 0;
            phase = 0;
            counter = 9;
            mc.thePlayer.addChatMessage(new ChatComponentText(
                    "§e[FullMod] 自动动作已 " + (running ? "§a开启" : "§c关闭")
            ));
        }
        lastRKeyState = currentRKey;

        if (!running) return;

        tickCounter++;

        // 获取方块坐标，防止空指针
        if (mc.objectMouseOver == null || mc.objectMouseOver.getBlockPos() == null) return;
        BlockPos pos = mc.objectMouseOver.getBlockPos();

        switch (phase) {
            case 0:
                if (tickCounter == 1 && counter == 9) mc.thePlayer.sendChatMessage("/warp garden");
                if (tickCounter >= 30) {
                    phase++;
                    tickCounter = 0;
                    counter--;
                    if (counter == 0) counter = 9;
                }
                break;

            case 1: // 向左走 10 秒
                if (tickCounter == 1) press(mc.gameSettings.keyBindLeft);
                clickBlock(pos);
                if (tickCounter >= 757) {
                    release(mc.gameSettings.keyBindLeft);
                    phase++;
                    tickCounter = 0;
                }
                break;

            case 2: // 向前走 2 秒
                if (tickCounter == 1) press(mc.gameSettings.keyBindForward);
                clickBlock(pos);
                if (tickCounter >= 20) {
                    release(mc.gameSettings.keyBindForward);
                    phase++;
                    tickCounter = 0;
                }
                break;

            case 3: // 向右走 10 秒
                if (tickCounter == 1) press(mc.gameSettings.keyBindRight);
                clickBlock(pos);
                if (tickCounter >= 757) {
                    release(mc.gameSettings.keyBindRight);
                    phase++;
                    tickCounter = 0;
                }
                break;

            case 4: // 向前走 2 秒，之后回到 phase 0
                if (tickCounter == 1) press(mc.gameSettings.keyBindForward);
                clickBlock(pos);
                if (tickCounter >= 20) {
                    release(mc.gameSettings.keyBindForward);
                    phase = 0;
                    tickCounter = 0;
                }
                break;
        }
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
    }
}
