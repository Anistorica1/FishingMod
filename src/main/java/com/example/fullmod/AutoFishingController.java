package com.example.fullmod;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.util.ChatComponentText;
import java.util.Random;

import java.lang.reflect.Field;

public class AutoFishingController {
    private static Field ticksCatchableField;

    static {
        try {
            ticksCatchableField = EntityFishHook.class.getDeclaredField("ticksCatchable");
            ticksCatchableField.setAccessible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private final Minecraft mc = Minecraft.getMinecraft();
    private static final Random RANDOM = new Random();
    private FishingState state = FishingState.IDLE;
    private int stateTick = 0;   // 进入当前状态后经过的 tick 数
    public void start() {
        state = FishingState.CASTING;
        stateTick = 0;
        mc.thePlayer.addChatMessage(new ChatComponentText(
                "start"
        ));
    }
    public void stop() {
        state = FishingState.IDLE;
        stateTick = 0;
        mc.thePlayer.addChatMessage(new ChatComponentText(
                "stop"
        ));
    }
    public void onTick() {
        if (state == FishingState.IDLE) return;

        stateTick++;

        switch (state) {
            case CASTING:
                handleCasting();
                break;
            case WAITING:
                handleWaiting();
                break;
            case BITE:
                handleBite();
                break;
            case REELING:
                handleReeling();
                break;
            case COOLDOWN:
                handleCooldown();
                break;
            case HYPERION:
                handleHyperion();
                break;
        }
    }
    private void changeState(FishingState newState) {
        state = newState;
        stateTick = 0;
    }
    private void handleCasting() {
        if (stateTick == 1) {
            rightClick();
        }

        // 给服务器 10 tick 反应
        if (stateTick > 50) {
            changeState(FishingState.WAITING);
        }
    }
    private void handleWaiting() {
        EntityFishHook hook = mc.thePlayer.fishEntity;

        if (hook == null) {
            changeState(FishingState.CASTING);
            return;
        }
        if(stateTick % 20 == 0)
            FullTestMod.instance.smoothLook(-0.5f+RANDOM.nextFloat(),29.5f+RANDOM.nextFloat(),0.2f);
        // 鱼上钩：鱼钩被猛地下拉
        if (hook.motionY < -0.04) {
            changeState(FishingState.HYPERION);
        }
    }
    private void handleBite() {
        rightClick();
        changeState(FishingState.COOLDOWN);
    }
    private void handleCooldown() {
        if (stateTick > 20) { // 1 秒
            changeState(FishingState.CASTING);
        }
    }
    private void rightClick() {
        mc.playerController.sendUseItem(
                mc.thePlayer,
                mc.theWorld,
                mc.thePlayer.getHeldItem()
        );
    }
    private void handleReeling() {
        EntityFishHook hook = mc.thePlayer.fishEntity;
        if (hook == null) {
            // 钩子丢失，重投
            changeState(FishingState.CASTING);
            return;
        }

        if (stateTick == 1) {
            // 触发拉线动作
            rightClick();
        }

        // 等待 2 tick 确保收线完成
        if (stateTick > 2) {
            changeState(FishingState.COOLDOWN);
        }
    }
    // 切换到指定的快捷栏槽位（0~8）
    private void switchHotbarSlot(int slot) {
        if (slot < 0 || slot > 8) return;
        mc.thePlayer.inventory.currentItem = slot;
    }

    private void handleHyperion() {
        if (stateTick == 1) {
            rightClick();
            switchHotbarSlot(1);
            FullTestMod.instance.smoothLook(0.0f,90.0f,0.5f);
        }
        if (stateTick == 22){
            rightClick();
            switchHotbarSlot(0);
            FullTestMod.instance.smoothLook(0.0f,30.0f,0.5f);
            changeState(FishingState.COOLDOWN);
        }

    }
}
