package airdefence;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TargetGeneratorAndMover implements AircraftLogic{
    private final ScheduledExecutorService scheduledExecutorService;
    List<Target> targetsList;
    private final Random r;
    private boolean working = false;

    public TargetGeneratorAndMover() {
        targetsList = new CopyOnWriteArrayList<>();
        r = new Random();
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    }
    void beginGeneratingAndMoving(){
        synchronized(this){
            if (working) {
                return;
            }
            working = true;
        }
        scheduledExecutorService.scheduleAtFixedRate(() -> {
                Target.Type[] ta = Target.Type.values();
                Target.Direction[] da = Target.Direction.values();
                int type = r.nextInt(4), dir = r.nextInt(8), sp = makeRandomInteger(15, 20),
                    x = makeRandomInteger(50, 750), y = makeRandomInteger(50, 750);
                Target target = new Target(ta[type], da[dir], "OUT", sp, x, y);
                targetsList.add(target);
        }, 0, 10, TimeUnit.SECONDS);
        
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            targetsList.stream().forEach((target)->{
                if (target.getX()<=0 | target.getX()>=800 | target.getY()<=0 | target.getY()>=800
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

}
