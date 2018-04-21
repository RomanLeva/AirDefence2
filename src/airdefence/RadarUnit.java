package airdefence;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.time.LocalTime;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public class RadarUnit implements Observer{
    private static final Logger logger = Logger.getLogger(HeadQuarters.class.getName());
    private TargetGeneratorAndMover generatorAndMover;
    private AntiAirMover antiAirMover;
    private volatile Map<Target, Target> target_antiair_Map;
    private SocketChannel sc;
    private int xr, yr;
    private Lock sendingLock = new ReentrantLock();
    private Clip clipRocket, clipExpl;
    private Target wraith;
    static {
        logger.addHandler(AirDefence.fileHandler);
    }
    
    public RadarUnit(AircraftLogic targetMover, AircraftLogic antiAirMover, int x, int y){
        generatorAndMover = (TargetGeneratorAndMover) targetMover;
        this.antiAirMover = (AntiAirMover) antiAirMover;
        target_antiair_Map = new ConcurrentHashMap<>();
        xr = x;
        yr = y;
        wraith = new Target(Target.Type.NONE, Target.Direction.NONE, "", 0, 0, 0);
        try {
            sc = SocketChannel.open();
            sc.connect(new InetSocketAddress("127.0.0.1", 5000)); //localhost connection
            sc.configureBlocking(false);
            clipRocket = AudioSystem.getClip();
            clipRocket.open(AudioSystem.getAudioInputStream(this.getClass().getClassLoader().getResource("sound/rocket.wav")));
            clipExpl = AudioSystem.getClip();
            clipExpl.open(AudioSystem.getAudioInputStream(this.getClass().getClassLoader().getResource("sound/explosion.wav")));
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException | NullPointerException ex) {
            logger.log(Level.FINE, ex.getMessage());
        }
    }
    @Override
    public void beginRadarSimulation(){
        CharsetDecoder decoder = Charset.forName("ISO-8859-1").newDecoder();
        ByteBuffer commandBuf = ByteBuffer.allocate(10000);
        StringBuilder sbO = new StringBuilder();
        StringBuilder sbF = new StringBuilder();
        ScheduledExecutorService observerExecutor = Executors.newSingleThreadScheduledExecutor();
        ScheduledExecutorService followingExecutor = Executors.newSingleThreadScheduledExecutor();
        String radarPosition = xr +"/"+yr;

        generatorAndMover.beginGeneratingAndMoving();
        observerExecutor.scheduleAtFixedRate(() -> {
            if (!generatorAndMover.targetsList.isEmpty()) {
                sbO.append("RADAR/".concat(radarPosition)).append("\n");
                generatorAndMover.targetsList.stream()
                    .filter((f) -> !f.getCode().contains("ACQ") | f.getCode().contains("DST")).forEach((t) -> {
                if (Math.sqrt((xr-t.getX())*(xr-t.getX())+(yr-t.getY())*(yr-t.getY())) <= 200 && target_antiair_Map.size() < 3 & canFire(t)
                        & !target_antiair_Map.containsKey(t)) {
                    t.setCode("ACQ:"+target_antiair_Map.size()+1);
                    target_antiair_Map.put(t, new Target(Target.Type.ANTIAIR, Target.Direction.NONE, "AAM", 26, xr, yr));
                } else { 
                    sbO.append(t.toString()).append("/").append(LocalTime.now()).append("\n");
                }
            });
            sendData(sbO);
            }
        }, 0, 4001, TimeUnit.MILLISECONDS);
        
        followingExecutor.scheduleAtFixedRate(()->{
            if(!target_antiair_Map.isEmpty()) {
                sbF.append("FOLOW/".concat(radarPosition)).append("\n");
                target_antiair_Map.entrySet().stream().forEach((e)->{
                    Target t = e.getKey();
                    Target a = e.getValue();
                    try {
                        int xt = t.getX(), yt = t.getY();
                        if (Math.sqrt((xr-xt)*(xr-xt)+(yr-yt)*(yr-yt)) <= 200) {
                            if(a.getDir() != Target.Direction.NONE){
                                antiAirMover.moveRocketToEnemy(a, t, false);
                                sbF.append(t.toString()).append("/").append(LocalTime.now()).append("\n");
                                sbF.append(a.toString()).append("/").append(LocalTime.now()).append("\n");
                            } else {
                                sbF.append(t.toString()).append("/").append(LocalTime.now()).append("\n");
                            }
                        } else {
                            t.setCode("OUT");
                            target_antiair_Map.remove(t);
                        }
                    } catch(TargetDestroyedException ex){
                        play(clipExpl);
                        t.setCode("DST");
                        target_antiair_Map.remove(t);
                        generatorAndMover.targetsList.remove(t);
                        sbF.append(t.toString()).append("/").append(LocalTime.now()).append("\n");
                    } catch(AntiAirMissedException ex){
                        t.setCode("MST");
                        sbF.append(t.toString()).append("/").append(LocalTime.now()).append("\n");
                        sbF.append(a.toString()).append("/").append(LocalTime.now()).append("\n");
                    }
                });
                try{
                    sendData(sbF);
                    Thread.sleep(200);
                    sc.read(commandBuf);
                    commandBuf.flip();
                    String commandData = decoder.decode(commandBuf).toString();
                    commandBuf.clear();
                    if (!commandData.isEmpty()) {
                        switch(commandData.substring(0, 4)){
                            case "FIRE":
                                StringTokenizer tokenizer = new StringTokenizer(commandData, ":", false);
                                while (tokenizer.hasMoreElements()) {
                                    tokenizer.nextToken(); //skip command code
                                    String s = tokenizer.nextToken();
                                    target_antiair_Map.keySet().stream().forEach((t)->{
                                        try{
                                            if (t.getCode().contains(s)) {
                                                t.setCode("ACQ:0");
                                                antiAirMover.moveRocketToEnemy(target_antiair_Map.get(t), t, true);
                                                play(clipRocket);
                                            }
                                        }catch(TargetDestroyedException te){
                                            play(clipExpl);
                                            target_antiair_Map.remove(t);
                                            generatorAndMover.targetsList.remove(t);
                                            t.setCode("DST");
                                            sbF.append(t.toString()).append("/").append(LocalTime.now()).append("\n");
                                            sendData(sbF);
                                        }catch(AntiAirMissedException ex){
                                            //do nothing
                                        }
                                    });
                                }
                                break;
                            case "STOP":
                                break;
                        }
                    }
                }catch(InterruptedException | IOException ex){
                    logger.log(Level.FINE, ex.getMessage());
                }
            }
        }, 0, 1000, TimeUnit.MILLISECONDS);
    }
    private void sendData(StringBuilder sb){
        sendingLock.lock();
        try {
            ByteBuffer dataBuf = ByteBuffer.wrap(sb.toString().getBytes());
            sc.write(dataBuf);
            dataBuf.clear();
            sb.setLength(0);
        } catch (IOException ex) {
            logger.log(Level.FINE, ex.getMessage());
        } finally {
            sendingLock.unlock();
        }
    }
    private boolean canFire(Target t){
        wraith.setDir(t.getDir());
        wraith.setSpeed(t.getSpeed());
        wraith.setX(t.getX());
        wraith.setY(t.getY());
        int distance = 0;
        while (Math.sqrt((xr-wraith.getX())*(xr-wraith.getX())+(yr-wraith.getY())*(yr-wraith.getY())) <= 200) {            
            switch(wraith.getDir().toString()){
                case "N":
                    wraith.setY(wraith.getY()-wraith.getSpeed());
                    break;
                case "S":
                    wraith.setY(wraith.getY()+wraith.getSpeed());
                    break;
                case "E":
                    wraith.setX(wraith.getX()+wraith.getSpeed());
                    break;
                case "W":
                    wraith.setX(wraith.getX()-wraith.getSpeed());
                    break;
                case "NE":
                    wraith.setX(wraith.getX()+wraith.getSpeed()*7/10);
                    wraith.setY(wraith.getY()-wraith.getSpeed()*7/10);
                    break;
                case "NW":
                    wraith.setX(wraith.getX()-wraith.getSpeed()*7/10);
                    wraith.setY(wraith.getY()-wraith.getSpeed()*7/10);
                    break;
                case "SE":
                    wraith.setX(wraith.getX()+wraith.getSpeed()*7/10);
                    wraith.setY(wraith.getY()+wraith.getSpeed()*7/10);
                    break;
                case "SW":
                    wraith.setX(wraith.getX()-wraith.getSpeed()*7/10);
                    wraith.setY(wraith.getY()+wraith.getSpeed()*7/10);
                    break;
            }
            distance += wraith.getSpeed();
        }
        return distance/wraith.getSpeed() >= Math.PI*200/2/25;
    }
    private void play(Clip clip){
        if (clip.isRunning()) {
            clip.stop();   // Stop the player if it is still running
        }
         clip.setFramePosition(0); // rewind to the beginning
         clip.start();     // Start playing
    }
}
