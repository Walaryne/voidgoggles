package twonukidevelopment.voidgoggles.client.events;

import baubles.api.BaublesApi;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.items.IItemHandler;
import org.lwjgl.opengl.GL11;
import thaumcraft.client.lib.events.HudHandler;
import thaumcraft.codechicken.lib.colour.Colour;
import thaumcraft.codechicken.lib.colour.ColourARGB;
import thaumcraft.common.entities.EntityFluxRift;
import twonukidevelopment.voidgoggles.items.armor.ItemVoidGoggles;

import java.lang.reflect.InvocationTargetException;
import java.text.DecimalFormat;

@Mod.EventBusSubscriber(value = Side.CLIENT)
public class RenderHudEvent {

    private static final DecimalFormat secondsFormatter = new DecimalFormat("#######.#");
    private static final Colour riftInfoColour = new ColourARGB(255, 201, 255, 229);
    private static final Colour collapseColour = new ColourARGB(255, 179, 0, 40);
    private static final double scale = 0.8D;


    @SubscribeEvent
    public static void renderTick(TickEvent.RenderTickEvent event) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        String msg;

        if(event.phase == TickEvent.Phase.END) {
            Minecraft mc = FMLClientHandler.instance().getClient();
            if(Minecraft.getMinecraft().getRenderViewEntity() instanceof EntityPlayer) {
                EntityPlayer player = (EntityPlayer) Minecraft.getMinecraft().getRenderViewEntity();

                if(player != null) {
                    IItemHandler baubles = BaublesApi.getBaublesHandler(player);
                    ItemStack headSlot = baubles.getStackInSlot(4);
                    if(player.getItemStackFromSlot(EntityEquipmentSlot.HEAD).getItem() instanceof ItemVoidGoggles ||
                    headSlot.getItem() instanceof ItemVoidGoggles) {
                        GL11.glPushMatrix();
                        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
                        GL11.glClear(256);
                        GL11.glMatrixMode(5889);
                        GL11.glLoadIdentity();
                        GL11.glOrtho(0.0, sr.getScaledWidth_double(), sr.getScaledHeight_double(), 0.0, 1000.0, 3000.0);
                        GL11.glMatrixMode(5888);
                        GL11.glLoadIdentity();
                        GL11.glTranslatef(0.0f, 80.0f, -2000.0f);
                        GL11.glEnable(3042);
                        GL11.glBlendFunc(770, 771);
                        if(mc.inGameHasFocus && Minecraft.isGuiEnabled()) {
                            GL11.glPushMatrix();
                            GL11.glTranslated(8.0D, 0.0D, 0.0D);
                            GL11.glScaled(scale, scale, scale);
                            msg = secondsFormatter.format(HudHandler.currentAura.getFlux());
                            mc.ingameGUI.drawString(mc.fontRenderer, msg, 0, 0, 11145659);
                            GL11.glPopMatrix();
                            GL11.glPushMatrix();
                            GL11.glTranslated(8.0D, 8.0D, 0.0D);
                            GL11.glScaled(scale, scale, scale);
                            msg = secondsFormatter.format(HudHandler.currentAura.getVis());
                            mc.ingameGUI.drawString(mc.fontRenderer, msg, 0, 0, 15641343);
                            GL11.glPopMatrix();
                            EntityFluxRift rift = ItemVoidGoggles.getRiftTarget();
                            if(rift != null) {
                                GL11.glPushMatrix();
                                GL11.glTranslated(8.0D, 20.0D, 0.0D);
                                GL11.glScaled(scale, scale, scale);
                                msg = "Rift Status:";
                                mc.ingameGUI.drawString(mc.fontRenderer, msg, 0, 0, riftInfoColour.argb());
                                GL11.glPopMatrix();
                                GL11.glPushMatrix();
                                GL11.glTranslated(12.0D, 28.0D, 0.0D);
                                GL11.glScaled(scale, scale, scale);
                                msg = "Size: " + rift.getRiftSize();
                                mc.ingameGUI.drawString(mc.fontRenderer, msg, 0, 0, riftInfoColour.argb());
                                GL11.glPopMatrix();
                                GL11.glPushMatrix();
                                GL11.glTranslated(12.0D, 36.0D, 0.0D);
                                GL11.glScaled(scale, scale, scale);
                                msg = "Stability: " + I18n.format("riftstability." + rift.getStability());
                                mc.ingameGUI.drawString(mc.fontRenderer, msg, 0, 0, riftInfoColour.argb());
                                GL11.glPopMatrix();
                                if(rift.getCollapse()) {
                                    GL11.glPushMatrix();
                                    GL11.glTranslated(12.0D, 44.0D, 0.0D);
                                    GL11.glScaled(scale, scale, scale);
                                    msg = "COLLAPSE IMMINENT";
                                    mc.ingameGUI.drawString(mc.fontRenderer, msg, 0, 0, collapseColour.argb());
                                    GL11.glPopMatrix();
                                }
                            }
                        }

                        GL11.glDisable(3042);
                        GL11.glPopMatrix();

                    }
                }
            }
        }
    }
}
