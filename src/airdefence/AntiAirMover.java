package airdefence;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author user
 */
public class AntiAirMover implements AircraftLogic{
    public AntiAirMover() {
        
    }
    void moveRocketToEnemy(Target antiAir, Target enemy, boolean firstLaunch) throws TargetDestroyedException,
            AntiAirMissedException{
        int enemyX = enemy.getX();
        int enemyY = enemy.getY();
        if (Math.sqrt((antiAir.getX()-enemyX)*(antiAir.getX()-enemyX)
                +(antiAir.getY()-enemyY)*(antiAir.getY()-enemyY)) <= antiAir.getSpeed()/2) {
            throw new TargetDestroyedException();
        }
        List<Target> antiAirStepList;
        List<Target> enemyStepList = getAirCraftPossiblePositions(enemy);
        enemyStepList.add(enemy);
        Target antiAirClosestStep;
        Target enemyClosestStep = getAirCraftClosestPosition(enemyStepList, antiAir);
        boolean a = enemyClosestStep.getX()>=antiAir.getX(); //true - цель в правой части от ракеты, false - в левой
        boolean b = enemyClosestStep.getY()>=antiAir.getY(); //true - цель в нижней части от ракеты, false - в верхней
        boolean c = (enemyClosestStep.getX()-antiAir.getX())*(enemyClosestStep.getX()-antiAir.getX())
                >= (enemyClosestStep.getY()-antiAir.getY())*(enemyClosestStep.getY()-antiAir.getY()); //true - цель ближе к оси Х, false - к оси Y
        boolean d = c ? (enemyClosestStep.getX()-antiAir.getX())*(enemyClosestStep.getX()-antiAir.getX()) 
                >= (enemyClosestStep.getY()-antiAir.getY())*(enemyClosestStep.getY()-antiAir.getY()) * 2 
                : (enemyClosestStep.getY()-antiAir.getY())*(enemyClosestStep.getY()-antiAir.getY()) 
                >= (enemyClosestStep.getX()-antiAir.getX())*(enemyClosestStep.getX()-antiAir.getX()) * 2;//true - цель билже именно к оси, чем к середине между осями
        if(firstLaunch) {
            if (a) {
                if (b) {
                    if (c) {
                        if (d) {
                            antiAir.setDir(Target.Direction.E);
                            antiAir.setX(antiAir.getX()+antiAir.getSpeed());
                        } else {
                            antiAir.setDir(Target.Direction.SE);
                            antiAir.setX(antiAir.getX()+antiAir.getSpeed()*7/10);
                            antiAir.setY(antiAir.getY()+antiAir.getSpeed()*7/10);
                        }
                    } else {
                        if (d) {
                            antiAir.setDir(Target.Direction.S);
                            antiAir.setY(antiAir.getY()+antiAir.getSpeed());
                        } else {
                            antiAir.setDir(Target.Direction.SE);
                            antiAir.setX(antiAir.getX()+antiAir.getSpeed()*7/10);
                            antiAir.setY(antiAir.getY()+antiAir.getSpeed()*7/10);
                        }
                    }
                } else {
                    if (c) {
                        if (d) {
                            antiAir.setDir(Target.Direction.E);
                            antiAir.setX(antiAir.getX()+antiAir.getSpeed());
                        } else {
                            antiAir.setDir(Target.Direction.NE);
                            antiAir.setX(antiAir.getX()+antiAir.getSpeed()*7/10);
                            antiAir.setY(antiAir.getY()-antiAir.getSpeed()*7/10);
                        }
                    } else {
                        if (d) {
                            antiAir.setDir(Target.Direction.N);
                            antiAir.setY(antiAir.getY()-antiAir.getSpeed());
                        } else {
                            antiAir.setDir(Target.Direction.NE);
                            antiAir.setX(antiAir.getX()+antiAir.getSpeed()*7/10);
                            antiAir.setY(antiAir.getY()-antiAir.getSpeed()*7/10);
                        }
                    }
                }
            } else {
                if (b) {
                    if (c) {
                        if (d) {
                            antiAir.setDir(Target.Direction.W);
                            antiAir.setX(antiAir.getX()-antiAir.getSpeed());
                        } else {
                            antiAir.setDir(Target.Direction.SW);
                            antiAir.setX(antiAir.getX()-antiAir.getSpeed()*7/10);
                            antiAir.setY(antiAir.getY()+antiAir.getSpeed()*7/10);
                        }
                    } else {
                        if (d) {
                            antiAir.setDir(Target.Direction.S);
                            antiAir.setY(antiAir.getY()+antiAir.getSpeed());
                        } else {
                            antiAir.setDir(Target.Direction.SW);
                            antiAir.setX(antiAir.getX()-antiAir.getSpeed()*7/10);
                            antiAir.setY(antiAir.getY()+antiAir.getSpeed()*7/10);
                        }
                    }
                } else {
                    if (c) {
                        if (d) {
                            antiAir.setDir(Target.Direction.W);
                            antiAir.setX(antiAir.getX()-antiAir.getSpeed());
                        } else {
                            antiAir.setDir(Target.Direction.NW);
                            antiAir.setX(antiAir.getX()-antiAir.getSpeed()*7/10);
                            antiAir.setY(antiAir.getY()-antiAir.getSpeed()*7/10);
                        }
                    } else {
                        if (d) {
                            antiAir.setDir(Target.Direction.N);
                            antiAir.setY(antiAir.getY()-antiAir.getSpeed());
                        } else {
                            antiAir.setDir(Target.Direction.NW);
                            antiAir.setX(antiAir.getX()-antiAir.getSpeed()*7/10);
                            antiAir.setY(antiAir.getY()-antiAir.getSpeed()*7/10);
                        }
                    }
                }
            }
        } else {
            antiAirStepList = getAirCraftPossiblePositions(antiAir);
            antiAirClosestStep = getAirCraftClosestPosition(antiAirStepList, enemyClosestStep);
            antiAir.setDir(antiAirClosestStep.getDir());
            antiAir.setX(antiAirClosestStep.getX());
            antiAir.setY(antiAirClosestStep.getY());
            switch(antiAir.getDir().toString()){
                case "N":
                    if (b) {
                        throw new AntiAirMissedException();
                    }
                    break;
                case "S":
                    if (!b) {
                        throw new AntiAirMissedException();
                    }
                    break;
                case "E":
                    if (!a) {
                        throw new AntiAirMissedException();
                    }
                    break;
                case "W":
                    if (a) {
                        throw new AntiAirMissedException();
                    }
                    break;
                case "NE":
                    if (!a & !b & c | !a & b | a & b & !c) {
                        throw new AntiAirMissedException();
                    }
                    break;
                case "NW":
                    if (a & !b & c | !a & b & !c | a & b) {
                        throw new AntiAirMissedException();
                    }
                    break;
                case "SE":
                    if (a & !b & !c | !a & !b | !a & b & c) {
                        throw new AntiAirMissedException();
                    }
                    break;
                case "SW":
                    if (a & !b | !a & !b & !c | a & b & c) {
                        throw new AntiAirMissedException();
                    }
                    break;
            }
        }
        if (Math.sqrt((antiAir.getX()-enemyX)*(antiAir.getX()-enemyX)
                +(antiAir.getY()-enemyY)*(antiAir.getY()-enemyY)) <= antiAir.getSpeed()/2) {
            throw new TargetDestroyedException();
        }
        
    }
    private Target getAirCraftClosestPosition(List<Target> tList, Target tr){
        Target targetClosestStep = null;
        for (Target t : tList) {
            if (targetClosestStep == null) {
                targetClosestStep = t;
            } else if(Math.sqrt((t.getX()-tr.getX())*(t.getX()-tr.getX())
                        +(t.getY()-tr.getY())*(t.getY()-tr.getY()))
                    < Math.sqrt((targetClosestStep.getX()-tr.getX())*(targetClosestStep.getX()-tr.getX())
                            +(targetClosestStep.getY()-tr.getY())*(targetClosestStep.getY()-tr.getY()))){
                targetClosestStep = t;
            }
        }
        return targetClosestStep;
    }
    private List<Target> getAirCraftPossiblePositions(Target enemy){
        List<Target> positionsList = new ArrayList<>();
        Target enemyStepForward = new Target(enemy.getType(), null, enemy.getCode(), enemy.getSpeed(), enemy.getX(), enemy.getY());
        Target enemyStepLeft = new Target(enemy.getType(), null, enemy.getCode(), enemy.getSpeed(), enemy.getX(), enemy.getY());
        Target enemyStepRight = new Target(enemy.getType(), null, enemy.getCode(), enemy.getSpeed(), enemy.getX(), enemy.getY());
        switch(enemy.getDir().toString()){
            case "N": 
                enemyStepForward.setDir(Target.Direction.N);
                enemyStepForward.setY(enemy.getY()-enemy.getSpeed());
                
                enemyStepLeft.setDir(Target.Direction.NW);
                enemyStepLeft.setX(enemy.getX()-enemy.getSpeed()*7/10);
                enemyStepLeft.setY(enemy.getY()-enemy.getSpeed()*7/10);
                
                enemyStepRight.setDir(Target.Direction.NE);
                enemyStepRight.setX(enemy.getX()+enemy.getSpeed()*7/10);
                enemyStepRight.setY(enemy.getY()-enemy.getSpeed()*7/10);
            break;
            case "NE": 
                enemyStepForward.setDir(Target.Direction.NE); 
                enemyStepForward.setX(enemy.getX()+enemy.getSpeed()*7/10);
                enemyStepForward.setY(enemy.getY()-enemy.getSpeed()*7/10);
                
                enemyStepLeft.setDir(Target.Direction.N);
                enemyStepLeft.setY(enemy.getY()-enemy.getSpeed());
                
                enemyStepRight.setDir(Target.Direction.E);
                enemyStepRight.setX(enemy.getX()+enemy.getSpeed());
            break;
            case "E":
                enemyStepForward.setDir(Target.Direction.E);
                enemyStepForward.setX(enemy.getX()+enemy.getSpeed());
                
                enemyStepLeft.setDir(Target.Direction.NE);
                enemyStepLeft.setX(enemy.getX()+enemy.getSpeed()*7/10);
                enemyStepLeft.setY(enemy.getY()-enemy.getSpeed()*7/10);
                
                enemyStepRight.setDir(Target.Direction.SE);
                enemyStepRight.setX(enemy.getX()+enemy.getSpeed()*7/10);
                enemyStepRight.setY(enemy.getY()+enemy.getSpeed()*7/10);
                break;
            case "SE":
                enemyStepForward.setDir(Target.Direction.SE); 
                enemyStepForward.setX(enemy.getX()+enemy.getSpeed()*7/10);
                enemyStepForward.setY(enemy.getY()+enemy.getSpeed()*7/10);
                
                enemyStepLeft.setDir(Target.Direction.E);
                enemyStepLeft.setX(enemy.getX()+enemy.getSpeed());
                
                enemyStepRight.setDir(Target.Direction.S);
                enemyStepRight.setY(enemy.getY()+enemy.getSpeed());
                break;
            case "S":
                enemyStepForward.setDir(Target.Direction.S);
                enemyStepForward.setY(enemy.getY()+enemy.getSpeed());
                
                enemyStepLeft.setDir(Target.Direction.SE);
                enemyStepLeft.setX(enemy.getX()+enemy.getSpeed()*7/10);
                enemyStepLeft.setY(enemy.getY()+enemy.getSpeed()*7/10);
                
                enemyStepRight.setDir(Target.Direction.SW);
                enemyStepRight.setX(enemy.getX()-enemy.getSpeed()*7/10);
                enemyStepRight.setY(enemy.getY()+enemy.getSpeed()*7/10);
                break;
            case "SW":
                enemyStepForward.setDir(Target.Direction.SW); 
                enemyStepForward.setX(enemy.getX()-enemy.getSpeed()*7/10);
                enemyStepForward.setY(enemy.getY()+enemy.getSpeed()*7/10);
                
                enemyStepLeft.setDir(Target.Direction.S);
                enemyStepLeft.setY(enemy.getY()+enemy.getSpeed());
                
                enemyStepRight.setDir(Target.Direction.W);
                enemyStepRight.setX(enemy.getX()-enemy.getSpeed());
                break;
            case "W":
                enemyStepForward.setDir(Target.Direction.W);
                enemyStepForward.setX(enemy.getX()-enemy.getSpeed());
                
                enemyStepLeft.setDir(Target.Direction.SW);
                enemyStepLeft.setX(enemy.getX()-enemy.getSpeed()*7/10);
                enemyStepLeft.setY(enemy.getY()+enemy.getSpeed()*7/10);
                
                enemyStepRight.setDir(Target.Direction.NW);
                enemyStepRight.setX(enemy.getX()-enemy.getSpeed()*7/10);
                enemyStepRight.setY(enemy.getY()-enemy.getSpeed()*7/10);
                break;
            case "NW":
                enemyStepForward.setDir(Target.Direction.NW); 
                enemyStepForward.setX(enemy.getX()-enemy.getSpeed()*7/10);
                enemyStepForward.setY(enemy.getY()-enemy.getSpeed()*7/10);
                
                enemyStepLeft.setDir(Target.Direction.W);
                enemyStepLeft.setX(enemy.getX()-enemy.getSpeed());
                
                enemyStepRight.setDir(Target.Direction.N);
                enemyStepRight.setY(enemy.getY()-enemy.getSpeed());
                break;
        }
        positionsList.add(enemyStepForward);
        positionsList.add(enemyStepLeft);
        positionsList.add(enemyStepRight);
        return  positionsList;
    }
}
