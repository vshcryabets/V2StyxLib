package com.v2soft.styxlib.library.types;

import java.math.BigInteger;

public class ULong {
	public static final int ULONG_LENGTH = 8;
    public static final ULong ZERO = new ULong(0L);
    
    private byte[] bytes;
    
    public ULong(byte[] b)
    {
        if (b == null)
            throw new IllegalArgumentException("Input array of bytes cannot be null");
        if (b.length != ULONG_LENGTH)
            throw new IllegalArgumentException("ULong must be "
                + ULONG_LENGTH + " bytes long");
        
        this.bytes = b;
    }
    
    public ULong(long l)
    {        
        this.bytes = new byte[ULONG_LENGTH];
        this.setValue(l);
    }
    
    public BigInteger asBigInteger()
    {
        byte[] revBytes = new byte[ULONG_LENGTH];
        for (int i = 0; i < ULONG_LENGTH; i++)
        {
            revBytes[i] = this.bytes[ULONG_LENGTH - 1 - i];
        }
        
        return new BigInteger(revBytes);
    }
    
    public long asLong()
    {
        return this.asBigInteger().longValue();
    }
    
    public byte[] getBytes()
    {
        return bytes;
    }
    
    public void setValue(long l)
    {
        this.bytes[0] = (byte)l;
        this.bytes[1] = (byte)(l >> 8);
        this.bytes[2] = (byte)(l >> 16);
        this.bytes[3] = (byte)(l >> 24);
        this.bytes[4] = (byte)(l >> 32);
        this.bytes[5] = (byte)(l >> 40);
        this.bytes[6] = (byte)(l >> 48);
        this.bytes[7] = (byte)(l >> 56);        
    }
    
    public ULong add(long value)
    {
    	long v = this.asLong();
    	return new ULong(v + value);
    }
    
    public String toString()
    {
        return this.asBigInteger().toString();
    }
    
    public boolean equals(Object otherULong)
    {
        if (otherULong == null)
            return false;
        
        if (otherULong instanceof ULong)
        {
            ULong ul2 = (ULong)otherULong;
            for (int i = 0; i < ULONG_LENGTH; i++)
            {
                if (this.bytes[i] != ul2.bytes[i])
                    return false;
            }
            
            return true;
        }
        
        return false;
    }
    
}
