package com.ibm.ws.os.specific.packaging.fat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;

import com.ibm.websphere.simplicity.ProgramOutput;
import com.ibm.websphere.simplicity.log.Log;
import com.ibm.websphere.simplicity.OperatingSystem;

/**
 *
 */
public class InstallRhelTest extends InstallUtilityToolTest{
    private static final Class<?> c = InstallRhelTest.class;

    @BeforeClass
    public static void beforeClassSetup() throws Exception {
        Assume.assumeTrue(isLinuxRhel());
        entering(c, System.getProperty("os.name").toLowerCase());
        entering(c, System.getProperty("gsaid").toLowerCase());
        entering(c, System.getProperty("gsapw").toLowerCase());
        entering(c, System.getProperty("intranet.user").toLowerCase());
        entering(c, System.getProperty("intranet.password").toLowerCase());
        //Assume.assumeTrue(ConnectedToIMRepo);
        setupEnv();
    }

    @AfterClass
    public static void cleanup() throws Exception {
        final String METHOD_NAME = "cleanup";
        entering(c, METHOD_NAME);
        cleanupEnv();
        exiting(c, METHOD_NAME);
    }

    @Test
    public void testInstallRpm() throws Exception {
       
        String METHOD_NAME = "testInstallRpm";
        entering(c, METHOD_NAME);

        String[] param1s = { "install", "./openliberty-19.0.0.5-1.noarch.rpm" };
        ProgramOutput po = runCommand(METHOD_NAME, "yum", param1s);
        assertEquals("Expected exit code", 0, po.getReturnCode());
        exiting(c, METHOD_NAME);
    }

    @Test
    public void testVerifyRpmInstall() throws Exception {
       
        String METHOD_NAME = "testVerifyRpmInstall";
        entering(c, METHOD_NAME);

        String[] param1s = { "-qi", "openliberty" };
        ProgramOutput po = runCommand(METHOD_NAME, "rpm", param1s);
        assertEquals("Expected exit code", 0, po.getReturnCode());
        exiting(c, METHOD_NAME);
    }

    @Test
    public void testServerStartStopRpm() throws Exception {
       
        String METHOD_NAME = "testServerStartStopRpm";
        entering(c, METHOD_NAME);

        String[] param1s = { "start", "openliberty@defaultServer.service" };
        ProgramOutput po1 = runCommand(METHOD_NAME, "systemctl", param1s);
        assertEquals("Expected exit code", 0, po1.getReturnCode());

        File f = new File("/var/run/openliberty/defaultServer.pid");
        assertTrue("Server pid should exist",
                   f.exists());
        
        String[] param2s = { "stop", "openliberty@defaultServer.service" };
        ProgramOutput po2 = runCommand(METHOD_NAME, "systemctl", param2s);
        assertEquals("Expected exit code", 0, po2.getReturnCode());
        exiting(c, METHOD_NAME);
    }


    @Test
    public void testUninstallRpm() throws Exception {
       
        String METHOD_NAME = "testUninstallRpm";
        entering(c, METHOD_NAME);

        String[] param1s = { "remove", "openliberty" };
        ProgramOutput po = runCommand(METHOD_NAME, "yum", param1s);
        assertEquals("Expected exit code", 0, po.getReturnCode());
        exiting(c, METHOD_NAME);
    }
}
