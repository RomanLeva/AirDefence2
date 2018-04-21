package airdefence;
//Class for both radar and headqarters applications
public class Target {
    enum Type {FIGHTER, TRANSPORT, MISSILE, WARHEAD, ANTIAIR, NONE}
    enum Direction{N,S,E,W,NE,NW,SE,SW,NONE}
    
    private Type type;
    private Direction dir;
    private int speed, x, y;
    private String code;
    
    Target(Type type, Direction dir, String code, int speed, int x, int y) {
        this.type=type;
        this.dir=dir;
        this.code=code;
        this.speed=speed;
        this.x = x;
        this.y = y;
    }

    void setDir(Direction dir) {
        this.dir = dir;
    }

    void setSpeed(int speed) {
        if (speed > 30) {
            this.speed = 30;
        }
        this.speed = speed;
    }

    void setCode(String code) {
        this.code = code;
    }

    void setX(int x) {
            this.x = x;
    }

    void setY(int y) {
            this.y = y;
    }

    Type getType() {
        return type;
    }

    Direction getDir() {
        return dir;
    }

    int getSpeed() {
        return speed;
    }

    int getX() {
        return x;
    }

    int getY() {
        return y;
    }

    String getCode() {
        return code;
    }
    
    @Override
    public String toString(){
        return type.toString()+"/"+code+"/"+dir.toString()+"/"+speed+"/"+x+"/"+y;
    }
}
