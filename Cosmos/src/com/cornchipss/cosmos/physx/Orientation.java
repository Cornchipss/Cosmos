package com.cornchipss.cosmos.physx;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import com.cornchipss.cosmos.utils.Maths;

public class Orientation
{
	private Quaternionf rotation;
	
	private Quaternionf temp;
	
	private Vector3f right, up, forward;

	public Orientation(Quaternionfc starting)
	{
		rotation = Maths.clone(starting);
		temp = Maths.blankQuaternion();
		 
		forward = new Vector3f(0, 0, 1);
		up = new Vector3f(0, 1, 0);
		right = new Vector3f(1, 0, 0);
		
		update();
	}
	
	public Orientation()
	{
		rotation = Maths.blankQuaternion();
		temp = Maths.blankQuaternion();
		 
		forward = new Vector3f(0, 0, 1);
		up = new Vector3f(0, 1, 0);
		right = new Vector3f(1, 0, 0);
	}
	
	public void applyRotation(Matrix4f transMatrix)
	{
		transMatrix.rotate(rotation);
	}

	public void rotateRelative(Vector3fc dRot)
	{
		rotateRelative(dRot, right, up, forward);
	}
	
	public void rotateRelative(Vector3fc dRot, Vector3fc right, Vector3fc up, Vector3fc forward)
	{
		// Default quaternion
		temp.x = 0;
		temp.y = 0;
		temp.z = 0;
		temp.w = 1;
		
		temp.rotateAxis(dRot.z(), forward);
		temp.rotateAxis(dRot.y(), up);
		temp.rotateAxis(dRot.x(), right);
		
		temp.mul(rotation, temp);
		
		rotation.set(temp);
		
		update();
	}

	public Quaternionfc quaternion()
	{
		return rotation;
	}
	
	public void quaternion(Quaternionfc q)
	{
		rotation.set(q);
		
		update();
	}

	private void update()
	{
		rotation.transform(0, 0, -1, forward); // opengl moment
		
		rotation.transform(1, 0, 0, right);
		
		rotation.transform(0, 1, 0, up);
	}
	
	public void zero()
	{
		rotation.set(0, 0, 0, 1);
		update();
	}
	
	public Vector3fc forward()
	{
		return forward;
	}
	
	public Vector3fc up()
	{
		return up;
	}
	
	public Vector3fc right()
	{
		return right;
	}
}
