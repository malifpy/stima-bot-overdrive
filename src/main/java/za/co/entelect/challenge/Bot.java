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
    private final static Command FIX = new FixCommand();
    private final static Command OIL = new OilCommand();
    private final static Command BOOST = new BoostCommand();
    private final static Command EMP = new EmpCommand();
    private final static Command LIZARD = new LizardCommand();
    private final static Command TWEET = new TweetCommand();
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
        if(myCar.position.lane == 2 | myCar.position.lane == 3) {
            List<Object> blocksInRight = getBlocksIn("right", myCar.position.lane, myCar.position.block);
            List<Object> blocksInLeft = getBlocksIn("left", myCar.position.lane, myCar.position.block);
            int potentialDamageRight = getPotentialDamage(blocksInRight);
            int potentialDamageLeft = getPotentialDamage(blocksInLeft);
            int potentialPowerUpsRight = getPotentialPowerUps(blocksInRight);
            int potentialPowerUpsLeft = getPotentialPowerUps(blocksInLeft);

            if(potentialDamageRight < potentialDamageLeft && potentialDamageRight < potentialDamageFront) {
                return TURN_RIGHT;
            }
            if(potentialDamageLeft < potentialDamageRight && potentialDamageLeft < potentialDamageFront) {
                return TURN_LEFT;
            } // damage lurus paling kecil, atau ada yang sama dan lurus bisa besar
            if(potentialDamageFront == potentialDamageLeft) {
                if(potentialPowerUpsLeft > potentialPowerUpsFront) {
                    return TURN_LEFT;
                } // tetap lurus
            }
            if(potentialDamageFront == potentialDamageRight) {
                if(potentialPowerUpsRight > potentialPowerUpsFront) {
                    return TURN_RIGHT;
                } // tetap lurus
            }
            if(potentialDamageLeft == potentialDamageRight && potentialDamageRight != 0 && potentialDamageFront > 0) {
                if(potentialPowerUpsRight < potentialPowerUpsLeft) {
                    return TURN_LEFT;
                }
                if(potentialPowerUpsRight > potentialPowerUpsLeft) {
                    return TURN_RIGHT;
                }
                // left == right
            }
            if(potentialDamageFront > 0) {
                if(potentialDamageLeft == 0) {
                    return TURN_LEFT;
                }
                if(potentialDamageRight == 0) {
                    return TURN_RIGHT;
                }
                if(hasPowerUp(PowerUps.LIZARD)) {
                    return LIZARD;
                }
                int i = random.nextInt(directionList.size());
                return new ChangeLaneCommand(directionList.get(i));
            }
        }
        if(myCar.position.lane == 1) {
            List<Object> blocksInRight = getBlocksIn("right", myCar.position.lane, myCar.position.block);
            int potentialDamageRight = getPotentialDamage(blocksInRight);
            int potentialPowerUpsRight = getPotentialPowerUps(blocksInRight);
            if(potentialDamageFront > potentialDamageRight) {
                return TURN_RIGHT;
            } // damageRight <= damageFront
            if(potentialDamageFront == potentialDamageRight) {
                if(potentialPowerUpsRight > potentialPowerUpsFront) {
                    return TURN_RIGHT;
                } // potensialFront >= potensialRight
            }
        }
        if(myCar.position.lane == 4) {
            List<Object> blocksInLeft = getBlocksIn("left", myCar.position.lane, myCar.position.block);
            int potentialDamageLeft = getPotentialDamage(blocksInLeft);
            int potentialPowerUpsLeft = getPotentialPowerUps(blocksInLeft);
            if(potentialDamageFront > potentialDamageLeft) {
                return TURN_LEFT;
            } // damageLeft <= damageFront
            if(potentialDamageFront == potentialDamageLeft) {
                if(potentialPowerUpsLeft > potentialPowerUpsFront) {
                    return TURN_LEFT;
                } // potensialFront >= potensialLeft
            }
        }
        if(hasPowerUp(PowerUps.EMP) && !isInFront() && isInSameLane() && isReachable()) {
            return EMP;
        }
        if(hasPowerUp(PowerUps.OIL) && isInFront()) {
            return OIL;
        }
        if(hasPowerUp(PowerUps.TWEET) && !isInFront() && isTweetable()) {
            return TWEET;
        }
        if(hasPowerUp(PowerUps.BOOST) && myCar.damage != 0) {
            return FIX;
        }
        if(hasPowerUp(PowerUps.BOOST)) {
            return BOOST;
        }
        return new AccelerateCommand();
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
     * Mengembalikan true apabila mobil musuh berada di jangkauan emp
     * @return
     */
    private Boolean isReachable() {
        return myCar.position.block + 15 >= opponent.position.block;
    }

    /**
     * Mengembalikan true apabila mobil berada di block + speednya kurang dari blok ke 76
     * @return
     */
    private Boolean isTweetable() {
        return myCar.position.block + 76 >= opponent.position.block + opponent.speed;
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
        }
        Lane[] laneList = map.get(lane - 1);
        for (int i = lowerBound; i <= upperBound; i++) {
            if (laneList[i] == null || laneList[i].terrain == Terrain.FINISH) {
                break;
            }

            blocks.add(laneList[i].terrain);

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
            } else if(listTerrain.get(i) == Terrain.WALL) {
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
}

/*


0. kalau ada di lane 3 atau lane 4, periksa apakah di lane 4 ada cybertruck.
    kalau ada harus dihindari

1. cek jumlah damage obstacle tiap lane yang mungkin berdasarkan jenis obstacle. jangan
periksa Block yang ditempati mobil karena sudah tidak berlaku lagi. Jadi mulai hitung block di depan mobil apabila lurus.
Mud: 1
Oil: 1
Wall: 2
Cybertruck: 2

2. pilih lane yang paling dikit damagenya. Belok jika damage besar apabila lurus.
{ kenapa gak cari lane yang ada power upnya? karena apabila bisa ambil power up tapi
kena damage, max speed akan berkurang yang membuat mobil tidak bisa menggunakan power up scr optimal }

4. apabila jumlahnya sama semua, pilih yang wallnya dikit, sama? pilih mud dikit.

3. apabila ada dua lane yang damagenya sama dengan syarat lebih kecil dari lane yang satunya,
    maka pilih jalur yang punya banyak power up (diperksa dulu).

4. apabila jumlah power up sama, ambil jalur lurus (karena speed tidak dikurangi)

5. apabila tetap ambil jalan lurus, gunakan power up dengan ketentuan
- gunain emp (posisi di belakang) -> membuat speed musuh jadi 3
- gunain tweet 4 76 (posisi di belakang) -> ngasih cyberstruk (damage 2)
- ambil oil (posisi di depan) -> damage 1

6.
 */