package co.uk.silvania.flenixskills.skills;

import co.uk.silvania.flenixskills.FlenixSkills;
import co.uk.silvania.rpgcore.skills.SkillLevelBase;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.IExtendedEntityProperties;
import net.minecraftforge.event.entity.EntityEvent.EntityConstructing;
import net.minecraftforge.event.entity.living.LivingFallEvent;

public class SkillAerobatics extends SkillLevelBase implements IExtendedEntityProperties {
	
	public static String staticSkillId;
	public boolean addedXp = false;
	public float prevTickFall = 0;
	public float prevHealth = 0;
	
	public SkillAerobatics(EntityPlayer player, String skillID) {
		super(skillID);
		staticSkillId = skillID;
		this.xp = 0;
		this.prevTickFall = 0;
		this.prevHealth = 0;
		this.addedXp = false;
	}

	public static final void register(EntityPlayer player) {
		player.registerExtendedProperties(SkillAerobatics.staticSkillId, new SkillAerobatics(player, staticSkillId));
	}

	@Override
	public void saveNBTData(NBTTagCompound compound) {
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setFloat(skillId + "xp", xp);
		nbt.setFloat(skillId + "prevFall", prevTickFall);
		nbt.setFloat(skillId + "prevHealth", prevHealth);
		nbt.setBoolean(skillId + "addedXp", addedXp);
		compound.setTag(skillId, nbt);
	}

	@Override
	public void loadNBTData(NBTTagCompound compound) {
		NBTTagCompound nbt = (NBTTagCompound) compound.getTag(skillId);
		xp = nbt.getFloat(skillId + "xp");
	}

	@Override public void init(Entity entity, World world) {}
	
	@SubscribeEvent
	public void onEntityConstructing(EntityConstructing event) {
		if (event.entity instanceof EntityPlayer) {
			event.entity.registerExtendedProperties(skillId, new SkillAerobatics((EntityPlayer)event.entity, skillId));
		}
	}
	
	//How far can they fall without taking damage (Increased by levelling)
	public int safeFallDistance(EntityPlayer player) {
		SkillAerobatics skill = (SkillAerobatics) SkillAerobatics.get(player, skillId);
		int lvl = skill.getLevel();
		int fall = 3;
		while (lvl > 30) {
			lvl -= 30;
			fall++;
		}
		if (fall > 15) {
			fall = 15;
		}
		return fall;
	}
	
	@Override
	public void levelUp(EntityPlayer player, float xpAdd) {
		super.levelUp(player, xpAdd);
		int level = getLevelFromXP(getXP() + xpAdd);
		if (!player.worldObj.isRemote && level % 30 == 0) {
			player.addChatComponentMessage(new ChatComponentText(nameFormat() + "You can now safely fall " + (3+(level/30)) + " blocks without damage."));
		}
	}
	
	@SubscribeEvent
	public void onPlayerTick(TickEvent.PlayerTickEvent event) {
		if (event.phase == TickEvent.Phase.START && event.player.ridingEntity == null && event.side == Side.SERVER) {
			EntityPlayer player = event.player;
			
			SkillAerobatics skill = (SkillAerobatics) SkillAerobatics.get(player, skillId);
			
			if (!player.onGround) {
				prtln(player.getDisplayName() + " is in the air! Falling: " + player.fallDistance + " (prevTickFall: " + skill.prevTickFall + " )");
				if (skill.prevTickFall > player.fallDistance && skill.prevTickFall > safeFallDistance(player) && skill.prevTickFall > 3) {
					prtln(player.getDisplayName() + "got some XP in the !onGround check!");

					skill.addedXp = true;
				}
				if (skill.prevTickFall < player.fallDistance || player.fallDistance <= 0) {
					skill.prevTickFall = player.fallDistance;
				}
				skill.prevHealth = player.getHealth();
			} else {
				if (skill.prevTickFall > player.fallDistance && skill.prevTickFall > safeFallDistance(player) && skill.prevTickFall > 3) {
					prtln("Player is now on ground but didn't get the XP, so we'll give it to them now.");
					
					skill.addedXp = true;
				}
			}
			if (skill.addedXp) {
				if (skill.prevHealth <= player.getHealth()) {
					skill.addXPWithUpdate(skill.prevTickFall/2, event.player);
				}
				skill.prevTickFall = 0;
				skill.addedXp = false;
			}
		}
	}
	
	@SubscribeEvent
	public void onPlayerFall(LivingFallEvent event) {
		if (event.entityLiving instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer) event.entityLiving;
			if (event.distance < safeFallDistance(player)) {
				prtln(player.getDisplayName() + " fell " + event.distance + " but didn't take damage, thanks to the glory of AERO!");
				event.setCanceled(true);
			}
		}
	}

	@Override
	public void addDescription() {
		description.add("\u00A7o"+skillName());
		description.add("Your ability to move through the air.");
		description.add("Gain XP by falling long distances without taking damage.");
		description.add("Unlock better fall damage resistance,");
		description.add("higher jumps and better air mobility.");
		description.add("");
		description.add("\u00A7oUnlocked abilities are coming soon! [WIP]");
	}
	
	@Override
	public void addRequirements() {
		requiredSkills.add("skillAgility");
	}
	
	@Override public int xpBarColour() { return 11665613; }
	@Override public void activateSkill(EntityPlayer arg0, World arg1) {}
	@Override public boolean hasGui() { return false; }
	@Override public void openGui() {}
	@Override public int iconX() { return 0; }
	@Override public int iconZ() { return 0; }
	@Override public ResourceLocation skillIcon() { return new ResourceLocation(FlenixSkills.MODID, "textures/gui/skills.png"); }
	@Override public String skillName() { return "Aerobatics"; }
	@Override public String nameFormat() { return "\u00A75"; }

	@Override
	public String shortName() {
		return "AERO";
	}
}
