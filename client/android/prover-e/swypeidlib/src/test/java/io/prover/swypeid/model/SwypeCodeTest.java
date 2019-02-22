package io.prover.swypeid.model;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

//@RunWith(RobolectricTestRunner.class)
public class SwypeCodeTest {

    @Test
    public void directionsFromCodeV2() {
        int[] dirs = SwypeCode.directionsFromCodeV1("56578456");
        assertArrayEquals(new int[]{7, 3, 2, 7, 4, 7, 7}, dirs);
        dirs = SwypeCode.directionsFromCodeV1("584268565");
        assertArrayEquals(new int[]{1, 4, 6, 8, 2, 5, 7, 3}, dirs);
    }

    @Test
    public void directionsFromCodeV1() {
        int[] dirs = SwypeCode.directionsFromCodeV2("*14682573");
        assertArrayEquals(new int[]{1, 4, 6, 8, 2, 5, 7, 3}, dirs);
    }

    @Test
    public void toSwypeCodeV2() {
        SwypeCode code = new SwypeCode("*123456781154");
        Assert.assertEquals("*123456781154", code.getCodeV2());
    }

    @Test
    public void toSwypeCodeV1Fail() {
        SwypeCode code = new SwypeCode("*1111");
        Assert.assertEquals(null, code.getCodeV1());
    }

    @Test
    public void toSwypeCodeV1() {
        SwypeCode code = new SwypeCode("*14682573");
        Assert.assertEquals("584268565", code.getCodeV1());

        code = new SwypeCode("*713355772");
        Assert.assertEquals("5698741235", code.getCodeV1());

        code = new SwypeCode("5321487596");
        Assert.assertEquals("5321487596", code.getCodeV1());
    }

    @Test

    public void rotateDirs() {
        SwypeCode code = new SwypeCode("*13572648");
        // code.rotateV1(90).getCodeV2()  = *71358426
        // code.rotateV1(180).getCodeV2()  = *57136284
        // code.rotateV1(270).getCodeV2()  = *35714862
        /**
         * run with Roboelectric
         */
/*        assertEquals(code.rotateV1(0).getCodeV2(), code.rotate(0).getCodeV2());
        assertEquals(code.rotateV1(90).getCodeV2(), code.rotate(90).getCodeV2());
        assertEquals(code.rotateV1(180).getCodeV2(), code.rotate(180).getCodeV2());
        assertEquals(code.rotateV1(270).getCodeV2(), code.rotate(270).getCodeV2());*/

        Assert.assertEquals("*13572648", code.rotate(0).getCodeV2());
        Assert.assertEquals("*71358426", code.rotate(90).getCodeV2());
        Assert.assertEquals("*57136284", code.rotate(180).getCodeV2());
        Assert.assertEquals("*35714862", code.rotate(270).getCodeV2());
    }
}