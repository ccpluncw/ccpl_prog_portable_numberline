/*
  JarEntryDataSource.java - (c) 2002 Chris Adamson, invalidname@mac.com
  http://homepage.mac.com/invalidname/spmovie/
  This source is released under terms of the GNU Public License
  http://www.gnu.org/licenses/gpl.html
*/
package ccpl.lib.feedback;

import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import javax.media.MediaLocator;
import javax.media.Time;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.DataSource;
import javax.media.protocol.PullDataSource;
import javax.media.protocol.PullSourceStream;
import javax.media.protocol.Seekable;
import javax.media.protocol.SourceStream;

/** A class to provide a DataSource to an entry within a
    jar file, perhaps found with ClassLoader.getResource().
    Basically, this class exists to provide PullSourceStreams
    that do little more than wrap the input stream derived
    from the jar entry.
 */
public class JarEntryDataSource extends PullDataSource {

    protected static Object[] EMPTY_OBJECT_ARRAY = {};

    protected JarEntryPullStream jarIn;
    protected PullSourceStream[] sourceStreams;

    public JarEntryDataSource (MediaLocator ml)
        throws IllegalArgumentException, IOException {
        super();
        setLocator (ml);
    }

    protected void createJarIn() throws IOException {
        jarIn = new JarEntryPullStream();
        sourceStreams = new PullSourceStream[1];
        sourceStreams[0] = jarIn;
    }

    public void setLocator (MediaLocator ml) 
        throws IllegalArgumentException {
        // System.out.println ("JarEntryDataSource.setLocator()");
        if (! ml.getProtocol().equals("jar"))
            throw new IllegalArgumentException ("Not a jar:-style URL: " +
                                                ml.toString());
        super.setLocator (ml);
        try {
            createJarIn();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }


    // -- spec'ed by PullDataSource
    public PullSourceStream[] getStreams() {
        return sourceStreams;
    }

    // -- spec'ed by DataSource
    public void connect() throws IOException {
        //System.out.println ("JarEntryDataSource.connect()");
    }

    public void disconnect() {
        if (jarIn != null) {
            jarIn.close();
            jarIn = null;
        }
    }


    /** A pretty bare-bones implementation, only supports
        <code>video.quicktime</code>, <code>video.mpeg</code>
        and <code>video.x_msvideo</code>, based solely on
        filename extension.
     */
    public String getContentType() {
        try {
            URL url = getLocator().getURL();
            String urlFile = url.getFile();
            if (urlFile.endsWith(".mov"))
                return "video.quicktime";
            else if (urlFile.endsWith(".mpg"))
                return "video.mpeg";
            else if (urlFile.endsWith(".avi"))
                // this is a cheat to help PlaybackEngine find
                // a suitable DEMULTIPLEXER plug-in -- normally, we'd
                // use "video.x-msvideo", but ContentDescriptor doesn't
                // transliterate '-' to '_' when source is in jar
                return "video.x_msvideo";
            else
                return "unknown";
        } catch (MalformedURLException murle) {
            return "unknown";
        }
    }

    public void start() {
        // nothing to do
    }

    public void stop() {
        // nothing to do
    }

    // -- spec'ed by Duration
    public Time getDuration () {
        // TODO: real implementation?
        return DataSource.DURATION_UNKNOWN;
    }

    // -- spec'ed by Controls
    public Object getControl(String controlName) {
        return null;
    }

    public Object[] getControls() {
        return EMPTY_OBJECT_ARRAY;
    }


    class JarEntryPullStream extends Object
        implements PullSourceStream, Seekable {

        protected InputStream in;
        protected ContentDescriptor unknownCD =
            new ContentDescriptor ("unknown");
        protected long tellPoint;

        public JarEntryPullStream()
            throws IOException {
            open();
        }

        public void open() throws IOException {
            // get input stream from the jar file specified by 
            // the MediaLocator (had BETTER be a file within jar!)
            URL url = getLocator().getURL();
            JarURLConnection conn = 
                (JarURLConnection) url.openConnection();
            in = conn.getInputStream();
            tellPoint = 0;
        }

        // just being tidy
        public void close() {
            try {
                in.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }

        public void thoroughSkip (long skipCount) 
            throws IOException {
            long totalSkipped = 0;
            while (totalSkipped < skipCount) {
                long skipped =
                    in.skip (skipCount-totalSkipped);
                totalSkipped += skipped;
                tellPoint += skipped;
            }
        }

        // spec'ed by PullSourceStream

        public int read (byte[] buf, int off, int length) 
            throws IOException {
            int bytesRead = in.read(buf, off, length);
            tellPoint += bytesRead;
            return bytesRead;
        }

        public boolean willReadBlock() {
            try {
                return (in.available() > 0);
            } catch (IOException ioe) {
                return true;
            }
        }

        // spec'ed by SourceStream

        public long getContentLength() {
            // TODO: implement - maybe can use jar.Attributes
            return SourceStream.LENGTH_UNKNOWN;
        }

        public boolean endOfStream() {
            try {
                return (in.available() == -1);
            } catch (IOException ioe) {
                return true;
            }
        }

        public ContentDescriptor getContentDescriptor() {
            return unknownCD;
        }

        // spec'ed by Controls
        public Object getControl (String controlType) {
            return null;
        }

        public Object[] getControls() {
            return EMPTY_OBJECT_ARRAY;
        }


        // spec'ed by Seekable
        public boolean isRandomAccess() {
            return true;
        }

        public long seek (long position) {
            // approach -- if seek is further in than tell,
            // then just skip bytes to get there
            // else close, reopen, and skip to position
            try {
                if (position > tellPoint) {
                    thoroughSkip (position - tellPoint);
                } else {
                    close();
                    open();
                    // now skip to this position
                    thoroughSkip (position);
                }
                return tellPoint;
            } catch (IOException ioe) {
                return 0; // bogus... who even knows where we are now?
            }
        }
        
        public long tell() {
            return tellPoint;
        }
    }

}
