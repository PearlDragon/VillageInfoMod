/**
 * Copyright 2016 VillageInfoMod (Jiraiyah)
 *
 * project link : http://minecraft.curseforge.com/projects/village-info
 *
 * Licensed under The MIT License (MIT);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://opensource.org/licenses/MIT
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jiraiyah.villageinfo;

import jiraiyah.villageinfo.infrastructure.Config;
import jiraiyah.villageinfo.proxies.CommonProxy;
import jiraiyah.villageinfo.references.Reference;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = Reference.MOD_ID, version = Reference.VERSION, name = Reference.MOD_NAME)
public class VillageInfo
{
    @SidedProxy(clientSide = Reference.CLIENT_PROXY, serverSide = Reference.COMMON_PROXY)
    public static CommonProxy PROXY;

    public static boolean solidDraw = false;
    public static boolean villageBorder = false;
    public static boolean showVillages = false;
    public static boolean villageDoors = false;
    public static boolean villageSphere = false;
    public static boolean villageGolem = false;
    public static boolean villageInfoText = true;
    public static boolean villageCenter = false;
    public static boolean disableDepth = false;
    public static boolean perVillageColor = false;
    public static boolean chunkBorder = false;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        PROXY.preInit(event);
        Config.loadConfigsFromFile(event.getSuggestedConfigurationFile());
    }

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        PROXY.init(event);
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
        PROXY.postInit(event);
    }
}
