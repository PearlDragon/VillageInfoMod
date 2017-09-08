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
package jiraiyah.villageinfo.events;

import jiraiyah.villageinfo.infrastructure.Config;
import jiraiyah.villageinfo.infrastructure.VillageData;
import jiraiyah.villageinfo.network.SpawnServerMessage;
import jiraiyah.villageinfo.network.VillageServerMessage;
import jiraiyah.villageinfo.utilities.Log;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.village.Village;
import net.minecraft.village.VillageCollection;
import net.minecraft.village.VillageDoorInfo;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings({"unused"})
public class WorldDataCollector
{
	private int resetCoolDown;

	private static List<UUID> VILLAGE_INFO_PLAYERS = new ArrayList<>();
	private static Map<UUID, List<VillageData>> villageDataList = new HashMap<>();
	private static BlockPos spawnPoint = null;
	private static List<Integer> xCoords = new ArrayList<>();
	private static List<Integer> zCoords = new ArrayList<>();

	@SubscribeEvent
	public void serverTickEvent(TickEvent.ServerTickEvent event)
	{
		resetCoolDown --;
		if (resetCoolDown > 0)
			return;
		resetCoolDown = 20 * Config.updateRatio;
		if (VILLAGE_INFO_PLAYERS == null || VILLAGE_INFO_PLAYERS.size() == 0)
			return;
		resetVillageDataList();
	}

	@SubscribeEvent
	public void worldLoadEvent(WorldEvent.Load event)
	{
		if (spawnPoint == null)
			getSpawnPoint();
		if (xCoords == null || zCoords == null || xCoords.size() == 0 || zCoords.size() == 0)
			getSpawnChunks();
	}

	private static void getSpawnChunks()
	{
		boolean foundFirstChunk = false;
		int cnt = 1;
		IChunkProvider chunkProvider = FMLCommonHandler.instance().getMinecraftServerInstance().getEntityWorld().getChunkProvider();
		for (int x = spawnPoint.getX() - 256; x < spawnPoint.getX() + 256; x += cnt)
		{
			for (int z = spawnPoint.getZ() - 256; z < spawnPoint.getZ() + 256; z += cnt)
			{
				BlockPos tempPos = new BlockPos(x,0,z);
				Chunk chunk = FMLCommonHandler.instance().getMinecraftServerInstance().getEntityWorld().getChunkFromBlockCoords(tempPos);
				if (FMLCommonHandler.instance().getMinecraftServerInstance().getEntityWorld().isSpawnChunk(chunk.x, chunk.z))
				{
					if (!foundFirstChunk)
					{
						foundFirstChunk = true;
						cnt = 16;
					}
					xCoords.add(chunk.x);
					zCoords.add(chunk.z);
				}
			}
		}
	}

	private static void getSpawnPoint()
	{
		spawnPoint = FMLCommonHandler.instance().getMinecraftServerInstance().getEntityWorld().getSpawnPoint();
	}

	@SubscribeEvent
	public void playerLogoutEvent(PlayerEvent.PlayerLoggedOutEvent event)
	{
		removePlayerFromVillageList(event.player.getUniqueID());
	}

	public static void addPlayerToVillageList(UUID player)
	{
		if (!VILLAGE_INFO_PLAYERS.contains(player))
		{
			VILLAGE_INFO_PLAYERS.add(player);
			//Log.info("=================> Added a player to the list");
		}
	}

	public static void removePlayerFromVillageList(UUID player)
	{
		if (VILLAGE_INFO_PLAYERS.contains(player))
		{
			//Log.info("=================> Removed a player from the list");
			VILLAGE_INFO_PLAYERS.remove(player);
			if (villageDataList.containsKey(player))
				villageDataList.remove(player);
		}
	}

	private void resetVillageDataList()
	{
		//Log.info("=================> resetVillageDataList");
		World world = FMLCommonHandler.instance().getMinecraftServerInstance().getEntityWorld();
		VillageCollection villageCollection = world.getVillageCollection();
		List<Village> allVillages = villageCollection.getVillageList();
		villageDataList.clear();
		for (UUID player : VILLAGE_INFO_PLAYERS)
		{
			List<VillageData> tempList = new ArrayList<>();
			if (allVillages == null || allVillages.size() == 0)
			{
				villageDataList.clear();
				VillageServerMessage.sendMessage(player, tempList);
				return;
			}
			EntityPlayer entityPlayer = world.getPlayerEntityByUUID(player);
			float psx = entityPlayer.getPosition().getX();
			float psz = entityPlayer.getPosition().getZ();
			allVillages.stream()
					.filter(v -> psx < v.getCenter().getX() + v.getVillageRadius() + Config.villageDetectDistance &&
							psz < v.getCenter().getZ() + v.getVillageRadius() + Config.villageDetectDistance &&
							psx > v.getCenter().getX() - v.getVillageRadius() - Config.villageDetectDistance &&
							psz > v.getCenter().getZ() - v.getVillageRadius() - Config.villageDetectDistance)
					.forEach(v -> {
						int radius = v.getVillageRadius();
						int villagerCount = v.getNumVillagers();
						int reputation = v.getPlayerReputation(entityPlayer.getName());
						BlockPos center = v.getCenter();
						List<VillageDoorInfo> doorInfos = v.getVillageDoorInfoList();
						List<BlockPos> doorPositions = doorInfos.stream().map(VillageDoorInfo::getDoorBlockPos).collect(Collectors.toList());
						tempList.add(new VillageData(radius, center, doorPositions, villagerCount, reputation));
					});
			if (!villageDataList.containsKey(player))
				villageDataList.put(player, tempList);
			else
				villageDataList.replace(player, tempList);
			VillageServerMessage.sendMessage(player, tempList);
			//Log.info("=================> Sent village data to client");
		}
	}

	public static void getSpawnInformation(UUID playerId)
	{

		SpawnServerMessage.sendMessage(playerId, spawnPoint, xCoords,zCoords);
	}
}
