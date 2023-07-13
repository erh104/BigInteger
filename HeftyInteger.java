

/**
 * HeftyInteger for CS 1501 Project 5
 * @author Dr. Farnan
 * @author Dr. Garrison
 */
public class HeftyInteger implements Comparable<HeftyInteger> {

    private static final byte[] ONE = {(byte) 1};
    private static final byte[] TWO = {(byte) 2};

    private static final byte[] ZERO = {(byte) 0};
    private static final byte[] TEN = {(byte) 0,(byte) 10};

    private static final byte[] HUNNID = {(byte) 0,(byte) 100};
    private static final byte[] THOUSAND = {(byte) 0,(byte) 3,(byte) -24};
    private static final byte[] TenKAY = {(byte) 0,(byte) 39,(byte) 16};
    private static final byte[] ONEMILLI = {(byte) 0,(byte) 15,(byte) 66,(byte) 64};
    private static final byte[] TENMILLI = {(byte) 0,(byte) -104,(byte) -106,(byte) -128};

    private static final byte[] HUNNIDMILLI = {(byte) 0,(byte) 5, (byte) -11,(byte) -31,(byte) 0};
    private static final byte[] TWOFIFTYMILLI = {(byte) 0,(byte) 14,(byte) -26,(byte) -78,(byte) -128};
     private byte[] val;

    /**
     * Construct the HeftyInteger from a given byte array
     *
     * @param b the byte array that this HeftyInteger should represent
     */
    public HeftyInteger(byte[] b) {
        // Copy into a new array to prevent client from manipulating later
        val = new byte[b.length];
        for (int i = 0; i < b.length; i++) {
            val[i] = b[i];
        }
    }

    /**
     * Return this HeftyInteger's value as a byte array
     *
     * @return val
     */
    public byte[] getVal() {
        // Copy into a new array to prevent client from manipulating private
        // data member
        byte[] result = new byte[val.length];
        for (int i = 0; i < val.length; i++) {
            result[i] = val[i];
        }
        return result;
    }

    /**
     * Return the number of bytes in this integer's representation
     *
     * @return length of the val byte array
     */
    public int length() {
        return val.length;
    }

    /**
     * Adds a new byte as the most significant in this integer. That is,
     * prepends a new byte to this integer.
     *
     * @param extension the byte to prepend as the most significant byte
     */
    public void prepend(byte extension) {
        byte[] newv = new byte[val.length + 1];
        newv[0] = extension;
        for (int i = 0; i < val.length; i++) {
            newv[i + 1] = val[i];
        }
        val = newv;
    }

    /**
     * Determines whether this integer is negative (less than 0).
     *
     * @return true if this integer is negative, false if non-negative
     */
    public boolean isNegative() {
        // If this integer is negative, then its most significant bit will be 1,
        // meaning its most significant byte will be a negative signed number
        return (val[0] < 0);
    }

    /**
     * Computes the sum of this integer and another
     *
     * @param other the other HeftyInteger to sum with this
     * @return the sum of this and other
     */
    public HeftyInteger plus(HeftyInteger other) {
        byte[] a, b;
        // If operands are of different sizes, put larger first ...
        if (val.length < other.length()) {
            a = other.getVal();
            b = val;
        }
        else {
            a = val;
            b = other.getVal();
        }

        // ... and normalize size for convenience
        if (b.length < a.length) {
            int diff = a.length - b.length;

            byte pad = (byte) 0;
            if (b[0] < 0) {
                pad = (byte) 0xFF;
            }

            byte[] newb = new byte[a.length];
            for (int i = 0; i < diff; i++) {
                newb[i] = pad;
            }

            for (int i = 0; i < b.length; i++) {
                newb[i + diff] = b[i];
            }

            b = newb;
        }

        // Actually compute the add
        int carry = 0;
        byte[] res = new byte[a.length];
        for (int i = a.length - 1; i >= 0; i--) {
            // Be sure to bitmask so that cast of negative bytes does not
            // introduce spurious 1 bits into result of cast
            carry = ((int) a[i] & 0xFF) + ((int) b[i] & 0xFF) + (carry & 0xFF);

            // Assign to next byte
            res[i] = (byte) (carry & 0xFF);

            // Carry remainder over to next byte (always want to shift in 0s)
            carry = carry >>> 8;
        }

        HeftyInteger res_li = new HeftyInteger(res);
        // If both operands are positive, magnitude could increase as a result
        // of addition
        if (!this.isNegative() && !other.isNegative()) {
            // If we have either a leftover carry value or we used the last bit
            // in the most significant byte, we need to prepend the result
            if (res_li.isNegative()) {
                res_li.prepend((byte) carry);
            }
        }
        // Magnitude could also increase if both operands are negative
        else if (this.isNegative() && other.isNegative()) {
            if (!res_li.isNegative()) {
                res_li.prepend((byte) 0xFF);
            }
        }

        // Note that result will always be the same size as biggest input (e.g.,
        // -127 + 128 will use 2 bytes to store the result value 1)
        return res_li;
    }

    /**
     * Computes the negation of this integer.
     *
     * @return negation of this integer
     */
    public HeftyInteger negate() {
        // Need to update representation using two's complement negation

        byte[] neg = new byte[val.length];
        int offset = 0;

        // Check to ensure we can represent negation in same length (e.g., -128
        // can be represented in 8 bits using two's complement, +128 requires 9)
        if (val[0] == (byte) 0x80) { // 0x80 is 10000000
            boolean needs_ex = true;
            for (int i = 1; i < val.length; i++) {
                if (val[i] != (byte) 0) {
                    needs_ex = false;
                    break;
                }
            }
            // if first byte is 0x80 and all others are 0, must prepend
            if (needs_ex) {
                neg = new byte[val.length + 1];
                neg[0] = (byte) 0;
                offset = 1;
            }
        }

        // flip all bits
        for (int i = 0; i < val.length; i++) {
            neg[i + offset] = (byte) ~val[i];
        }

        HeftyInteger neg_li = new HeftyInteger(neg);

        // add 1 to complete two's complement negation
        return neg_li.plus(new HeftyInteger(ONE));
    }

    /**
     * Computes the difference of this integer and another
     *
     * @param other HeftyInteger to subtract from this
     * @return difference of this and other
     */
    public HeftyInteger minus(HeftyInteger other) {
        // Implement subtraction as simply negation and addition
        return this.plus(other.negate());
    }

    /**
     * Determines if this integer is equal to another
     *
     * @param other the integer to compare to this
     * @return true if this == other
     */
    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
        if (!(other instanceof HeftyInteger)) return false;
        return (compareTo((HeftyInteger)other) == 0);
    }

    /**
     * Compares this integer to another
     *
     * @param other the integer to compare to this
     * @return a positive value if this > other;
     *         a negative value if this < other;
     *         0 if this == other
     */
    @Override
    public int compareTo(HeftyInteger other) 
    {
        if(this.length() == 1 && other.length() == 1)
        {
            if(this.getVal()[0] > other.getVal()[0])
                return 1;
            else if(this.getVal()[0] < other.getVal()[0])
                return -1;
            else
            return 0;
        }
        byte[] w = this.getVal();
        byte[] p = other.getVal();
        HeftyInteger thisCopy = new HeftyInteger(w);
        HeftyInteger otherCopy = new HeftyInteger(p);
        while(thisCopy.getVal().length < otherCopy.getVal().length)
        {
            thisCopy.prepend((byte) 0);
        }
        while(otherCopy.getVal().length < thisCopy.getVal().length)
            otherCopy.prepend((byte) 0);
        byte[] a = thisCopy.getVal();
        byte[] b = otherCopy.getVal();
        
        //basic positive negative check
        if(this.isNegative() && !other.isNegative())
        {
            return -1;
        }
        else if (!this.isNegative() && other.isNegative())
        {
            return 1;
        }
        //the arrays we are left with are of the same sign..


        HeftyInteger output1 = new HeftyInteger(a);
        HeftyInteger output2 = new HeftyInteger(b);

        //iterate from least significant digit to most significant digit (without sign index)
        int[] zerosA = new int[a.length];
        int[] zerosB = new int[b.length];
        int countA = 0;
        int countB = 0;
        int max = Math.max(a.length,b.length);
		
		//compare each byte to corresponding one and tally it
        for(int i = 0;i < max;i++)
        {
            
            int abra = (int) a[i] & 0xFF;
            int babra = (int) b[i] & 0xFF;
            if(abra < babra)
            {
                zerosA[countA] = i;
                countA++;
            }
            else
            {
                zerosA[countA] = -1;
                countA++;
            }
            
            if(babra < abra)
            {
                zerosB[countB] = i;
                countB++;
            }
            else
            {
                
                zerosB[countB] = -1;
                countB++;
            }
            
        }
        
        //our arrays now hold a mapping of every less than in the byte array and its position in the array


        //for every zero
        for(int i = 0;i < zerosA.length;i++)
        {
            //see if the index position is lower
            
            if(zerosA[i] < zerosB[i] && zerosB[i] != -1)           
            {
                //if it is..that number is smaller
                return 1;
            }
            else if(zerosA[i] < zerosB[i] && zerosB[i] == -1)
                return -1;     
            else if(zerosA[i] > zerosB[i] && zerosB[i] != -1)
                return -1;
            else if(zerosA[i] > zerosB[i] && zerosB[i] == -1)
                return -1;
            else{
                if(zerosB.length > zerosA.length)
                {
                    if(i + 1 == zerosA.length)
                    {
                        //if im at the end of my array.. and theres more in the other one, i had less zeroes in less significant places
                        return -1;
                    }
                    
                }
                if(zerosA.length > zerosB.length)
                {

                }
                
            }
        }


        // TODO: Implement this method and replace the return statement
        return 0;
    }
    private void normalizeLength(byte[] a, byte b[])
    {
        //put larger first..
        

        // ... and normalize size for convenience
        if (b.length < a.length) {
            int diff = a.length - b.length;

            byte pad = (byte) 0;
            if (b[0] < 0) {
                pad = (byte) 0xFF;
            }

            byte[] newb = new byte[a.length];
            for (int i = 0; i < diff; i++) {
                newb[i] = pad;
            }

            for (int i = 0; i < b.length; i++) {
                newb[i + diff] = b[i];
            }

            b = newb;
        }
        else if (a.length < b.length) {
            int diff = b.length - a.length;

            byte pad = (byte) 0;
            if (b[0] < 0) {
                pad = (byte) 0xFF;
            }

            byte[] newA = new byte[b.length];
            for (int i = 0; i < diff; i++) {
                newA[i] = pad;
            }

            for (int i = 0; i < a.length; i++) {
                newA[i + diff] = a[i];
            }

            a = newA;
        }
        
    }
    /**
     * Computes the product of this integer and another
     *
     * @param other HeftyInteger to multiply by this
     * @return product of this and other
     */
    public HeftyInteger times(HeftyInteger other) 
    {
        HeftyInteger product;
        HeftyInteger otherCopy = new HeftyInteger(other.getVal());
        HeftyInteger thisCopy = new HeftyInteger(this.getVal());
        int count = 0;
        // TODO: Implement this method and replace the return statement
        if(other.isNegative() == true)
        {
            count++;
            otherCopy = other.negate();
        }
        if(this.isNegative() == true)
        {
            count++;
            thisCopy = this.negate();
        }
        thisCopy.prepend((byte)0);
        otherCopy.prepend((byte)0);
        byte one[] = thisCopy.getVal();
        byte two[] = otherCopy.getVal();
        
        //for every byte in a
       //if(this.length() < 85 && other.length() < 85)

        product = multiply(thisCopy,otherCopy);
       //else
       //product = multiplyKaratsuba(this.getVal(), other.getVal());
        

        if(count == 1)
        {
            product = product.negate();

        }

        return product;
    }
private HeftyInteger multiply(HeftyInteger x,HeftyInteger y)
{
        
        
        byte[] xA = x.getVal();
        byte[] yA = y.getVal();
        
        byte[] result = new byte[x.length() + y.length()+1];
        int carry = 0;
        int count = x.length() + y.length();
        int rounds = 0;

        for(int j = yA.length - 1; j >=0;j--)
        {
            carry = 0;
            for(int i = xA.length-1; i >= 0;i--)
            {
                if(xA[i] == 0 && yA[j] == 0)
                {

                }
                else
                {
               
                carry = (((int) xA[i] & 0xFF) * ((int) yA[j] & 0xFF)) + ((carry & 0xFF)) + ((int) result[i+j+1] & 0xFF);
               
                
                result[i+j+1] = (byte) (carry & 0xFF);
                

                carry = carry >>> 8;
                }
            }
            rounds++;
            
            while(carry != 0)
                {
                    carry = ((int) result[j+1] & 0xFF);
                    result[j+1] = (byte) (carry & 0xFF);
                    carry = carry >>> 8;
                    
                }

        }
       
        byte[] newA = new byte[result.length-1];
        for(int i = 0;i<result.length-1;i++)
        {
            newA[i] = result[i];
        }
        HeftyInteger res_li = new HeftyInteger(newA);
        if(y.isNegative() == true && x.isNegative() == false)
        {
            res_li = res_li.negate();
        }
        return res_li;
        
}       
    
   

    private HeftyInteger multiplyKaratsuba(byte[] x, byte[] y)
    {
        String s = new String(x);
        String xUpper = s.substring(0,s.length()/2);
        HeftyInteger a = new HeftyInteger(xUpper.getBytes());
        String xLower = s.substring(s.length()/2);
        HeftyInteger b = new HeftyInteger(xLower.getBytes());
        
        s = new String(y);
        String yUpper = s.substring(0,s.length()/2);
        HeftyInteger c = new HeftyInteger(yUpper.getBytes());
        String yLower = s.substring(s.length()/2);
        HeftyInteger d = new HeftyInteger(yLower.getBytes());

        HeftyInteger product1 = a.times(c);
        
        HeftyInteger product2 = b.times(d);

        HeftyInteger product3 = a.plus(b).times(c.plus(d));
        
        product1 = product1.shiftLeft(s.length()-1).plus(product3.minus(product1).minus(product2)).shiftLeft(Math.max(x.length,y.length)/2).plus(product2);
        return product1;


    }
    private HeftyInteger shiftLeftCopy(int i)
    {
        byte[] newv = new byte[val.length + i];
        
        for (int z = 0; z < val.length; z++) {
            newv[z] = val[z];
        }
        for(int z = val.length;z< val.length + i;z++)
        {
            newv[z] = (byte) 0;
        }

        return new HeftyInteger(newv);
    }
    private HeftyInteger shiftLeft(int i) 
    {
        byte[] newv = new byte[val.length + i];
        
        for (int z = 0; z < val.length; z++) {
            newv[z] = val[z];
        }
        for(int z = val.length;z< val.length + i;z++)
        {
            newv[z] = (byte) 0;
        }
        val = newv;

        return new HeftyInteger(val);
    }

    /**
     * Runs the extended Euclidean algorithm on this and other
     * @param other another HeftyInteger
     * @return an array structured as follows:
     *   0:  the GCD of this and other
     *   1:  a valid x value
     *   2:  a valid y value
     * such that this * x + other * y == GCD in index 0
     */
    public HeftyInteger[] Xgcd(HeftyInteger other) 
    {
        HeftyInteger[] results = this.extendedEuclidean(other);
        if(results[1].isNegative() == true)
        {
            int count = 0;
            byte[] bb = results[1].getVal();
            for(int i = 0;i<bb.length;i++)
            {
                if(bb[i] == ((byte) 255))
                    count++;
            }
            byte[] newRes = new byte[bb.length - count+1];
            int cc=0;
            for(int i = count-1;i<bb.length;i++)
            {
                newRes[cc++] = bb[i];
            }
            results[1] = new HeftyInteger(newRes);
        }
        if(results[2].isNegative() == true)
        {
            int count = 0;
            byte[] bb = results[2].getVal();
            for(int i = 0;i<bb.length;i++)
            {
                if(bb[i] == ((byte) 255))
                    count++;
            }
            byte[] newRes = new byte[bb.length - count+1];
            int cc=0;
            for(int i = count-1;i<bb.length;i++)
            {
                newRes[cc++] = bb[i];
            }
            results[2] = new HeftyInteger(newRes);
        }

        if(results[1].isNegative() == false)
        {
            int count = 0;
            byte[] bb = results[1].getVal();
            for(int i = 0;i<bb.length;i++)
            {
                if(bb[i] == ((byte) 0))
                    count++;
            }
            if(count > 0)
            {
            byte[] newRes = new byte[bb.length - count+1];
            int cc=0;
            for(int i = count-1;i<bb.length;i++)
            {
                newRes[cc++] = bb[i];
            }
            results[1] = new HeftyInteger(newRes);
            }
        }
        if(results[2].isNegative() == false)
        {
            int count = 0;
            byte[] bb = results[2].getVal();
            for(int i = 0;i<bb.length;i++)
            {
                if(bb[i] == ((byte) 0))
                    count++;
            }
            byte[] newRes = new byte[bb.length - count+1];
            int cc=0;
            for(int i = count-1;i<bb.length;i++)
            {
                newRes[cc++] = bb[i];
            }
            results[2] = new HeftyInteger(newRes);
        }
        return results;
        //cant divide by 0
       
    }
    private HeftyInteger[] extendedEuclidean(HeftyInteger other) {
        if(other.compareTo(new HeftyInteger(HeftyInteger.ZERO)) == 0)
        {
                        
            return new HeftyInteger[]{new HeftyInteger(this.getVal()),new HeftyInteger(ONE),new HeftyInteger(ZERO)};
        }
        
        HeftyInteger a;
        HeftyInteger b;
        if(other.compareTo(this) == 1)
        {
            a = new HeftyInteger(other.getVal());
            b = new HeftyInteger(this.getVal());
        }
        else
        {
            a= new HeftyInteger(this.getVal());
            b = new HeftyInteger(other.getVal());
        }
        //recursive call
        HeftyInteger aModb = a.divideHandler(b)[1];
        
        HeftyInteger vals[] = other.Xgcd(aModb);

        //compute results and store
        HeftyInteger res1 = vals[0];
        HeftyInteger res2 = vals[2];
        HeftyInteger result = a.divideHandler(b)[0];
        HeftyInteger res3 = vals[1].minus((result.times(res2)));
        
        
        return new HeftyInteger[]{res1,res2, res3};
    }

    public HeftyInteger[] divideHandler(HeftyInteger other)
    {
        HeftyInteger thisCopy = new HeftyInteger(this.getVal());
        HeftyInteger otherCopy = new HeftyInteger(other.getVal());
        int count = 0;
        if(other.isNegative() == true)
        {
            count++;
            otherCopy = other.negate();
        }
        if(this.isNegative() == true)
        {
            count++;
            thisCopy = this.negate();
        }

        HeftyInteger[] results = thisCopy.divide(otherCopy);
        HeftyInteger quotient = results[0];
        if(count == 1)
            quotient = quotient.negate();
        return new HeftyInteger[]{quotient,results[1]};
    }
    public HeftyInteger[] div(HeftyInteger other)
    {
        byte[] xA = this.getVal();
        byte[] yA = other.getVal();
        

        HeftyInteger remainder = new HeftyInteger(this.getVal());
        HeftyInteger count = new HeftyInteger(ZERO);
        
        HeftyInteger copy = new HeftyInteger(remainder.getVal());
            HeftyInteger multipier = new HeftyInteger(other.getVal());
           
            HeftyInteger intermediateTwoFiftyMill = multipier.times(new HeftyInteger(TWOFIFTYMILLI));

            HeftyInteger intermediateHunMill = multipier.times(new HeftyInteger(HUNNIDMILLI));
            HeftyInteger intermediateTenMill = multipier.times(new HeftyInteger(TENMILLI));

            HeftyInteger intermediateOneMill = multipier.times(new HeftyInteger(ONEMILLI));
            HeftyInteger intermediateTenThou = multipier.times(new HeftyInteger(TenKAY));
            HeftyInteger intermediateThou = multipier.times(new HeftyInteger(THOUSAND));

            HeftyInteger intermediateHun = multipier.times(new HeftyInteger(HUNNID));
            HeftyInteger intermediateTen = multipier.times(new HeftyInteger(TEN));
        while(remainder.minus(other).isNegative() == false)
        {
            copy = new HeftyInteger(remainder.getVal());
            
            
            
            if(copy.minus(intermediateTwoFiftyMill).isNegative() == false)
            {
                
                remainder = new HeftyInteger(remainder.minus(intermediateTwoFiftyMill).getVal());;
                count = count.plus(new HeftyInteger(TWOFIFTYMILLI));
            }
            else if(copy.minus(intermediateHunMill).isNegative() == false)
            {
                

                remainder = new HeftyInteger(remainder.minus(intermediateHunMill).getVal());
                count = count.plus(new HeftyInteger(HUNNIDMILLI));
            }
            else if(copy.minus(intermediateTenMill).isNegative() == false)
            {
                
                 

                remainder = new HeftyInteger(remainder.minus(intermediateTenMill).getVal());
                count = count.plus(new HeftyInteger(TENMILLI));
            }
            else if(copy.minus(intermediateOneMill).isNegative() == false)
            {
                

                remainder = new HeftyInteger(remainder.minus(intermediateOneMill).getVal());
                count = count.plus(new HeftyInteger(ONEMILLI));
            }
            else if(copy.minus(intermediateTenThou).isNegative() == false)
            {
                

                remainder = new HeftyInteger(remainder.minus(intermediateTenThou).getVal());
                count = count.plus(new HeftyInteger(TenKAY));
            }
            else if(copy.minus(intermediateThou).isNegative() == false)
            {
                

                remainder = new HeftyInteger(remainder.minus(intermediateThou).getVal());
                count = count.plus(new HeftyInteger(THOUSAND));
            }
            else if(copy.minus(intermediateHun).isNegative() == false)
            {
                
                remainder = new HeftyInteger(remainder.minus(intermediateHun).getVal());
                count = count.plus(new HeftyInteger(HUNNID));
            }
            else if(copy.minus(intermediateTen).isNegative() == false)
            {
                
                remainder = new HeftyInteger(remainder.minus(intermediateTen).getVal());
                count = count.plus(new HeftyInteger(TEN));
            }
            else if(copy.minus(other).isNegative() == false)
            {
                HeftyInteger multiplier = new HeftyInteger(other.getVal());
                
                remainder = new HeftyInteger(remainder.minus(multiplier).getVal());
                count = count.plus(new HeftyInteger(ONE));
            }
           

            
        }

        }

        return new HeftyInteger[]{count,remainder};
        
    }
    public HeftyInteger[] divide(HeftyInteger other) 
    {
        
        if(other.compareTo(this) == 1)
        {
            truncateArray(val);
            return new HeftyInteger[]{new HeftyInteger(ZERO),this};
        }
        if(other.compareTo(this) == 0)
        {
            return new HeftyInteger[]{new HeftyInteger(ONE),new HeftyInteger(ZERO)};
        }
        
        int difference = 0;
        if(this.length() > other.length())
            difference = this.length();
        else
            difference = other.length();
        
        if(other.length() == 1 || (other.length() == 2 && other.getVal()[0] == 0))
        {
            return standardDivide(other);
        }
        
        byte[] xA = this.getVal();
        byte[] yA = other.getVal();
        HeftyInteger result = new HeftyInteger(ZERO);

        byte[] res;
        
        res = new byte[Math.abs(xA.length-yA.length) + 1];
        int carry = 0;
        int count = 0;
        int rounds = 0;

    HeftyInteger remainder = new HeftyInteger(ZERO);
            int imD;
            //take equal portion of bytes as Divisor
            byte[] toDivide = new byte[yA.length];
            boolean youChill = false;
            for(int i = 0; i < toDivide.length && i < xA.length;i++)
            {
                toDivide[i] = xA[i];
                 
            }
            

            int currentPos=toDivide.length;
            HeftyInteger divisor = new HeftyInteger(yA);
            int offset= 0;
            HeftyInteger resultz[];
            if(yA.length+offset== xA.length)
            {
                remainder = new HeftyInteger(xA);
            }
            for(int i = yA.length+offset; i < xA.length;i++)
            {
                
                HeftyInteger intermediateDividend = new HeftyInteger(toDivide);
                
                //if our divisor is less than our intermediate dividend, we must append a digit from the actual dividend
                if(divisor.compareTo(intermediateDividend) == 1 && currentPos < xA.length)
                {
                    
                    
                    byte[] newNew = new byte[toDivide.length+1];
                    for(int z = 0;z<newNew.length;z++)
                    {
                        if(z < toDivide.length)
                        newNew[z] = toDivide[z];
                        else if(currentPos<xA.length)
                        newNew[z] = xA[currentPos++];
                        
                    }
                    toDivide = newNew;
                    intermediateDividend = new HeftyInteger(toDivide);
                }
                
                //our divisor is goes into our dividend
                if(divisor.compareTo(intermediateDividend) == -1)
                {

                HeftyInteger[] results = intermediateDividend.div(divisor);
                if(results[0].length() == 1)
                    res[count++] = results[0].getVal()[0];
                else if(results[0].length() == 0)
                {
                }
                else
                    res[count++] = (results[0].getVal()[results[0].getVal().length-1]);
                if(res[count-1] == 0)
                {
                }
                remainder = results[1];
                //if we have no remainder
                if(remainder.equals(new HeftyInteger(ZERO)))
                {
                    toDivide = new byte[yA.length];
            
                    
                    int initialPos = currentPos;
                    int cc=0;

                    byte[] newNew = new byte[toDivide.length+1];
                    for(int z = 0;z<newNew.length;z++)
                    {
                        if(z < toDivide.length)
                        newNew[z] = toDivide[z];
                        else
                        newNew[z] = xA[currentPos++];
                        
                    }
                    toDivide = newNew;
                    intermediateDividend = new HeftyInteger(toDivide);

                    //take next "divisor length" chunk of dividend
                    // for(int q = currentPos;q<xA.length && (q-initialPos) < toDivide.length ;q++)
                    // {
                    //     toDivide[cc++] = xA[q];
                    // }
                    //from current count position -> iterate equal to number of spots to divide and place zeros
                    for(int ww = count-1; ww< toDivide.length+count;ww++)
                    {
                        res[ww] = (byte) 0;
                    }
                    
                    
                }
                else
                {
                    toDivide = remainder.getVal();
                }
            }
            else
            {

            }
            }
            
            if(remainder.compareTo(new HeftyInteger(ZERO)) > 0)
            {

                HeftyInteger intermediateDividend = new HeftyInteger(remainder.getVal());
                //if our divisor is less than our intermediate dividend, we must append a digit from the actual dividend
                if(divisor.compareTo(intermediateDividend) == 1)
                {
                    
                    byte[] newNew = new byte[toDivide.length+1];
                    if(xA.length == currentPos)
                    {

                        byte[] newRes;
                        if(res.length != 1)
                        {
                        newRes = new byte[res.length - 1];
                        for (int i = 0; i < newRes.length; i++) {
                            newRes[i] = res[i];
                        }
                        res = newRes;
                        }
                        return new HeftyInteger[] { new HeftyInteger(res), remainder };
                    }
                    for(int z = 0;z<newNew.length;z++)
                    {
                        if(z < toDivide.length)
                        newNew[z] = toDivide[z];
                        else
                        newNew[z] = xA[currentPos++];

                        
                    }
                    toDivide = newNew;
                    intermediateDividend = new HeftyInteger(toDivide);
                }
                //our divisor goes into our dividend
                HeftyInteger[] results = intermediateDividend.div(divisor);
                
                if(results[0].length() == 1)
                    res[count++] = results[0].getVal()[0];
                else if(results[0].length() == 0)
                {
                    //do nothing no elements
                }
                else
                    res[count++] = (results[0].getVal()[results[0].getVal().length-1]);
                remainder = results[1];
            }
           
            return new HeftyInteger[]{new HeftyInteger(res),new HeftyInteger(remainder.getVal())};
    }

    private HeftyInteger[] standardDivide(HeftyInteger other) 
    {
        byte[] xA = this.getVal();
        byte yA = other.getVal()[other.length()-1];
        int carry = 0;
        byte[] result;
        
        result = new byte[xA.length];
        int addOn = 0;
        for(int i = 0;i< xA.length;i++)
        {
            int xVal = (int) (xA[i] & 0xFF);
            int yVal = (int) (yA & 0xFF);
            carry = Math.floorDiv((xVal + (addOn)),(yVal));
            addOn = Math.abs((xVal + addOn) - (yVal * (carry))); 
            result[i] = (byte) (carry & 0xFF);

            carry = carry >>> 8;
            addOn*= 256;
            carry+=addOn;
        }
        byte[] remainder = new byte[xA.length];
        int pos = 0;
        while(carry != 0)
        {
            remainder[pos++] = (byte)(carry & 0xFF);
            carry = carry >>>8;
        }
        truncateArray(remainder);
        return new HeftyInteger[]{new HeftyInteger(result),new HeftyInteger(remainder)};
    }

    private HeftyInteger mod(HeftyInteger other) 
    {
        return this.divide(other)[1];
    }
    private HeftyInteger truncateArray(byte[] a)
    {
        boolean matchFound = false;
        int newSize = 0;
        for(int i =0;i<a.length;i++)
        {
            if(matchFound == false)
            {
                if(a[i] != 0)
                {
                    matchFound = true;
                    newSize++;
                }
            }
            else
                newSize++;
        }
        byte[] newArray = new byte[newSize];
        if(newSize == 1)
        {
            newArray[0] = a[a.length-1];
        }
        else
        {
        int count = 0;
        matchFound = false;
        for(int i = 0;i<a.length;i++)
        {
            if(matchFound == false)
            {
                if(a[i] != 0)
                {
                    matchFound = true;
                    newArray[count++] = a[i];
                }
            }
            else
                newArray[count++] = a[i];        
        }
        }
        a = newArray;
        return new HeftyInteger(a);
    }
    public String toString()
    {
        byte[] a = this.getVal();
        String s = "";
        for(byte b:a)
            s = s + Byte.toUnsignedInt(b) + " ";
        return s;
    }

    public static HeftyInteger createFromInt(int a)
    {
        byte[] res = new byte[100];
        
        int lp = res.length-1;
        int zp=0;
        boolean beginningFound = false;
        while(a > 0)
        {
            res[lp--] = (byte) (a & 0xFF);
            
            zp++;

            a = a >>> 8;
        }

        byte[] ret = new byte[zp];
        int count = 0;
        beginningFound = false;
        for(int i = lp+1; i < res.length;i++)
        {
            if(beginningFound == false)
            {
                if(res[i] != 0 && res[i] > 0)
                {
                beginningFound = true;
                ret[count++] = res[i];
                }
            }
            else
                ret[count++] = res[i];
        }
        return new HeftyInteger(ret);
    }

}

