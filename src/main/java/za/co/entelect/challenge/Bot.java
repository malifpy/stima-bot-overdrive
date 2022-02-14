package za.co.entelect.challenge;

import za.co.entelect.challenge.command.*;
import za.co.entelect.challenge.entities.*;
import za.co.entelect.challenge.enums.Terrain;
import za.co.entelect.challenge.enums.PowerUps;

import java.util.*;

import static java.lang.Math.max;

public class Bot {

    private static final int maxSpeed = 9;
    private List<Integer> directionList = new ArrayList<>();

    private Random random;
    private GameState gameState;
    private Car opponent;
    private Car myCar;
    private boolean emp = false,lizard=false,boost=false,tweet=false,oil=false;
    private final static Command FIX = new FixCommand();


    public Bot(Random random, GameState gameState) {
        this.random = random;
        this.gameState = gameState;
        this.myCar = gameState.player;
        this.opponent = gameState.opponent;

        directionList.add(-1);
        directionList.add(1);
    }

    public Command run() {
        List<Object> blocks = getBlocksInFront(myCar.position.lane, myCar.position.block,"mid");
        for (PowerUps powerUp: myCar.powerups) {
            if (powerUp.equals(PowerUps.BOOST)) {
                boost = true;
            }
            if (powerUp.equals(PowerUps.LIZARD)) {
                lizard = true;
            }
            if (powerUp.equals(PowerUps.EMP)) {
                emp = true;
            }
            if (powerUp.equals(PowerUps.OIL)) {
                oil = true;
            }
            if (powerUp.equals(PowerUps.TWEET)) {
                tweet = true;
            }

        }
        if (myCar.damage > 0) {
            return new FixCommand();
        }
        if(myCar.speed == 0){
            return new AccelerateCommand();
        }
        if (blocks.contains(Terrain.MUD) | blocks.contains(Terrain.OIL_SPILL) | blocks.contains(Terrain.WALL) ) {

            if(myCar.position.lane==1){
                List<Object> blocksRight = getBlocksInFront((myCar.position.lane)+1, myCar.position.block,"right");
                if(blocksRight.contains(Terrain.MUD) | blocksRight.contains(Terrain.OIL_SPILL) | blocksRight.contains(Terrain.WALL) & lizard ){
                    return new LizardCommand();
                }
                return new ChangeLaneCommand(directionList.get(1));
            }
            else if(myCar.position.lane==4){

                List<Object> blocksLeft = getBlocksInFront((myCar.position.lane)-1, myCar.position.block,"left");
                if(blocksLeft.contains(Terrain.MUD) | blocksLeft.contains(Terrain.OIL_SPILL) | blocksLeft.contains(Terrain.WALL)  & lizard ){
                    return new LizardCommand();
                }
                return new ChangeLaneCommand(directionList.get(0));
            }
            else {
                List<Object> blocksLeft = getBlocksInFront((myCar.position.lane)-1, myCar.position.block,"left");
                List<Object> blocksRight = getBlocksInFront((myCar.position.lane)+1, myCar.position.block,"right");
                if(blocksLeft.contains(Terrain.MUD) | blocksLeft.contains(Terrain.OIL_SPILL) | blocksLeft.contains(Terrain.WALL) & !(blocksRight.contains(Terrain.MUD) | blocksRight.contains(Terrain.OIL_SPILL) | blocksRight.contains(Terrain.WALL)) ){
                    return new ChangeLaneCommand(directionList.get(1));
                }
                if(blocksRight.contains(Terrain.MUD) | blocksRight.contains(Terrain.OIL_SPILL) | blocksRight.contains(Terrain.WALL) & !(blocksLeft.contains(Terrain.MUD) | blocksLeft.contains(Terrain.OIL_SPILL) | blocksLeft.contains(Terrain.WALL))){
                    return new ChangeLaneCommand(directionList.get(0));
                }
                if(blocksLeft.contains(Terrain.MUD) | blocksLeft.contains(Terrain.OIL_SPILL) | blocksLeft.contains(Terrain.WALL) & blocksRight.contains(Terrain.MUD) | blocksRight.contains(Terrain.OIL_SPILL) | blocksRight.contains(Terrain.WALL) & lizard ){
                    return new LizardCommand();
                }
                if(blocksLeft.contains(Terrain.MUD) | blocksLeft.contains(Terrain.OIL_SPILL) | blocksLeft.contains(Terrain.WALL) & blocksRight.contains(Terrain.MUD) | blocksRight.contains(Terrain.OIL_SPILL) | blocksRight.contains(Terrain.WALL)){
                    int obsLeft = hitungObstacle((myCar.position.lane)-1,myCar.position.block,"left");
                    int obsRight = hitungObstacle((myCar.position.lane)+1,myCar.position.block,"right");
                    int obsNow = hitungObstacle((myCar.position.lane),myCar.position.block,"mid");
                    if(obsNow<=obsRight & obsNow <= obsLeft){
                        return new AccelerateCommand();
                    }
                    if(obsLeft>=obsRight & obsNow >= obsRight){
                        return new ChangeLaneCommand(directionList.get(1));
                    }
                    if(obsLeft<=obsRight & obsNow >= obsLeft){
                        return new ChangeLaneCommand(directionList.get(0));
                    }

                }

                int i = random.nextInt(directionList.size());
                return new ChangeLaneCommand(directionList.get(i));
            }
        }
        if (boost & cekBoost((myCar.position.lane),myCar.position.block)) {
            return new BoostCommand();
        }
        if (emp & myCar.speed == maxSpeed) {
            return new EmpCommand();
        }
        if (oil & myCar.speed == maxSpeed) {
            return new OilCommand();
        }
        return new AccelerateCommand();
    }

    /**
     * Returns map of blocks and the objects in the for the current lanes, returns the amount of blocks that can be
     * traversed at max speed.
     **/
    private Boolean cekBoost(int lane, int block) {
        List<Lane[]> map = gameState.lanes;
        int startBlock = map.get(0)[0].position.block;
        Lane[] laneList = map.get(lane - 1);
        for (int i = max(block - startBlock, 0); i <= block - startBlock + 15; i++) {
            if (laneList[i].terrain == Terrain.MUD | laneList[i].terrain == Terrain.WALL | laneList[i].terrain == Terrain.OIL_SPILL) {
                return false;
            }
        }
        return true;
    }
    private List<Object> getBlocksInFront(int lane, int block, String dirr) {
        List<Lane[]> map = gameState.lanes;
        List<Object> blocks = new ArrayList<>();
        int startBlock = map.get(0)[0].position.block;
        int start = max(block - startBlock, 0);
        int end=block - startBlock + myCar.speed;
        Lane[] laneList = map.get(lane - 1);
        if (dirr.equalsIgnoreCase("left") | dirr.equalsIgnoreCase("right")){
            end = end-1;
        }
        else{
            start = start+1;
        }
        for (int i = start; i <= end; i++) {
            if (laneList[i] == null || laneList[i].terrain == Terrain.FINISH) {
                break;
            }

            blocks.add(laneList[i].terrain);

        }
        return blocks;
    }
    private int hitungObstacle(int lane, int block,String dirr) {
        List<Lane[]> map = gameState.lanes;
        int jumlah = 0;
        int startBlock = map.get(0)[0].position.block;

        int start = max(block - startBlock, 0);
        int end=block - startBlock + myCar.speed;
        Lane[] laneList = map.get(lane - 1);
        if (dirr.equalsIgnoreCase("left") | dirr.equalsIgnoreCase("right")){
            end = end-1;
        }
        else{
            start = start+1;
        }
        for (int i = start; i <= end; i++){
            if(laneList[i].terrain  == Terrain.MUD | laneList[i].terrain  == Terrain.WALL | laneList[i].terrain == Terrain.OIL_SPILL){
                jumlah++;
            }
        }
        return jumlah;
    }

}
