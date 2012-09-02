
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.concurrent.TimeoutException;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import com.v2soft.styxlib.library.StyxServerManager;
import com.v2soft.styxlib.library.exceptions.StyxException;
import com.v2soft.styxlib.library.server.vfs.DiskStyxDirectory;
import com.v2soft.styxlib.library.server.vfs.MemoryStyxDirectory;
import com.v2soft.styxlib.library.server.vfs.MemoryStyxFile;

/**
 * 
 * @author V.Shcriyabets (vshcryabets@gmail.com)
 *
 */
public class JavaServerTest {
    private static SSLContext getSSLContext(String keyStorePath, String keyStorePassword, String trustStorePath, String trustStorePassword )
            throws NoSuchAlgorithmException, KeyStoreException, CertificateException, FileNotFoundException, 
            IOException, UnrecoverableKeyException, KeyManagementException {
        KeyStore ks = null;
        KeyStore ts = null;
        KeyManagerFactory keyManagerFactory = null;
        TrustManagerFactory trustManagerFactory = null;

        // load key store
        if ( keyStorePath != null ) {
            keyManagerFactory = 
                    KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            ks = KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(new FileInputStream(keyStorePath), keyStorePassword.toCharArray());
            keyManagerFactory.init(ks, keyStorePassword.toCharArray());
        }

        //      load trust keys store
        if ( trustStorePath != null ) {
            trustManagerFactory = 
                    TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            ts = KeyStore.getInstance(KeyStore.getDefaultType());
            ts.load(new FileInputStream(trustStorePath), trustStorePassword.toCharArray());
            trustManagerFactory.init(ts);
        }

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init( (keyManagerFactory == null ? null : keyManagerFactory.getKeyManagers()), 
                (trustManagerFactory == null ? null : trustManagerFactory.getTrustManagers()), 
                new SecureRandom());
        return sslContext;
    }

    public static void main(String[] args) throws IOException, StyxException, InterruptedException, TimeoutException {
        try {
            String keyStorePath = "/Users/user/Dropbox/private/git/SSLServer/serverkey.jks";
            String keyStorePassword = "123456";

            SSLContext sslContext = getSSLContext(keyStorePath, keyStorePassword, null, null);
            
            File testDirectory = new File("./test");
            if ( !testDirectory.exists() ) {
                testDirectory.mkdirs();
            }
            DiskStyxDirectory root = new DiskStyxDirectory(testDirectory);
            MemoryStyxDirectory dir = new MemoryStyxDirectory("vdir1"); 
            root.addFile(dir);
            dir = new MemoryStyxDirectory("vdir2");
            root.addFile(dir);
            dir.addFile(new MemoryStyxFile("vfile1"));
            dir.addFile(new MemoryStyxFile("vfile2"));
            dir.addFile(new MemoryStyxFile("vfile3"));
            dir.addFile(new MemoryStyxFile("vfile4"));
            StyxServerManager manager = new StyxServerManager(
                    InetAddress.getByName("127.0.0.1"), 
                    8080, 
                    sslContext,
                    root,
                    StyxServerManager.PROTOCOL);
            manager.start();
            while ( true ) {
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}