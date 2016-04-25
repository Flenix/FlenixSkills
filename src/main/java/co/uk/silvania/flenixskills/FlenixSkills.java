package co.uk.silvania.flenixskills;

import co.uk.silvania.flenixskills.skills.SkillAerobatics;
import co.uk.silvania.rpgcore.RegisterSkill;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.common.MinecraftForge;

@Mod(modid = FlenixSkills.MODID, version = FlenixSkills.VERSION, dependencies = "required-after:rpgcore")
public class FlenixSkills {
    public static final String MODID = "flenixskills";
    public static final String VERSION = "1.2";
    
    @EventHandler
    public void init(FMLInitializationEvent event) {
    	SkillAerobatics skillAerobatics = new SkillAerobatics(null, "skillAerobatics");

    	RegisterSkill.register(skillAerobatics);

    	MinecraftForge.EVENT_BUS.register(new SkillAerobatics(null, "skillAerobatics"));
    	FMLCommonHandler.instance().bus().register(new SkillAerobatics(null, "skillAerobatics"));
    }
}
