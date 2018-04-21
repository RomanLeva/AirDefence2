package airdefence;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.Initializable;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public class HeadQuarters {
    private static final Logger logger = Logger.getLogger(HeadQuarters.class.getName());
    private HeadQuartersController mainScreen;
    private final Listener listener;
    private final Map<String, SocketChannel> radarChannelMap;
    private final Map <String, Integer> targetsMap;
    private final Map<String, List<Target>> radarAcqiredMap;
    private Clip clipRadar;
    static {
        logger.addHandler(AirDefence.fileHandler);
    }

    public <T extends Listener> HeadQuarters(T listener) {
        this.listener = listener;
        radarChannelMap = new ConcurrentHashMap<>();
        targetsMap = new ConcurrentHashMap<>();
        radarAcqiredMap = new ConcurrentHashMap<>();

        try {
            clipRadar = AudioSystem.getClip();
            clipRadar.open(AudioSystem.getAudioInputStream(this.getClass().getClassLoader().getResource("sound/radar.wav")));
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException | NullPointerException ex) {
            logger.log(Level.FINE, ex.getMessage());
        }
    }
    void beginSimulation(){
        listener.beginListening(this);
        ScheduledExecutorService targetPrinter = Executors.newSingleThreadScheduledExecutor();
        targetPrinter.scheduleAtFixedRate(()->{
            if (!targetsMap.isEmpty()) {
                targetsMap.entrySet().forEach((entry)->{
                    if (entry.getKey().contains("ACQ") || entry.getKey().contains("DST") || entry.getKey().contains("MST")) {
                        switch(entry.getValue()){ //entry is acquired target
                            case 1000:
                                mainScreen.draw(entry.getKey());
                                entry.setValue(entry.getValue() - 500);
                                break;
                            case 500:
                                mainScreen.delete(entry.getKey());
                                targetsMap.remove(entry.getKey());
                                break;
                        }
                    } else { //entry is "OUT" target
                        switch(entry.getValue()){
                            case 4000:
                                play(clipRadar);
                                mainScreen.draw(entry.getKey());
                                entry.setValue(entry.getValue() - 500);
                                break;
                            case 0:
                                mainScreen.delete(entry.getKey());
                                targetsMap.remove(entry.getKey());
                                break;
                            default:
                                entry.setValue(entry.getValue() - 500);
                        }
                    }
                });
            }
        }, 0, 500, TimeUnit.MILLISECONDS);
    }
    @SuppressWarnings("fallthrough")
    void pushData(String data, SocketChannel ch){
        StringTokenizer tokenizer1 = new StringTokenizer(data, "\n");
        StringTokenizer tokenizer2 = new StringTokenizer(tokenizer1.nextToken(), "/");
        String report = tokenizer2.nextToken();
        String radarPosition = tokenizer2.nextToken().concat("/").concat(tokenizer2.nextToken());
        switch(report){
            case "RADAR":
                targetsMap.put(data, 4000);
                if (!radarChannelMap.containsKey(radarPosition)) {
                    radarChannelMap.put(radarPosition, ch);
                    radarAcqiredMap.put(radarPosition, new CopyOnWriteArrayList<>());
                    radarAcqiredMap.get(radarPosition).add(new Target(Target.Type.NONE, Target.Direction.NONE, "", 0, 0, 0));
                    radarAcqiredMap.get(radarPosition).add(new Target(Target.Type.NONE, Target.Direction.NONE, "", 0, 0, 0));
                    radarAcqiredMap.get(radarPosition).add(new Target(Target.Type.NONE, Target.Direction.NONE, "", 0, 0, 0));
                }
                break;
            case "FOLOW": 
                targetsMap.put(data, 1000);
                while(tokenizer1.hasMoreTokens()) {
                    tokenizer2 = new StringTokenizer(tokenizer1.nextToken(), "/");
                    //пропускаем не нужные строки
                    tokenizer2.nextToken();
                    String code = tokenizer2.nextToken();
                    tokenizer2.nextToken();
                    tokenizer2.nextToken();
                    tokenizer2.nextToken();
                    tokenizer2.nextToken();
                    int targetN = 0;
                    tokenizer2 = new StringTokenizer(code, ":", false);
                    tokenizer2.nextToken();
                    if (tokenizer2.hasMoreTokens()) {
                        targetN = Integer.parseInt(tokenizer2.nextToken());
                    }
                    if (targetN != 0) {
                        for(Entry<String, SocketChannel> e : radarChannelMap.entrySet()){
                            if (e.getKey().equals(radarPosition)) {
                                try {
                                    ByteBuffer buf = ByteBuffer.wrap(("FIRE:"+targetN).getBytes());
                                    e.getValue().write(buf);
                                } catch (IOException ex) {
                                    logger.log(Level.FINE, ex.getMessage());
                                }
                                break;
                            }
                        }
                    }
                }
            break;
        }
    }
    private void play(Clip clip){
        if (clip.isRunning()) {
            clip.stop();
        }
        clip.setFramePosition(0);
        clip.start();
    }
    void setScreen(Initializable i){
        mainScreen = (HeadQuartersController)i;
    }
}
