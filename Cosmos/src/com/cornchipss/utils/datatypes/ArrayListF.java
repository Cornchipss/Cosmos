package com.cornchipss.utils.datatypes;

import java.io.Serializable;
import java.util.RandomAccess;

/**
 * <p>A way faster version of the standard {@link java.util.ArrayList} just for primitive floats</p>
 * <p>Main benifit is the {@link ArrayListF#asArray} being quite fast.</p>
 * @author Cornchip
 */
public class ArrayListF implements Serializable, RandomAccess, Cloneable
{
	private static final long serialVersionUID = -6294067774439727369L;
	
	public static final int DEFAULT_SIZE = 3000;
	public static final int DEFAULT_INCR_AMOUNT = 100;
	
	private float[] list;
	
	private int size = 0;
	private int icrAmount;
	
	public ArrayListF()
	{
		this(DEFAULT_SIZE);
	}
	
	public ArrayListF(int size)
	{
		this(size > 0 ? size : 1, DEFAULT_INCR_AMOUNT);
	}
	
	public ArrayListF(int size, int icrAmount, float... floats)
	{
		if(floats.length == size)
			list = floats.clone();
		else if(floats.length != 0)
		{
			list = new float[size];
			System.arraycopy(floats, 0, list, 0, floats.length);
		}
		else
			list = new float[size];
		
		this.icrAmount = icrAmount;
	}
	
	public void add(float f)
	{
		add(size, f);
	}
	
	private void expand(int amt, int splitIndex, float value)
	{
		float[] temp = new float[list.length + amt * (int)Math.ceil(splitIndex / (double)icrAmount)];
		System.arraycopy(list, 0, temp, 0, splitIndex);
		temp[splitIndex] = value;
		
		if(splitIndex < size())
			System.arraycopy(list, splitIndex, temp, splitIndex + 1, size() - splitIndex);
		
		list = temp;
	}
	
	public void add(int i, float f)
	{
		if(i + 1 >= list.length)
			expand(icrAmount, i, f);
		else if(i < size())
			System.arraycopy(list, i, list, i + 1, size - i);
		
		list[i] = f;
		size++;
	}
	
	public void remove(int i)
	{
//		if(i >= size() || i < 0)
//			throw new IndexOutOfBoundsException("Cannot remove index " + i + " from list of size " + size());
//		if(i < size() - 1)
//			System.arraycopy(list, i + 1, list, i, size() - i - 1);
//		else
//			list[i] = 0; // No shifting required if it's the last digit
//		size--;
		
		remove(i, 1);
	}
	
	public void remove(int start, int len)
	{
		if(start + len > size() || start < 0)
			throw new IndexOutOfBoundsException("Cannot remove index " + start + " to " + (start + len) + " from list of size " + size());
		
//		if(start + len != size())
			System.arraycopy(list, start + len, list, start, list.length - (start + len));
		
		// If it's equal to the size of the array, we can just shrink the size and not worry about shifting stuff over
		
		size -= len;
	}
	
	public void shrink()
	{
		if(size() != list.length)
		{
			float[] temp = new float[size()];
			System.arraycopy(list, 0, temp, 0, size());
			list = temp;
		}
	}
	
	/**
	 * <p>Converts the {@link ArrayListF} to a standard float array that has the length of {@link ArrayListF#size()}</p>
	 * <p>To save time, this array is a direct reference to the internal array in the ArrayListF, so modifying one will effect the other (aka don't mess with the returned copy).</p>
	 * <p>The {@link #shrink()} method is called during this to ensure lengths are correct
	 * @return The ArrayListF as standard float array that does <b>not</b> have the length of {@link ArrayListF#size()}
	 */
	public float[] getArray()
	{
		shrink();
		return list;
	}
	
	public float get(int i)
	{
		return list[i];
	}
	
	public int size()
	{
		return size;
	}
	
	@Override
	public String toString()
	{
		StringBuilder b = new StringBuilder();
		b.append("[");
		for(int i = 0; i < size(); i++)
		{
			if(i != 0)
				b.append(", ");
			b.append(get(i));
		}
		b.append("]");
		return b.toString();
	}
	
	@Override
	public ArrayListF clone()
	{
		return new ArrayListF(list.length, icrAmount, list);
	}

	public void clear()
	{
		clear(list.length);
	}
	
	public void clear(int newSize)
	{
		list = new float[newSize];
		size = 0;
	}

	public void swap(int i, int j)
	{
		float temp = list[i];
		list[i] = list[j];
		list[j] = temp;
	}

	public boolean empty()
	{
		return size() == 0;
	}
}
