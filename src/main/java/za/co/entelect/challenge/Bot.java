package za.co.entelect.challenge;

import za.co.entelect.challenge.command.*;
import za.co.entelect.challenge.entities.*;
import za.co.entelect.challenge.enums.PowerUps;
import za.co.entelect.challenge.enums.Terrain;

import java.util.*;

import static java.lang.Math.max;


public class Bot {

    private List<Command> directionList = new ArrayList<>();
    private static List<Terrain> AllPowerUps = Arrays.asList(
            Terrain.BOOST,
            Terrain.EMP,
            Terrain.LIZARD,
            Terrain.OIL_POWER,
            Terrain.TWEET);
    private static Boolean debugLog = false;
    private static Integer debugDepth = 0;


    private final static Command ACCELERATE = new AccelerateCommand();
    private final static Command LIZARD = new LizardCommand();
    private final static Command OIL = new OilCommand();
    private final static Command BOOST = new BoostCommand();
    private final static Command EMP = new EmpCommand();
    private final static Command FIX = new FixCommand();

    private final static Command TURN_RIGHT = new ChangeLaneCommand(1);
    private final static Command TURN_LEFT = new ChangeLaneCommand(-1);

    static class laneRating {
        public String laneType;
        public int potScr;
        public int potDmg;

        public laneRating(int scr, int dmg, String type) {
            potDmg = dmg;
            potScr = scr;
            laneType = type;
        }
    }

    public Bot() {
        directionList.add(TURN_LEFT);
        directionList.add(TURN_RIGHT);
    }

    public Bot(Boolean debug){
        directionList.add(TURN_LEFT);
        directionList.add(TURN_RIGHT);
        debugLog = debug;
    }

    public Command run(GameState gameState) {
        // Car Setup
        Car myCar = gameState.player;

        List<laneRating> pathsProcessed = processAllLane(myCar, gameState.lanes);
        if (pathsProcessed.isEmpty()) {
            return FIX;
        } else {
            laneRating optimalLane = maxScoreLaneRating(pathsProcessed);
            if (optimalLane.laneType.equals("MIDDLE")) {
                return usePowerUpOr(Arrays.asList(myCar.powerups), ACCELERATE);
            } else if (optimalLane.laneType.equals("LEFT")) {
                return TURN_LEFT;
            } else {
                return TURN_RIGHT;
            }
        }
    }

    private static void debugMessage(String text, int addDepth){
        if(debugLog){
            System.out.println("[%d] %s".formatted(debugDepth, text));
            debugDepth += addDepth;
        }
    }

    private static Command usePowerUpOr(List<PowerUps> powerUpsList, Command other) {
        if (powerUpsList.contains(PowerUps.BOOST)) {
            return BOOST;
        } else if (powerUpsList.contains(PowerUps.OIL)) {
            return OIL;
        } else if (powerUpsList.contains(PowerUps.EMP)) {
            return EMP;
        } else {
            return other;
        }

    }

    private static laneRating maxScoreLaneRating(List<laneRating> lanes) {
        debugMessage("IN  maxScoreLaneRating(List<laneRating>)", 1);
        laneRating maxScrLane = new laneRating(Integer.MIN_VALUE, Integer.MAX_VALUE, "NONE");
        laneRating newLane;
        for (int i = 0; i < lanes.size(); i++) {
            newLane = lanes.get(i);
            if (newLane.potScr > maxScrLane.potScr
                    || (newLane.potScr == maxScrLane.potScr && newLane.potDmg < maxScrLane.potDmg)) {
                maxScrLane = newLane;
            }
        }
        debugMessage("OUT return %s".formatted(maxScrLane.laneType), -1);
        return maxScrLane;
    }

    private static List<laneRating> processAllLane(Car myCar, List<Lane[]> map) {
        debugMessage("IN  processLane(Car, List<Lane[]>)", 1);
        List<laneRating> potLanes = new ArrayList<>();
        // Process Left
        laneRating rLane;
        for (int laneIdx : Arrays.asList(0, -1, 1)) {
            rLane = processLane(myCar, map, laneIdx);
            if (myCar.damage + rLane.potDmg < 5 && !rLane.laneType.equals("NONE")) {
                potLanes.add(rLane);
            }
        }
        debugMessage("OUT  processLane(Car, List<Lane[]>, int)", -1);
        return potLanes;
    }

    private static String kToLaneType(int k) {
        debugMessage("IN  kToLaneType(int)", 1);
        String laneType;
        switch (k) {
            case -1:
                laneType = "LEFT";
                break;
            case 1:
                laneType = "RIGHT";
                break;
            default:
                laneType = "MIDDLE";
                break;
        }
        debugMessage("OUT return %s".formatted(laneType), -1);
        return laneType;
    }

    private static laneRating processLane(Car myCar, List<Lane[]> map, int k) {
        debugMessage("IN  processLane(Car, List<Lane[]>, int)", 1);
        List<Object> lane = getBlocks(myCar, map, k);
        if (lane != null) {
            debugMessage("OUT return normal", -1);
            return laneRate(lane, kToLaneType(k));
        } else {
            debugMessage("OUT return NONE", -1);
            return new laneRating(Integer.MIN_VALUE, Integer.MAX_VALUE, "NONE");
        }

    }

    private static laneRating laneRate(List<Object> lane, String laneType) {
        debugMessage("IN  laneRate(List<Object>, String)", 1);
        int potScr = 0;
        int potDmg = 0;

        for (int objIdx = 0; objIdx < lane.size(); objIdx++) {
            if (AllPowerUps.contains(lane.get(objIdx))) {
                potScr += 8;
                // 4 untuk ngambil Power Up
                // 4 untuk pakai Power Up
            }
            if (lane.get(objIdx).equals(Terrain.MUD)) {
                potScr -= 3;
                potDmg += 1;
            }
            if (lane.get(objIdx).equals(Terrain.OIL_SPILL)) {
                potScr -= 4;
                potDmg += 1;
            }
            if (lane.get(objIdx).equals(Terrain.WALL)) {
                potDmg += 2;
            }
        }

        debugMessage("OUT potScr: %d, potDmg, laneType %s".formatted(potScr, potDmg, laneType), -1);
        return new laneRating(potScr, potDmg, laneType);

    }

    // private Boolean hasPowerUp(PowerUps powerUpToCheck, PowerUps[] available) {
    //     for (PowerUps powerUp : available) {
    //         if (powerUp.equals(powerUpToCheck)) {
    //             return true;
    //         }
    //     }
    //     return false;
    // }

    private static List<Object> getBlocks(Car myCar, List<Lane[]> map, int k) {
        debugMessage("IN  getBlocks(Car, List<Lane[]>, int)", 1);
        /*
         * Left -> k = -1
         * Middle -> k = 0
         * Right -> k = +1
         */

        int lane = myCar.position.lane;
        int block = myCar.position.block;
        // List<Lane[]> map = gameState.lanes; // The whole map
        List<Object> blocks = new ArrayList<>(); // Blocks container
        int startBlock = map.get(0)[0].position.block; // Getting the first block

        int getLane = lane - 1 + k;
        if (getLane >= 0) {
            Lane[] laneList = map.get(lane - 1 + k);
            for (int i = max(block - startBlock, 0); i <= block - startBlock + myCar.speed - Math.abs(k); i++) {
                if (laneList[i] == null || laneList[i].terrain == Terrain.FINISH) {
                    break;
                }

                blocks.add(laneList[i].terrain);

            }
            debugMessage("OUT returned blocks", -1);
            return blocks;
        } else {
            debugMessage("OUT returned null", -1);
            return null;
        }
    }

}
