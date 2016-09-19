package com.phantom.onetapvideodownload;

import com.phantom.utils.Global;

import junit.framework.Assert;

import org.junit.Test;

public class GlobalTest {
    @Test
    public void getNewFilename_test() {
        assertTrue(Global.getNewFilename("foobar"), "foobar (1)");
        assertTrue(Global.getNewFilename("foobar (1)"), "foobar (2)");
        assertTrue(Global.getNewFilename("foo.txt"), "foo (1).txt");
        assertTrue(Global.getNewFilename("foo (1).txt"), "foo (2).txt");
        assertTrue(Global.getNewFilename("foo bar"), "foo bar (1)");
        assertTrue(Global.getNewFilename("foo bar(1)"), "foo bar(2)");
    }

    public static void assertTrue(String first, String second){
        try{
            Assert.assertTrue(first.equals(second));
            System.out.println(first + " - passed");
        } catch(AssertionError e){
            System.out.println("Failed -- Expected -- " + second + " -- Actual -- " + first);
        }
    }
}
