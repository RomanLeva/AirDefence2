package airdefence;

/**
 *
 * @author user
 */
//Class for both radar and headqarters applications
public class Target {
    enum Type {FIGHTER, TRANSPORT, MISSILE, WARHEAD, ANTIAIR}
    enum Direction{N,S,E,W,NE,NW,SE,SW}
    
    private Type type;
    private Direction dir;
    private int speed, x, y;
    private String code;
    
    public Target(Type type, Direction dir, String code, int speed, int x, int y) {
        this.type=type;
        this.dir=dir;
        this.code=code;
        this.speed=speed;
        this.x = x;
        this.y = y;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public void setDir(Direction dir) {
        this.dir = dir;
    }

    public void setSpeed(int speed) {
        if (speed > 30) {
            this.speed = 30;
        }
        this.speed = speed;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setX(int x) {
            this.x = x;
    }

    public void setY(int y) {
            this.y = y;
    }

    public Type getType() {
        return type;
    }

    public Direction getDir() {
        return dir;
    }

    public int getSpeed() {
        return speed;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public String getCode() {
        return code;
    }
    
    @Override
    public String toString(){
        return type.toString()+"/"+code+"/"+dir.toString()+"/"+speed+"/"+x+"/"+y;
    }
}
