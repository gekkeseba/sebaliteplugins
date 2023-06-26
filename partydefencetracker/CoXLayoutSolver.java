package net.runelite.client.plugins.partydefencetracker;

import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.InstanceTemplates;
import net.runelite.api.Point;
import net.runelite.api.Tile;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.VarbitChanged;
import net.runelite.client.plugins.raids.Raid;
import net.runelite.client.plugins.raids.RaidRoom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class CoXLayoutSolver {
    private static final Logger log = LoggerFactory.getLogger(CoXLayoutSolver.class);

    static final int ROOM_MAX_SIZE = 32;

    private static final int LOBBY_PLANE = 3;

    private static final int SECOND_FLOOR_PLANE = 2;

    private static final int ROOMS_PER_PLANE = 8;

    private static final int AMOUNT_OF_ROOMS_PER_X_AXIS_PER_PLANE = 4;

    private static final WorldPoint TEMP_LOCATION = new WorldPoint(3360, 5152, 2);

    private static final String CM_RAID_CODE = "SPCFPC#SPC#";

    private final Client client;

    boolean checkInRaid;

    private boolean loggedIn;

    private boolean inRaidChambers;

    private int raidPartyID;

    private Raid raid;

    public Raid getRaid() {
        return this.raid;
    }

    @Inject
    public CoXLayoutSolver(Client client) {
        this.client = client;
    }

    public boolean isCM() {
        return (this.raid != null && "SPCFPC#SPC#".equals(this.raid.toCode()));
    }

    public void onVarbitChanged(VarbitChanged event) {
        if (event.getVarpId() == 1427) {
            boolean tempInRaid = (this.client.getVarbitValue(5432) == 1);
            if (this.loggedIn && !tempInRaid)
                this.raid = null;
            this.raidPartyID = event.getValue();
        }
        if (event.getVarbitId() == 5432) {
            boolean tempInRaid = (event.getValue() == 1);
            if (tempInRaid && this.loggedIn)
                checkRaidPresence();
            this.inRaidChambers = tempInRaid;
        }
    }

    public void onGameTick(GameTick event) {
        if (this.checkInRaid) {
            this.loggedIn = true;
            this.checkInRaid = false;
            if (this.inRaidChambers) {
                checkRaidPresence();
            } else if (this.raidPartyID == -1) {
                this.raid = null;
            }
        }
    }

    public void onGameStateChanged(GameStateChanged event) {
        if (this.client.getGameState() == GameState.LOGGED_IN) {
            if (this.client.getLocalPlayer() == null || this.client
                    .getLocalPlayer().getWorldLocation().equals(TEMP_LOCATION))
                return;
            this.checkInRaid = true;
        } else if (this.client.getGameState() == GameState.LOGIN_SCREEN || this.client
                .getGameState() == GameState.CONNECTION_LOST) {
            this.loggedIn = false;
        } else if (this.client.getGameState() == GameState.HOPPING) {
            this.raid = null;
        }
    }

    private void checkRaidPresence() {
        if (this.client.getGameState() != GameState.LOGGED_IN)
            return;
        this.inRaidChambers = (this.client.getVarbitValue(5432) == 1);
        if (!this.inRaidChambers)
            return;
        this.raid = buildRaid(this.raid);
    }

    private Raid buildRaid(Raid from) {
        Raid raid = from;
        if (raid == null) {
            Point gridBase = findLobbyBase();
            if (gridBase == null)
                return null;
            Integer lobbyIndex = findLobbyIndex(gridBase);
            if (lobbyIndex == null)
                return null;
            raid = new Raid(new WorldPoint(this.client.getBaseX() + gridBase.getX(), this.client.getBaseY() + gridBase.getY(), 3), lobbyIndex.intValue());
        }
        int baseX = raid.getLobbyIndex() % 4;
        int baseY = (raid.getLobbyIndex() % 8 > 3) ? 1 : 0;
        for (int i = 0; i < (raid.getRooms()).length; i++) {
            int x = i % 4;
            int y = (i % 8 > 3) ? 1 : 0;
            int plane = (i > 7) ? 2 : 3;
            x -= baseX;
            y -= baseY;
            x = raid.getGridBase().getX() + x * 32;
            y = raid.getGridBase().getY() - y * 32;
            x -= this.client.getBaseX();
            y -= this.client.getBaseY();
            if (x >= -31 && x < 104) {
                if (x < 1)
                    x = 1;
                if (y < 1)
                    y = 1;
                Tile tile = this.client.getScene().getTiles()[plane][x][y];
                if (tile != null) {
                    RaidRoom room = determineRoom(tile);
                    raid.setRoom(room, i);
                }
            }
        }
        return raid;
    }

    private Point findLobbyBase() {
        Tile[][] tiles = this.client.getScene().getTiles()[3];
        for (int x = 0; x < 104; x++) {
            for (int y = 0; y < 104; y++) {
                if (tiles[x][y] != null && tiles[x][y].getWallObject() != null)
                    if (tiles[x][y].getWallObject().getId() == 12231)
                        return tiles[x][y].getSceneLocation();
            }
        }
        return null;
    }

    private RaidRoom determineRoom(Tile base) {
        int chunkData = this.client.getInstanceTemplateChunks()[base.getPlane()][base.getSceneLocation().getX() / 8][base.getSceneLocation().getY() / 8];
        InstanceTemplates template = InstanceTemplates.findMatch(chunkData);
        if (template == null)
            return RaidRoom.EMPTY;
        switch (template) {
            case RAIDS_LOBBY:
            case RAIDS_START:
                return RaidRoom.START;
            case RAIDS_END:
                return RaidRoom.END;
            case RAIDS_SCAVENGERS:
            case RAIDS_SCAVENGERS2:
                return RaidRoom.SCAVENGERS;
            case RAIDS_SHAMANS:
                return RaidRoom.SHAMANS;
            case RAIDS_VASA:
                return RaidRoom.VASA;
            case RAIDS_VANGUARDS:
                return RaidRoom.VANGUARDS;
            case RAIDS_ICE_DEMON:
                return RaidRoom.ICE_DEMON;
            case RAIDS_THIEVING:
                return RaidRoom.THIEVING;
            case RAIDS_FARMING:
            case RAIDS_FARMING2:
                return RaidRoom.FARMING;
            case RAIDS_MUTTADILES:
                return RaidRoom.MUTTADILES;
            case RAIDS_MYSTICS:
                return RaidRoom.MYSTICS;
            case RAIDS_TEKTON:
                return RaidRoom.TEKTON;
            case RAIDS_TIGHTROPE:
                return RaidRoom.TIGHTROPE;
            case RAIDS_GUARDIANS:
                return RaidRoom.GUARDIANS;
            case RAIDS_CRABS:
                return RaidRoom.CRABS;
            case RAIDS_VESPULA:
                return RaidRoom.VESPULA;
        }
        return RaidRoom.EMPTY;
    }

    private Integer findLobbyIndex(Point gridBase) {
        int x, y;
        if (104 <= gridBase.getX() + 32 || 104 <= gridBase
                .getY() + 32)
            return null;
        Tile[][] tiles = this.client.getScene().getTiles()[3];
        if (tiles[gridBase.getX()][gridBase.getY() + 32] == null) {
            y = 0;
        } else {
            y = 1;
        }
        if (tiles[gridBase.getX() + 32][gridBase.getY()] == null) {
            x = 3;
        } else {
            for (x = 0; x < 3; x++) {
                int sceneX = gridBase.getX() - 1 - 32 * x;
                if (sceneX < 0 || tiles[sceneX][gridBase.getY()] == null)
                    break;
            }
        }
        return Integer.valueOf(x + y * 4);
    }
}
