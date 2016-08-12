package ifml2.unittests;

import ifml2.om.Action;
import ifml2.om.Hook;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class HookTest{
    @Test
    public void testClone() throws Exception {
        Hook hook = new Hook();
        Action action = new Action();
        action.setName("Sample");
        action.setDescription("Desc");
        hook.setAction(action);
        hook.setObjectElement("objEl");
        hook.setType(Hook.Type.AFTER);

        Hook hookClone = hook.clone();
        assertEquals(hook, hookClone);

        //Hook hookCopy = new Hook();
        // TODO: 12.08.2016 copyTO hookClone.cop
    }
}