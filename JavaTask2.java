import java.io.*;
import java.net.InetSocketAddress;
import java.util.*;
import java.nio.channels.*;
import java.nio.ByteBuffer;


public class JavaTask2 {
    private final Selector selector;
    private final ServerSocketChannel ssc;
    private byte[] buffer = new byte[2048];
    
    Map<SelectionKey, ByteBuffer> connections = new HashMap<SelectionKey, ByteBuffer>();
    
    public static void main(String[] args) throws IOException {
        new JavaTask2(1234).run();
    }
    
    
    public JavaTask2(int port) throws IOException { 
        ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false);
        ssc.socket().bind(new InetSocketAddress(port));
        selector = Selector.open(); 
        ssc.register(selector, SelectionKey.OP_ACCEPT);
    }
 
    public void run() {
        while (true) {
            try {
                if (selector.isOpen()) {
                    selector.select(1000);
                    Set<SelectionKey> keys = selector.selectedKeys();
                    for (SelectionKey sk:keys) {
	
                        if (!sk.isValid()) {
                            continue;
                        }


                        if (sk.isAcceptable()) { // Добавление нового коннекта
	                        ServerSocketChannel ssca = (ServerSocketChannel)sk.channel();
                            SocketChannel sc = ssca.accept();
                            sc.configureBlocking(false);
                        
                            SelectionKey skr = sc.register(selector, SelectionKey.OP_READ);
                            ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
                            connections.put(skr, byteBuffer);
                        
						} 
						else if (sk.isReadable()) { // Читаем
                            SocketChannel socketChannel= (SocketChannel)sk.channel();
                            int read;
                            ByteBuffer byteBuffer = connections.get(sk);

                            byteBuffer.clear();
                            try {
                                read = socketChannel.read(byteBuffer); 
                            } 
							 catch (IOException e) {
                                closeChannel(sk);
                                break;
                            }
                            
 							 if (read == -1) {
                                closeChannel(sk);
                                break;
                            } 
							 else if (read > 0) {
                                byteBuffer.flip(); 
                                byteBuffer.mark(); 
                                final int pos = byteBuffer.position(); 
                                final int lim = byteBuffer.limit();

                                Set <Map.Entry<SelectionKey, ByteBuffer>> entries = connections.entrySet();
                                for (Map.Entry<SelectionKey, ByteBuffer> entry: entries) { 
                                    SelectionKey selectionKey = entry.getKey();
                                    selectionKey.interestOps(SelectionKey.OP_WRITE);
                                    ByteBuffer entryBuffer = entry.getValue();
                                    entryBuffer.position(pos); 
                                    entryBuffer.limit(lim);
                                }
                            }
                        
						} 
						else if (sk.isWritable()) { // Пишим
                            ByteBuffer bb = connections.get(sk); 
                            SocketChannel s = (SocketChannel)sk.channel(); 

                            try {
                                int result = s.write(bb); 
                                if (result == -1) { 
                                    closeChannel(sk);
                                }
                            } 
							 catch (IOException e2) { 
                                closeChannel(sk);
                            }

                            if (bb.position() == bb.limit()) { 
                                sk.interestOps(SelectionKey.OP_READ);
                            }
                        }
                    }
                    keys.clear();
                } 
				else break;
				
            } 
			catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
   
    private void closeChannel(SelectionKey sk) throws IOException {
        connections.remove(sk);
        SocketChannel socketChannel = (SocketChannel)sk.channel();
       
	    if (socketChannel.isConnected()) {
            socketChannel.close();
        }
       
        sk.cancel(); 
    }
}
