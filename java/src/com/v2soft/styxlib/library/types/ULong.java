package com.v2soft.styxlib.library.types;

import java.math.BigInteger;

public class ULong {
	public static final int ULONG_LENGTH = 8;
    public static final ULong ZERO = new ULong(0L);
    
    private byte[] mBytes;
    
    public ULong(byte[] b) {
    	assert b != null;
    	assert b.length == ULONG_LENGTH;
        this.mBytes = b;
    }
    
    public ULong(long l) {        
        this.mBytes = new byte[ULONG_LENGTH];
        this.setValue(l);
    }
    
    public BigInteger asBigInteger() {
        byte[] revBytes = new byte[ULONG_LENGTH];
        for (int i = 0; i < ULONG_LENGTH; i++) {
            revBytes[i] = this.mBytes[ULONG_LENGTH - 1 - i];
        }
        return new BigInteger(revBytes);
    }
    
    public long asLong() {
        return this.asBigInteger().longValue();
    }
    
    public byte[] getBytes() {
        return mBytes;
    }
    
    public void setValue(long l) {
        this.mBytes[0] = (byte)l;
        this.mBytes[1] = (byte)(l >> 8);
        this.mBytes[2] = (byte)(l >> 16);
        this.mBytes[3] = (byte)(l >> 24);
        this.mBytes[4] = (byte)(l >> 32);
        this.mBytes[5] = (byte)(l >> 40);
        this.mBytes[6] = (byte)(l >> 48);
        this.mBytes[7] = (byte)(l >> 56);        
    }

    // TODO this method may be wrong
    public ULong add(long value) {
    	long v = this.asLong();
    	return new ULong(v + value);
    }
    
    public String toString() {
        return this.asBigInteger().toString();
    }
    
    @Override
    public boolean equals(Object otherULong)  {
        if (otherULong == null)
            return false;
        
        if (otherULong instanceof ULong)
        {
            ULong ul2 = (ULong)otherULong;
            for (int i = 0; i < ULONG_LENGTH; i++)
            {
                if (this.mBytes[i] != ul2.mBytes[i])
                    return false;
            }
            
            return true;
        }
        
        return false;
    }
    
}
