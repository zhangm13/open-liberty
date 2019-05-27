package com.ibm.ws.os.specific.packaging.fat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.ibm.websphere.simplicity.ProgramOutput;
import com.ibm.websphere.simplicity.log.Log;
import com.ibm.websphere.simplicity.OperatingSystem;

/**
 *
 */
public class InstallTest extends InstallUtilityToolTest{
    private static final Class<?> c = InstallTest.class;

    @BeforeClass
    public static void beforeClassSetup() throws Exception {

        Assume.assumeTrue(isLinux);
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
    public void testInstallDeb() throws Exception {
       
        String METHOD_NAME = "testInstallDeb";
        entering(c, METHOD_NAME);

        String[] param1s = { "install", "./openliberty.rpm" };
        ProgramOutput po = runCommand(METHOD_NAME, "apt-get", param1s);
        assertEquals("Expected exit code", 0, po.getReturnCode());
        String output = po.getStdout();
        exiting(c, METHOD_NAME);
    }

    // @Test
    // public void testInstallRpm() throws Exception {
       
    //     String METHOD_NAME = "testInstallRpm";
    //     entering(c, METHOD_NAME);

    //     String[] param1s = { "install", "./openliberty.rpm" };
    //     ProgramOutput po = runCommand(METHOD_NAME, "yum", param1s);
    //     assertEquals("Expected exit code", 0, po.getReturnCode());
    //     String output = po.getStdout();
    //     assertTrue("Should contain genericCoreFeatureDependancyOnEsaPass",
    //                output.indexOf("genericCoreFeatureDependancyOnEsaPass : com.ibm.genericCoreFeatureDependancyOnEsaPass") >= 0);
    //     exiting(c, METHOD_NAME);
    // }
}
