package com.ibm.ws.os.specific.packaging.fat;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

import com.ibm.websphere.simplicity.ProgramOutput;
import com.ibm.websphere.simplicity.RemoteFile;
import com.ibm.websphere.simplicity.log.Log;

import componenttest.topology.impl.LibertyServer;
import componenttest.topology.impl.LibertyServerFactory;

/**
 *
 */
public abstract class InstallUtilityToolTest {

    private static final Class<?> c = InstallUtilityToolTest.class;
    public static LibertyServer server;
    public static String installRoot;
    public static boolean isLinux = System.getProperty("os.name").toLowerCase().contains("linux");
    protected static List<String> cleanFiles;
    protected static List<String> cleanDirectories;
    public static boolean connectedToRepo = true;

    /**
     * Setup the environment.
     * 
     * @param svr
     *            The server instance.
     * 
     * @throws Exception
     */
    protected static void setupEnv() throws Exception {
        final String METHOD_NAME = "setup";
        server = LibertyServerFactory.getLibertyServer("com.ibm.ws.os.specific.packaging_fat");
        installRoot = server.getInstallRoot();
        Log.info(c, METHOD_NAME, "installRoot: " + installRoot);
        cleanDirectories = new ArrayList<String>();
        cleanFiles = new ArrayList<String>();
    }

    protected static void entering(Class<?> c, String METHOD_NAME) {
        Log.info(c, METHOD_NAME, "---- " + METHOD_NAME + " : entering ----------------------------");
    }

    protected static void exiting(Class<?> c, String METHOD_NAME) {
        Log.info(c, METHOD_NAME, "---- " + METHOD_NAME + " : exiting ----------------------------");
    }

    /**
     * This method removes all the testing artifacts from the server directories.
     * 
     * @throws Exception
     */
    protected static void cleanupEnv() throws Exception {
        final String METHOD_NAME = "cleanupEnv";
        entering(c, METHOD_NAME);
        if (server.isStarted())
            server.stopServer();
        for (String cFile : cleanFiles) {
            server.deleteFileFromLibertyInstallRoot(cFile);
            Log.info(c, METHOD_NAME, "delete " + cFile);
        }
        for (String cDir : cleanDirectories) {
            server.deleteDirectoryFromLibertyInstallRoot(cDir);
            Log.info(c, METHOD_NAME, "delete " + cDir);
        }
        exiting(c, METHOD_NAME);
    }

    protected static boolean isLinuxUbuntu() throws Exception {
        if (isLinux){
            String content = new Scanner(new File("/etc/os-release")).useDelimiter("\\Z").next();
            if (content.contains("ubuntu")){
                return true;
            }
        }
        return false;
    }

    protected static boolean isLinuxRhel() throws Exception {
        if (isLinux){
            String content = new Scanner(new File("/etc/os-release")).useDelimiter("\\Z").next();
            if (content.contains("rhel")){
                return true;
            }
        }
        return false;
    }

    protected ProgramOutput runCommand(String testcase, String command, String[] params) throws Exception {
        String args = "";
        for (String param : params) {
            args = args + " " + param;
        }
        Log.info(c, testcase,
                 "command: " + command + " " + args);
        ProgramOutput po = server.getMachine().execute(command, params, installRoot);
        Log.info(c, testcase, po.getStdout());
        Log.info(c, testcase, command + " command exit code: " + po.getReturnCode());
        return po;
    }

    protected static void remove(Collection<String> files) throws Exception {
        String METHOD_NAME = "remove";
        entering(c, METHOD_NAME);
        for (String f : files) {
            server.deleteFileFromLibertyInstallRoot(f);
            Log.info(c, METHOD_NAME, "delete " + f);
        }
        exiting(c, METHOD_NAME);
    }

    protected static void remove(Collection<String> files, Collection<String> directories) throws Exception {
        String METHOD_NAME = "remove";
        entering(c, METHOD_NAME);
        for (String f : files) {
            server.deleteFileFromLibertyInstallRoot(f);
            Log.info(c, METHOD_NAME, "delete " + f);
        }
        for (String d : directories) {
            server.deleteDirectoryFromLibertyInstallRoot(d);
            Log.info(c, METHOD_NAME, "delete " + d);
        }
        exiting(c, METHOD_NAME);
    }

    protected static void assertFilesExist(String msg, String[] filePaths) throws Exception {
        for (String filePath : filePaths) {
            assertTrue(msg + ": " + filePath + " does not exist.", server.fileExistsInLibertyInstallRoot(filePath));
        }
    }

    protected static void assertFilesNotExist(String msg, String[] filePaths) throws Exception {
        for (String filePath : filePaths) {
            assertFalse(msg + ": " + filePath + " exists.", server.fileExistsInLibertyInstallRoot(filePath));
        }
    }

    public static void testRepositoryConnection() {
        try {
            String localPath = new File(".").getCanonicalPath();
            File massiveRepoPath = new File(localPath.endsWith("autoFVT") ? "publish/massiveRepo" : "autoFVT/publish/massiveRepo");
            File[] repoPropsFiles = massiveRepoPath.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.toLowerCase().endsWith(".props");
                }
            });
            //Randomly ordered the files, to balance the work load for the servers
            Arrays.sort(repoPropsFiles, new Comparator<File>() {
                @Override
                public int compare(File f1, File f2) {
                    return (int) Math.round(Math.random() * 10 - 5);
                }
            });

            String userId;
            String password;
            String url;
            String apiKey;
            String softlayerUserId;
            String softlayerPassword;

            /*
             * use existing connection to create login first, set connectedToRepo for results;
             */
            if (connectedRepoProperties != null) {
                userId = connectedRepoProperties.get("userId");
                password = connectedRepoProperties.get("password");
                url = connectedRepoProperties.get("repository.url");
                apiKey = connectedRepoProperties.get("apiKey");
                softlayerUserId = connectedRepoProperties.get("softlayerUserId");
                softlayerPassword = connectedRepoProperties.get("softlayerPassword");

                RestRepositoryConnection loginInfo = new RestRepositoryConnection(userId, password, apiKey, url, softlayerUserId, softlayerPassword);
                Log.info(TestUtils.class, "testRepositoryConnection", "Connecting to apikey = " + loginInfo.getApiKey() + " repository url = " + loginInfo.getRepositoryUrl());
                connectedToRepo = loginInfo.isRepositoryAvailable() && isRepoEnabled(loginInfo);
                if (connectedToRepo) {
                    Log.info(TestUtils.class, "testRepositoryConnection", "Repository connection is OK.");
                } else {
                    Log.info(TestUtils.class, "testRepositoryConnection", "Cannot connect to the repository");
                }
            }

            //fail over part
            if (connectedRepoProperties == null || connectedToRepo == false) {
                for (File f : repoPropsFiles) {
                    Properties p = new Properties();
                    p.load(new FileInputStream(f));

                    // Load basic properties, set to null if blank
                    userId = p.getProperty("userId");
                    password = p.getProperty("password");
                    url = p.getProperty("repository.url");
                    apiKey = p.getProperty("apiKey");
                    softlayerUserId = p.getProperty("softlayerUserId");
                    softlayerPassword = p.getProperty("softlayerPassword");

                    RestRepositoryConnection loginInfo = new RestRepositoryConnection(userId, password, apiKey, url, softlayerUserId, softlayerPassword);
                    Log.info(TestUtils.class, "testRepositoryConnection",
                             "Connecting to apikey = " + loginInfo.getApiKey() + " repository url = " + loginInfo.getRepositoryUrl());
                    connectedToRepo = loginInfo.isRepositoryAvailable() && isRepoEnabled(loginInfo);
                    if (connectedToRepo) {
                        Log.info(TestUtils.class, "testRepositoryConnection", "Connected to Repository +" + url
                                                                              + ", ApiKey = " + apiKey);
                        repositoryDescriptionUrl = f.getCanonicalFile().toURI().toURL().toString();
                        connectedRepoProperties = new HashMap<String, String>();
                        connectedRepoProperties.put("userId", userId);
                        connectedRepoProperties.put("password", password);
                        connectedRepoProperties.put("repository.url", url);
                        connectedRepoProperties.put("apiKey", apiKey);
                        connectedRepoProperties.put("softlayerUserId", softlayerUserId);
                        connectedRepoProperties.put("softlayerPassword", softlayerPassword);
                        return;
                    } else {
                        Log.info(TestUtils.class, "testRepositoryConnection", "Cannot connect to the repository");
                    }
                    if (!connectedToRepo)
                        Log.info(TestUtils.class, "testRepositoryConnection", "None of the Repository is conncted, " + "The Testcases will be skipped");
                }
            }

        } catch (Exception e) {
            Log.info(TestUtils.class, "testRepositoryConnection", "Cannot connect to the repository : " + e.getMessage());
            connectedToRepo = false;
        } finally {
            MainRepository.clearCachedRepoProperties();
        }
    }
}
