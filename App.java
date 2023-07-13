import HeftyInteger;
import java.math.BigInteger;
/**
 * A driver for CS1501 Project 5
 * @author Dr. Farnan
 * @author Dr. Garrison
 */
public class App {

    public static void main(String[] args) {
		BigInteger biA = new BigInteger("987654321654321");
		BigInteger biB = new BigInteger("78314141999");
		
		HeftyInteger hiA = new HeftyInteger(biA.toByteArray());
		HeftyInteger hiB = new HeftyInteger(biB.toByteArray());
		
		HeftyInteger hiRes = hiA.times(hiB);
		BigInteger res = biA.multiply(biB);
		
		System.out.println(res.compareTo(new BigInteger(hiRes.getVal()));

		
    }

}