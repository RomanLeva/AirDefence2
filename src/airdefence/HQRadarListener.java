package airdefence;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author user
 */
public class HQRadarListener implements Listener{
    private static final Logger logger = Logger.getLogger(HQRadarListener.class.getName());
    private boolean ready = false;
    private HeadQuarters hq;
    private Selector selector;
    private ServerSocketChannel ssc;
    private Queue<SocketChannel> channels;
    private ByteBuffer buf;
    static {
        logger.addHandler(AirDefence.fileHandler);
    }
    
    public HQRadarListener() {
        try {
            selector = Selector.open();
            ssc = ServerSocketChannel.open().bind(new InetSocketAddress(5000));
            ssc.configureBlocking(false);
            ssc.register(selector, SelectionKey.OP_ACCEPT);
            buf = ByteBuffer.allocate(5000);
        } catch (IOException ex) {
            logger.log(Level.FINE, ex.getMessage());
        }
    }
    @Override
    public void beginListening(HeadQuarters hq) {
        new Thread(() -> {
            ready = true;
            Set<SelectionKey> keySet;
            Iterator<SelectionKey> iterator;
            CharsetDecoder decoder = Charset.forName("ISO-8859-1").newDecoder();
            try {
                while (true) {  
                    if (selector.selectNow() == 0) {
                        continue;
                    } else {
                        keySet = selector.selectedKeys();
                        iterator = keySet.iterator();
                        while(iterator.hasNext()){
                            SelectionKey sk = iterator.next();
                            if ((sk.readyOps() & SelectionKey.OP_READ) == SelectionKey.OP_READ) {
                                SocketChannel sc = (SocketChannel)sk.channel();
                                sc.configureBlocking(false);
                                sc.read(buf);
                                buf.flip();
                                String data = decoder.decode(buf).toString();
                                buf.clear();
                                hq.pushData(data, sc);
                            }
                            else if ((sk.readyOps() & SelectionKey.OP_ACCEPT) == SelectionKey.OP_ACCEPT) {
                                ServerSocketChannel sscN = (ServerSocketChannel)sk.channel();
                                SocketChannel sc = sscN.accept();
                                sc.configureBlocking(false);
                                sc.register(selector, SelectionKey.OP_READ);
                            }
                        }
                    }
                    keySet.clear();
                }
            } catch (IOException | NullPointerException ex) {
                logger.log(Level.FINE, ex.getMessage());
            }
            finally{
                try {
                    ssc.close();
                    selector.close();
                } catch (IOException ex) {
                    logger.log(Level.FINE, ex.getMessage());
                }
            }
        }).start();
    }
    @Override
    public boolean isReady(){
        return ready;
    }
}
