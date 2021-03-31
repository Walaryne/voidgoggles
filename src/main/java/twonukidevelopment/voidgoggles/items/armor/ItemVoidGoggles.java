package twonukidevelopment.voidgoggles.items.armor;

import baubles.api.BaubleType;
import baubles.api.BaublesApi;
import baubles.api.IBauble;
import baubles.api.render.IRenderBauble;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;
import thaumcraft.api.ThaumcraftMaterials;
import thaumcraft.api.capabilities.ThaumcraftCapabilities;
import thaumcraft.api.items.IGoggles;
import thaumcraft.api.items.IRevealer;
import thaumcraft.api.items.IVisDiscountGear;
import thaumcraft.api.items.ItemsTC;
import thaumcraft.client.lib.UtilsFX;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.entities.EntityFluxRift;
import thaumcraft.common.items.IThaumcraftItems;
import thaumcraft.common.items.baubles.ItemVoidseerCharm;
import thaumcraft.common.lib.network.PacketHandler;
import thaumcraft.common.lib.network.misc.PacketAuraToClient;
import thaumcraft.common.lib.research.ResearchManager;
import thaumcraft.common.lib.utils.EntityUtils;
import thaumcraft.common.world.aura.AuraChunk;
import thaumcraft.common.world.aura.AuraHandler;

import java.lang.ref.WeakReference;
import java.util.List;

public class ItemVoidGoggles extends ItemArmor implements IVisDiscountGear, IRevealer, IThaumcraftItems, IGoggles, IBauble, IRenderBauble {

    ResourceLocation tex = new ResourceLocation("voidgoggles", "textures/items/voidgoggles_bauble.png");

    public static WeakReference<EntityFluxRift> RIFT_TARGET = null;

    @SideOnly(Side.CLIENT)
    public static EntityFluxRift getRiftTarget() {
        return RIFT_TARGET != null ? RIFT_TARGET.get() : null;
    }

    public ItemVoidGoggles() {
        super(ThaumcraftMaterials.ARMORMAT_SPECIAL, 4, EntityEquipmentSlot.HEAD);
        this.setMaxDamage(350);
        this.setRegistryName("voidgoggles");
        this.setTranslationKey("voidgoggles");
        ConfigItems.ITEM_VARIANT_HOLDERS.add(this);
    }

    @Override
    public boolean showIngamePopups(ItemStack itemStack, EntityLivingBase entityLivingBase) {
        return true;
    }

    @Override
    public BaubleType getBaubleType(ItemStack itemStack) {
        return BaubleType.HEAD;
    }

    @Override
    public boolean canEquip(ItemStack itemstack, EntityLivingBase player) {
        return true;
    }

    @Override
    public boolean canUnequip(ItemStack itemstack, EntityLivingBase player) {
        return true;
    }

    @Override
    public boolean willAutoSync(ItemStack itemstack, EntityLivingBase player) {
        return false;
    }

    public ModelResourceLocation getCustomModelResourceLocation(String variant) {
        return new ModelResourceLocation("voidgoggles:" + variant);
    }

    public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type) {
        return "voidgoggles:textures/entity/armor/voidgoggles.png";
    }

    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        tooltip.add(TextFormatting.DARK_BLUE + "" + TextFormatting.ITALIC + I18n.translateToLocal("item.voidseergoggles.text"));
        super.addInformation(stack, worldIn, tooltip, flagIn);
    }

    @Override
    public Item getItem() {
        return this;
    }

    @Override
    public String[] getVariantNames() {
        return new String[]{ "normal" };
    }

    @Override
    public int[] getVariantMeta() {
        return new int[]{0};
    }

    @SideOnly(Side.CLIENT)
    public ItemMeshDefinition getCustomMesh() {
        return null;
    }

    public EnumRarity getRarity(ItemStack itemstack) {
        return EnumRarity.EPIC;
    }

    public void onUpdate(ItemStack stack, World world, Entity entity, int p_77663_4_, boolean p_77663_5_) {
        super.onUpdate(stack, world, entity, p_77663_4_, p_77663_5_);
        if (!world.isRemote && stack.isItemDamaged() && entity.ticksExisted % 20 == 0 && entity instanceof EntityLivingBase) {
            stack.damageItem(-1, (EntityLivingBase)entity);
        }

        if (this.isWorn(entity) && !world.isRemote && entity.ticksExisted % 20 == 0 && entity instanceof EntityPlayerMP) {
            this.updateAura(world, (EntityPlayerMP)entity);
        }
        if (entity.world.isRemote && entity instanceof EntityPlayerSP) {
            updateRiftTarget(entity);
        }
    }

    public void onWornTick(ItemStack itemstack, EntityLivingBase player) {
        if(player != null) {
            if (!player.world.isRemote && player.ticksExisted % 20 == 0 && player instanceof EntityPlayerMP) {
                this.updateAura(player.world, (EntityPlayerMP) player);
            }
            if (player.world.isRemote && player instanceof EntityPlayerSP) {
                updateRiftTarget(player);
            }
        }
    }

    public void onArmorTick(World world, EntityPlayer player, ItemStack armor) {
        super.onArmorTick(world, player, armor);
        if (!world.isRemote && armor.getItemDamage() > 0 && player.ticksExisted % 20 == 0) {
            armor.damageItem(-1, player);
        }

    }

    public boolean getIsRepairable(ItemStack stack1, ItemStack stack2) {
        return stack2.isItemEqual(new ItemStack(ItemsTC.ingots, 1, 1)) || super.getIsRepairable(stack1, stack2);
    }

    public boolean isWorn(Entity player) {
        if(player instanceof EntityPlayerMP) {
            IItemHandler baubles = BaublesApi.getBaublesHandler((EntityPlayerMP) player);
            ItemStack headSlot = baubles.getStackInSlot(4);
            return (((EntityPlayerMP)player).getItemStackFromSlot(EntityEquipmentSlot.HEAD).getItem() instanceof ItemVoidGoggles || headSlot.getItem() instanceof ItemVoidGoggles);
        }
        return false;
    }

    private void updateAura(World world, EntityPlayerMP player) {
        AuraChunk ac = AuraHandler.getAuraChunk(world.provider.getDimension(), player.getPosition().getX() >> 4, player.getPosition().getZ() >> 4);
        if (ac != null) {
            if ((ac.getFlux() > ac.getVis() || ac.getFlux() > (float)(ac.getBase() / 3)) && !ThaumcraftCapabilities.knowsResearch(player, new String[]{"FLUX"})) {
                ResearchManager.startResearchWithPopup(player, "FLUX");
                player.sendStatusMessage(new TextComponentString(TextFormatting.DARK_PURPLE + I18n.translateToLocal("research.FLUX.warn")), true);
            }

            PacketHandler.INSTANCE.sendTo(new PacketAuraToClient(ac), player);
        }
    }

    @SideOnly(Side.CLIENT)
    private void updateRiftTarget(Entity entity) {
        Entity target = EntityUtils.getPointedEntity(entity.world, entity, 1.0, 16.0, 5.0f, true);
        if(target instanceof EntityFluxRift) {
            if(getRiftTarget() != target) {
                RIFT_TARGET = new WeakReference<>((EntityFluxRift) target);
            }
        } else if(getRiftTarget() != null) {
            RIFT_TARGET = null;
        }
    }

    @Override
    public void onPlayerBaubleRender(ItemStack itemStack, EntityPlayer entityPlayer, RenderType renderType, float v) {
        if (renderType == RenderType.HEAD) {
            boolean armor = entityPlayer.getItemStackFromSlot(EntityEquipmentSlot.HEAD) != null;
            Minecraft.getMinecraft().renderEngine.bindTexture(this.tex);
            Helper.translateToHeadLevel(entityPlayer);
            Helper.translateToFace();
            Helper.defaultTransforms();
            GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
            GlStateManager.translate(-0.5D, -0.5D, armor ? 0.11999999731779099D : 0.0D);
            UtilsFX.renderTextureIn3D(0.0F, 0.0F, 1.0F, 1.0F, 16, 26, 0.1F);
        }
    }

    @Override
    public boolean showNodes(ItemStack itemStack, EntityLivingBase entityLivingBase) {
        return false;
    }

    @Override
    public int getVisDiscount(ItemStack itemStack, EntityPlayer entityPlayer) {
        return ((ItemVoidseerCharm)ItemsTC.charmVoidseer).getVisDiscount(itemStack, entityPlayer);
    }
}
