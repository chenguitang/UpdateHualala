package com.posin.updatehualala.update;

import android.content.Context;

import com.posin.updatehualala.R;
import com.posin.updatehualala.utils.TelnetSuProc;
import com.posin.updatehualala.utils.Zip;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;


/**
 * FileName: Installer
 * Author: Greetty
 * Time: 2018/6/1 19:47
 * Desc: TODO
 */
public abstract class Installer extends MyThread {

    private static final String TAG = "Update.Installer";

    private final Context mContext;
    private final File mPkgFile;
    private File mExtractPath;
    //private BufferedReader mReader = null;
    private File mScript;

    protected abstract void onStart();

    protected abstract void onSuccess();

    protected abstract void onStop();

    protected abstract void onError(String text);

    protected abstract void onAddItem(String text);

    protected abstract void onAddLog(String text);

    public Installer(Context context, File ppk) {
        super("UpdateInstaller", null);

        mContext = context;
        mPkgFile = ppk;

        File storage = new File("/mnt/sdcard");
        mScript = new File(storage, "update.sh");
        mExtractPath = new File(storage, getFileName(mPkgFile));
    }

    private static String getFileName(File f) {
        String n = f.getName();
        int p = n.lastIndexOf('.');
        if (p == -1)
            return n;
        return n.substring(0, p);
    }

    private void addLog(int id, String msg) {
        onAddLog(mContext.getString(id) + msg);
    }

    private void addLog(String msg) {
        onAddLog(msg);
    }

    private void addItem(int id) {
        onAddItem(mContext.getString(id));
    }

    private void addItem(String msg) {
        onAddItem(msg);
    }

    private final String CMD_FINISH = "**CMD_DONE**";

    private static class UserCancelException extends Exception {
        public UserCancelException() {
            super("User Cancelled");
        }
    }

    @Override
    protected void onRun() throws IOException {

        onStart();

        try {

            unpackPackage();

            if (exitPending())
                throw new UserCancelException();

            runInstallScript();

            if (exitPending())
                throw new UserCancelException();

            addItem(R.string.strUpdateDone);

            onSuccess();

        } catch (UserCancelException e) {
            onError(mContext.getResources().getString(R.string.strUserCancelInstall));
        } catch (Throwable e) {
            e.printStackTrace();
            onError(e.getMessage());
        } finally {
            onStop();
        }
    }

    void unpackPackage() throws Exception {


        addItem(R.string.strUnpacking);
        addLog(mPkgFile.getName() + " --> " + mExtractPath);

        addLog("unpacking...");

        Zip.unzipFile(mPkgFile, mExtractPath.getAbsolutePath(), new Zip.UnzipListener() {
            @Override
            public boolean onUnzip(String filename, boolean isDir) {
                return !exitPending();
            }
        });

        if (exitPending())
            throw new UserCancelException();
    }

    void runInstallScript() throws Exception {

        TelnetSuProc proc = null;

        try {

            File inst_sh = new File(mExtractPath, "install.sh");

            addItem(R.string.strRunSetupScript);
            addLog(inst_sh.getAbsolutePath());

            if (!inst_sh.exists())
                throw new Exception("no update script found.");

            try {
                proc = new TelnetSuProc(9323);
                Thread.sleep(100);

                if (!proc.isRunning())
                    proc = new TelnetSuProc(23);
            } catch (Throwable e) {
                e.printStackTrace();
            }

            if (proc == null || (proc != null && !proc.isRunning()))
                throw new Exception("failed to create su process.");

            String cmd = "busybox sh " + inst_sh.getAbsolutePath() + " " + inst_sh.getParent()
                    + "\necho " + CMD_FINISH + "$?\nexit\n";
            writeTextFile(mScript, cmd);
            cmd = "exec sh " + mScript.getAbsolutePath() + "\n";

            proc.execute(cmd);

            int result = 1;

            ArrayList<String> out = new ArrayList<String>();

            while (!exitPending()) {
                //String line = mReader.readLine();

                Thread.sleep(100);

                out.clear();
                proc.getOutputs(out);

                if (out.size() == 0 && !proc.isRunning())
                    break;

                for (String line : out) {
                    if (line.startsWith("busybox") ||
                            line.startsWith("root@") ||
                            line.startsWith("exec "))
                        continue;

                    if (line.startsWith(CMD_FINISH)) {
                        try {
                            result = Integer.parseInt(line.substring(CMD_FINISH.length()));
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                        break;
                    }

                    addLog(line);
                }
            }

            if (exitPending())
                throw new UserCancelException();

            if (result != 0)
                throw new Exception("failed to install update package.");

        } finally {

            if (proc != null) {
                proc.stop();
            }

            if (mScript.exists()) {
                mScript.delete();
            }
        }
    }

    private static void writeTextFile(File path, String txt) throws Exception {
        FileOutputStream os = null;
        try {
            os = new FileOutputStream(path);
            os.write(txt.getBytes("UTF-8"));
            os.flush();
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void onExitRequest() {
    }
}
