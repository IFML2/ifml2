package ifml2.om;

import ifml2.IFML2Exception;
import ifml2.vm.RunningContext;
import ifml2.vm.VirtualMachine;

import java.util.Date;
import java.util.TimerTask;

public class RealTimeTimer extends GameTimer {
    private java.util.Timer timer = new java.util.Timer();
    private Date time;

    public RealTimeTimer(String name, Date time) {
        super(name);
        this.time = time;
    }

    @Override
    public void start(final VirtualMachine virtualMachine) {
        this.virtualMachine = virtualMachine;

        Date now = new Date();
        Date when = new Date(now.getTime() + time.getTime());

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    virtualMachine
                            .runInstructionList(RealTimeTimer.this, RunningContext.CreateNewContext(virtualMachine));
                } catch (IFML2Exception e) {
                    virtualMachine.reportError(e, "{0}\n  при работе таймера \"{1}\"", e.getMessage(), getName());
                } finally {
                    cancel();
                }
            }
        }, when);
    }

    public Date getTime() {
        return time;
    }
}

