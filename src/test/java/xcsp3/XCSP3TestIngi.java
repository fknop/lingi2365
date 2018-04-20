package xcsp3;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class XCSP3TestIngi extends XCSP3TestHelper{
    public XCSP3TestIngi(String path) { super(path); }

    @Parameterized.Parameters
    public static Object[] data() { return dataFromFolder("data/xcsp3/inginious"); }
}
