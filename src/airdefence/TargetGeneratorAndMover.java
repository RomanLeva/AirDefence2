package airdefence;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author user
 */
public class TargetGeneratorAndMover implements AircraftLogic{
    private final Random r;
    private final ScheduledExecutorService scheduledExecutorService;
    private final List<RadarUnit> observList;
    protected final List<Target> targetsList;
    private final int width = 800;
    private final int height = 800;
    private boolean working = false;

    public TargetGeneratorAndMover() {
        observList = new ArrayList<>();
        targetsList = new CopyOnWriteArrayList<>();
        r = new Random();
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    }
    public void beginGeneratingAndMoving(){
        synchronized(this){
            if (working==true) {
                return;
            }
            working = true;
        }
        scheduledExecutorService.scheduleAtFixedRate(() -> {
                Target.Type[] ta = Target.Type.values();
                Target.Direction[] da = Target.Direction.values();
                int type = r.nextInt(4), dir = r.nextInt(8), sp = makeRandomInteger(15, 20), 
                    x = makeRandomInteger(50, width-50), y = makeRandomInteger(50, height-50);
                Target target = new Target(ta[type], da[dir], "OUT", sp, x, y);
//                Target target1 = new Target(ta[type], Target.Direction.NW, "OUT", sp, 780, 700);
//                Target target2 = new Target(ta[type], Target.Direction.SW, "OUT", sp, 700, 200);
//                Target target3 = new Target(ta[type], Target.Direction.NE, "OUT", sp, 230, 710);
//                Target target4 = new Target(ta[type], Target.Direction.SE, "OUT", sp, 180, 180);
//                targetsList.add(target1);
//                targetsList.add(target2);
//                targetsList.add(target3);
//                targetsList.add(target4);
                targetsList.add(target);
        }, 0, 10, TimeUnit.SECONDS);
        
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            targetsList.stream().forEach((target)->{
                if (target.getX()<=0 | target.getX()>=width | target.getY()<=0 | target.getY()>=height 
                        | target.getCode().equals("DST")) {
                    targetsList.remove(target);
                } else {
                    switch(target.getDir()){
                        case S: target.setY(target.getY()+ target.getSpeed()); break;
                        case N: target.setY(target.getY() - target.getSpeed()); break;
                        case E: target.setX(target.getX() + target.getSpeed()); break;
                        case W: target.setX(target.getX() - target.getSpeed()); break;
                        case NE: target.setX((target.getX() + target.getSpeed() * 7 / 10));
                            target.setY((target.getY() - target.getSpeed() * 7 / 10)); break;
                        case NW: target.setX((target.getX() - target.getSpeed() * 7 / 10));
                            target.setY((target.getY() - target.getSpeed() * 7 / 10)); break;
                        case SW: target.setX((target.getX() - target.getSpeed() * 7 / 10));
                            target.setY((target.getY() + target.getSpeed() * 7 / 10)); break;
                        case SE: target.setX((target.getX() + target.getSpeed() * 7 / 10));
                            target.setY((target.getY() + target.getSpeed() * 7 / 10)); break;
                    }
                }
            });
        }, 0, 1, TimeUnit.SECONDS);
    }
    private int makeRandomInteger(int min, int max){
        return r.nextInt((max-min)+1) + min;
    }
    protected void addObserver(Observer ob) {
        observList.add((RadarUnit) ob);
    }
}
