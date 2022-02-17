package za.co.entelect.challenge;

import za.co.entelect.challenge.command.*;
import za.co.entelect.challenge.entities.*;
import za.co.entelect.challenge.enums.Terrain;
import za.co.entelect.challenge.enums.PowerUps;

import java.util.*;

import static java.lang.Math.max;

public class Bot {

    private List<Integer> directionList = new ArrayList<>();

    private Random random;
    private GameState gameState;
    private Car opponent;
    private Car myCar;
    private final static Command FIX = new FixCommand();
    private final static Command OIL = new OilCommand();
    private final static Command BOOST = new BoostCommand();
    private final static Command EMP = new EmpCommand();
    private final static Command LIZARD = new LizardCommand();
    private final static Command TURN_RIGHT = new ChangeLaneCommand(1);
    private final static Command TURN_LEFT = new ChangeLaneCommand(-1);

    public Bot(Random random, GameState gameState) {
        this.random = random;
        this.gameState = gameState;
        this.myCar = gameState.player;
        this.opponent = gameState.opponent;

        directionList.add(-1);
        directionList.add(1);
    }

    public Command run() {
        List<Object> blocksInFront = getBlocksIn("front", myCar.position.lane, myCar.position.block);
        int potentialDamageFront = getPotentialDamage(blocksInFront);
        int potentialPowerUpsFront = getPotentialPowerUps(blocksInFront);
        if (myCar.damage > 1) {
            return FIX;
        }
        if(myCar.speed == 0) {
            return new AccelerateCommand();
        }
        if(myCar.position.lane == 2 | myCar.position.lane == 3) {
            List<Object> blocksInRight = getBlocksIn("right", myCar.position.lane, myCar.position.block);
            List<Object> blocksInLeft = getBlocksIn("left", myCar.position.lane, myCar.position.block);
            int potentialDamageRight = getPotentialDamage(blocksInRight);
            int potentialDamageLeft = getPotentialDamage(blocksInLeft);
            int potentialPowerUpsRight = getPotentialPowerUps(blocksInRight);
            int potentialPowerUpsLeft = getPotentialPowerUps(blocksInLeft);
            // kasus hanya kiri kosong
            if(isNgekor()) {
                if (hasPowerUp(PowerUps.LIZARD) && cekLizard(blocksInFront,potentialDamageFront) !=0) { // tambahain syarat kalo jatuh ke block yang ada penghalangnya
                    return LIZARD;
                }
                if(hasPowerUp(PowerUps.LIZARD) && potentialDamageFront !=0 && cekLizard(blocksInFront,potentialDamageFront) ==0){
                    return new DecelerateCommand();
                }
                if(potentialDamageLeft == 0 && potentialDamageRight == 0) {
                    return turn_random();
                }
                if(potentialDamageLeft == 0) {
                    return TURN_LEFT;
                }
                if(potentialDamageRight == 0) {
                    return TURN_RIGHT;
                }
            }
            if (potentialDamageLeft == 0 && potentialDamageFront > 0 && potentialDamageRight > 0) {
                return TURN_LEFT;
            }
            // kasus hanya kanan kosong
            if (potentialDamageLeft > 0 && potentialDamageFront > 0 && potentialDamageRight == 0) {
                return TURN_RIGHT;
            }
            // kasus kanan kiri kosong, yang tengah ada
            if (potentialDamageLeft == 0 && potentialDamageFront > 0 && potentialDamageRight == 0) {
                if (potentialPowerUpsLeft > potentialPowerUpsRight) {
                    return TURN_LEFT;
                }
                if (potentialPowerUpsLeft == potentialPowerUpsRight) {
                    return turn_random();
                }
                // power left < power right
                return TURN_RIGHT;
            }
            // kasus damage semua lane nol
            if (potentialDamageLeft == 0 && potentialDamageFront == 0 && potentialDamageRight == 0) {
                if (potentialPowerUpsLeft > potentialPowerUpsRight && potentialPowerUpsLeft > potentialPowerUpsFront) {
                    return TURN_LEFT;
                }
                if (potentialPowerUpsLeft < potentialPowerUpsRight && potentialPowerUpsRight > potentialPowerUpsFront) {
                    return TURN_RIGHT;
                }
                if (potentialPowerUpsLeft == potentialPowerUpsRight && potentialPowerUpsLeft > potentialPowerUpsFront) {
                    return turn_random();
                }
            }
            // kasus semua lane ada damage
            if (potentialDamageLeft > 0 && potentialDamageFront > 0 && potentialDamageRight > 0) {
                if (hasPowerUp(PowerUps.LIZARD) && cekLizard(blocksInFront,potentialDamageFront) !=0) { // tambahain syarat kalo jatuh ke block yang ada penghalangnya
                    return LIZARD;
                }
                if(hasPowerUp(PowerUps.LIZARD) && cekLizard(blocksInFront,potentialDamageFront) ==0){
                    return new DecelerateCommand();
                }// gak punya lizard
                if (potentialDamageLeft < potentialDamageRight && potentialDamageLeft < potentialDamageFront) {
                    return TURN_LEFT;
                }
                if (potentialDamageLeft > potentialDamageRight && potentialDamageRight < potentialDamageFront) {
                    return TURN_RIGHT;
                }
                if (potentialDamageFront > potentialDamageLeft && potentialDamageFront > potentialDamageRight) {
                    if (potentialPowerUpsLeft > potentialPowerUpsRight && potentialPowerUpsLeft > potentialPowerUpsFront) {
                        return TURN_LEFT;
                    }
                    if (potentialPowerUpsLeft < potentialPowerUpsRight && potentialPowerUpsRight > potentialPowerUpsFront) {
                        return TURN_RIGHT;
                    }
                    if (potentialPowerUpsLeft == potentialPowerUpsRight && potentialPowerUpsLeft > potentialPowerUpsFront) {
                        return turn_random();
                    }
                }
            }
        }
        if(myCar.position.lane == 1) {
            List<Object> blocksInRight = getBlocksIn("right", myCar.position.lane, myCar.position.block);
            int potentialDamageRight = getPotentialDamage(blocksInRight);
            int potentialPowerUpsRight = getPotentialPowerUps(blocksInRight);
            if(potentialDamageRight == 0 && potentialDamageFront > 0) {
                return TURN_RIGHT;
            }
            if((potentialDamageRight > 0 && potentialDamageFront > 0) || isNgekor()) {
                if (hasPowerUp(PowerUps.LIZARD) && cekLizard(blocksInFront,potentialDamageFront) !=0) { // tambahain syarat kalo jatuh ke block yang ada penghalangnya
                    return LIZARD;
                }
                if(hasPowerUp(PowerUps.LIZARD) && potentialDamageFront !=0 && cekLizard(blocksInFront,potentialDamageFront) ==0){
                    return new DecelerateCommand();
                }
                if(potentialDamageFront > potentialDamageRight) {
                    return TURN_RIGHT;
                }
            } // damageRight <= damageFront
            if(potentialDamageFront == 0 && potentialDamageRight == 0) {
                if(potentialPowerUpsRight > potentialPowerUpsFront) {
                    return TURN_RIGHT;
                } // potensialFront >= potensialRight
            }
        }
        if(myCar.position.lane == 4) {
            List<Object> blocksInLeft = getBlocksIn("left", myCar.position.lane, myCar.position.block);
            int potentialDamageLeft = getPotentialDamage(blocksInLeft);
            int potentialPowerUpsLeft = getPotentialPowerUps(blocksInLeft);
            if(potentialDamageLeft == 0 && potentialDamageFront > 0) {
                return TURN_LEFT;
            }
            if((potentialDamageLeft > 0 && potentialDamageFront > 0) || isNgekor()) {
                if (hasPowerUp(PowerUps.LIZARD) && cekLizard(blocksInFront,potentialDamageFront) !=0) { // tambahain syarat kalo jatuh ke block yang ada penghalangnya
                    return LIZARD;
                }
                if(hasPowerUp(PowerUps.LIZARD) && potentialDamageFront !=0 && cekLizard(blocksInFront,potentialDamageFront) ==0){
                    return new DecelerateCommand();
                }
                if(potentialDamageFront > potentialDamageLeft) {
                    return TURN_LEFT;
                }
            } // damageRight <= damageFront
            if(potentialDamageFront == 0 && potentialDamageLeft == 0) {
                if(potentialPowerUpsLeft > potentialPowerUpsFront) {
                    return TURN_LEFT;
                } // potensialFront >= potensialRight
            }
        }
        if(hasPowerUp(PowerUps.EMP) && !isInFront() && !isInSameBlock() && isReachable()) {
            return EMP;
        }
        if(hasPowerUp(PowerUps.OIL) && isInFront()) {
            return OIL;
        }
        if(hasPowerUp(PowerUps.TWEET)) {
            if((myCar.position.block - opponent.position.block + opponent.speed > 1) || (!isInFront() && !isInSameLane()) || (!isInFront() && opponent.position.block - myCar.position.block > 15)) {
                return new TweetCommand(opponent.position.lane, opponent.position.block + opponent.speed + 1);
            }
        }
        List<Object> blocksAtNextSpeed = getBlocksIn("nextSpeed", myCar.position.lane, myCar.position.block);
        int potentialDamageAtNextSpeed = getPotentialDamage(blocksAtNextSpeed);
        if(hasPowerUp(PowerUps.BOOST)) {
            List<Object> blocksAtMaxSpeed = getBlocksIn("maxSpeed", myCar.position.lane, myCar.position.block);
            int potentialDamageAtMaxSpeed = getPotentialDamage(blocksAtMaxSpeed);
            if(potentialDamageAtMaxSpeed > 0) {
                if(!isMaxSpeed() && potentialDamageAtNextSpeed == 0) {
                    return new AccelerateCommand();
                }
                return new DoNothingCommand();
            }
            if(myCar.damage != 0) {
                return FIX;
            }
            if(myCar.boostCounter == 0) {
                return BOOST;
            }
        }
        // jalan lurus doang
        if(!isMaxSpeed() && myCar.speed > 0 && potentialDamageAtNextSpeed - potentialDamageFront > 0) {
            return new DoNothingCommand();
        }
        return new AccelerateCommand();
    }

    /**
     * mengembalikan true apabila mobil berada di maxSpeed
     */
    private Boolean isMaxSpeed() {
        return myCar.speed == maxSpeed();
    }

    /**
     * mengembalikan speed selanjutnya apabila diaccelerate
     */
    private int nextSpeed() {
        if(myCar.speed == 0) {
            return 3;
        }
        if(myCar.speed == 3) {
            return 6;
        }
        if(myCar.speed == 5) {
            return 6;
        }
        if(myCar.speed == 6) {
            return 8;
        }
        if(myCar.speed == 8) {
            return 9;
        }
        return 15;
    }

    /**
     * mengembalikan nilai maksimal speed apabila mobil sedang berada di maxSpeed
     */
    private int maxSpeed() {
        if(myCar.damage == 0) {
            return 15;
        }
        if(myCar.damage == 1) {
            return 9;
        }
        if(myCar.damage == 2) {
            return 8;
        }
        if(myCar.damage == 3) {
            return 6;
        }
        if(myCar.damage == 4) {
            return 3;
        }
        return 0;
    }

    /**
     * mengembalikan true apabila mobil ngekor di belakang musuh
     */
    private Boolean isNgekor() {
        return (myCar.position.block == opponent.position.block - 1) && isInSameLane();
    }

    /**
     * mengembalikan true apabila memiliki power up yang diinginkan
     */
    private Boolean hasPowerUp(PowerUps powerUpToCheck) {
        for(PowerUps powerUp : myCar.powerups) {
            if(powerUp.equals(powerUpToCheck)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Mengembalikan arah belok random
     */

    private Command turn_random() {
        int i = random.nextInt(directionList.size());
        return new ChangeLaneCommand(directionList.get(i));
    }

    /**
     * Mengembalikan true apabila mobil berada di depan musuh
     */
    private Boolean isInFront() {
        return myCar.position.block > opponent.position.block;
    }

    /**
     * Mengembalikan true apbila mobil berada di lane yang sama
     */
    private Boolean isInSameLane() {
        return myCar.position.lane == opponent.position.lane;
    }

    /**
     * Mengembalikan true apbila mobil berada di block yang sama
     */
    private Boolean isInSameBlock() {
        return myCar.position.block == opponent.position.block;
    }

    /**
     * Mengembalikan true apabila mobil musuh berada di jangkauan emp
     * @return
     */
    private Boolean isReachable() {
        return myCar.position.lane == opponent.position.lane || myCar.position.lane == opponent.position.lane + 1 || myCar.position.lane == opponent.position.lane - 1;
    }

    /**
     * Returns map of blocks and the objects in the for the current lanes, returns the amount of blocks that can be
     * traversed at car speed.
     **/
    private List<Object> getBlocksIn(String position, int lane, int block) {
        List<Lane[]> map = gameState.lanes;
        List<Object> blocks = new ArrayList<>();
        int startBlock = map.get(0)[0].position.block;
        // untuk di depan dari posisi mobil, block di mana mobil berada tidak
        // ikut diperhitungkan
        int lowerBound = max(block + 1 - startBlock, 0);
        int upperBound = block - startBlock + myCar.speed;
        if(position == "right") {
            lane += 1;
            lowerBound = max(block - startBlock, 0);
            upperBound = block - startBlock + myCar.speed - 1;
        } else if(position == "left") {
            lane -= 1;
            lowerBound = max(block - startBlock, 0);
            upperBound = block - startBlock + myCar.speed - 1;
        } else if(position == "nextSpeed") {
            upperBound = block - startBlock + nextSpeed();
        } else if(position == "maxSpeed") {
            upperBound = block - startBlock + maxSpeed();
        }
        Lane[] laneList = map.get(lane - 1);
        for (int i = lowerBound; i <= upperBound; i++) {
            if (laneList[i] == null || laneList[i].terrain == Terrain.FINISH) {
                break;
            }
            if(laneList[i].isOccupiedByCyberTruck) {
                blocks.add("Cybertruck");
            } else {
                blocks.add(laneList[i].terrain);
            }
        }
        return blocks;
    }

    /**
     * mengembalikan jumlah potensial damage yang diterima di suatu lane
     * exclude cybertruck
     * @param listTerrain
     * @return
     */
    private int getPotentialDamage(List<Object> listTerrain) {
        int damage = 0;
        for (int i = 0; i < listTerrain.size(); i++) {
            if(listTerrain.get(i) == Terrain.MUD | listTerrain.get(i) == Terrain.OIL_SPILL) {
                damage += 1;
            } else if(listTerrain.get(i) == Terrain.WALL || listTerrain.get(i).equals("Cybertruck")) {
                damage += 2;
            }
        }
        return damage;
    }

    private int getPotentialPowerUps(List<Object> listTerrain) {
        int countPowerUp = 0;
        for (int i = 0; i < listTerrain.size(); i++) {
            if(listTerrain.get(i) == Terrain.OIL_POWER | listTerrain.get(i) == Terrain.BOOST | listTerrain.get(i) == Terrain.LIZARD | listTerrain.get(i) == Terrain.EMP | listTerrain.get(i) == Terrain.TWEET) {
                countPowerUp++;
            }
        }
        return countPowerUp;
    }
    private int cekLizard(List<Object> listTerrain,int damage) {
        int i = listTerrain.size()-1;
        if(listTerrain.get(i) == Terrain.MUD | listTerrain.get(i) == Terrain.OIL_SPILL) {
            damage -= 1;
        } else if(listTerrain.get(i) == Terrain.WALL || listTerrain.get(i).equals("Cybertruck")) {
            damage -= 2;
        }
        return damage;
    }
}

/*
1. cek jumlah damage obstacle tiap lane yang mungkin berdasarkan jenis obstacle. jangan
periksa Block yang ditempati mobil karena sudah tidak berlaku lagi. Jadi mulai hitung block di depan mobil apabila lurus.
Mud: 1
Oil: 1
Wall: 2
Cybertruck: 2
2. pilih lane yang paling dikit damagenya. Belok jika damage besar apabila lurus.
{ kenapa gak cari lane yang ada power upnya? karena apabila bisa ambil power up tapi
kena damage, max speed akan berkurang yang membuat mobil tidak bisa menggunakan power up scr optimal }
3. apabila ada dua lane yang damagenya sama dengan syarat lebih kecil dari lane yang satunya,
    maka pilih jalur yang punya banyak power up (diperksa dulu).
4. apabila jumlah power up sama, ambil jalur lurus (karena speed tidak dikurangi)
5. apabila jalan lurus ada block dan punya lizard, pakai aja.
5. apabila tetap ambil jalan lurus, gunakan power up dengan ketentuan
- gunain emp (posisi di belakang) -> membuat speed musuh jadi 3
- gunain tweet -> ngasih cyberstruk (damage 2)
- gunain oil (posisi di depan) -> damage 1
 */