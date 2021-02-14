package nyanli.hackersmorph.other.mchorse.aperture.client.curve;

public interface ICurve {

	String getId();
	
	String getName();
	
	void applyCurve(double value);
	
	void enable();
	
	void disable();
	
}
