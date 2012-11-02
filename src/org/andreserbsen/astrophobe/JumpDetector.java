package org.andreserbsen.astrophobe;


import java.util.LinkedList;

public abstract class JumpDetector extends LinkedList<Long> {
    private int limit;
    private float threshold;

    public JumpDetector(int limit, float threshold) {
        this.limit = limit;
	this.threshold = threshold;
    }

    @Override
    public boolean add(Long val) {
	if (size() >= 1 && size() >= limit/2) {
	    float avg = 0;
	    for (long oldval : this) avg += oldval;
	    avg /= size();
	    if ( (Math.signum(threshold) > 0 && val > avg*(1+threshold))
	      || (Math.signum(threshold) < 0 && val < avg*(1+threshold)) ) {
	        onJump();
	    }
	}
        super.add(val);
        while (size() > limit) { super.remove(); }
        return true;
    }

    protected abstract void onJump();

}
